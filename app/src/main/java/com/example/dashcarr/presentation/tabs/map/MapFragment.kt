package com.example.dashcarr.presentation.tabs.map

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.icu.util.Calendar
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.SystemClock
import android.preference.PreferenceManager
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.dashcarr.R
import com.example.dashcarr.databinding.FragmentMapBinding
import com.example.dashcarr.extensions.collectWithLifecycle
import com.example.dashcarr.extensions.getFormattedDate
import com.example.dashcarr.extensions.locationPermissions
import com.example.dashcarr.extensions.setHeightSmooth
import com.example.dashcarr.extensions.toastThrowableShort
import com.example.dashcarr.presentation.core.BaseFragment
import com.example.dashcarr.presentation.mapper.toMarker
import com.example.dashcarr.presentation.tabs.map.data.PointOfInterest
import com.example.dashcarr.presentation.tabs.map.data.SensorData
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.time.LocalDateTime

/**
 * Fragment representing the map screen in the application. This fragment includes the map view,
 * handles location updates, and provides functionality to create markers on the map.
 *
 * @constructor Creates a new instance of MapFragment.
 */
@AndroidEntryPoint
class MapFragment : BaseFragment<FragmentMapBinding>(
    FragmentMapBinding::inflate,
    showBottomNavBar = true
), LocationListener, MapEventsReceiver, SensorEventListener {

    private val viewModel: MapViewModel by viewModels()
    private val recordingViewModel: RecordingViewModel by viewModels()


    // Location manager for handling location updates
    private val locationManager by lazy { requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager }

    // Activity Result Launcher for requesting location permissions
    private val requestLocationPermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionsResult ->
        if (permissionsResult.values.isEmpty()) return@registerForActivityResult
        if (permissionsResult.values.contains(false)) {
            Log.e(this::class.simpleName, "False location!")
        } else {
            Log.e(this::class.simpleName, "Got Location!")
            createLocationRequest()
        }
    }

    private lateinit var sensorManager: SensorManager
    private var accelSensor: Sensor? = null
    private var gyroSensor: Sensor? = null
    private var magnetoSensor: Sensor? = null
    private var isRecording = false
    private var isTimerPaused = true

    private var elapsedTime = ""


    // Accelerometer
    private var rawAccData = FloatArray(3)
    private var rawAccDataIndex = 0
    private var filtAccData = FloatArray(3)
    private var filtAccPrevData = FloatArray(3)
    private var rawAcclRecord = mutableListOf<SensorData>()
    private var filtAcclRecord = mutableListOf<SensorData>()

    private var count = 0
    private var beginTime = System.nanoTime()
    private var rc = 0.002f

    // Gyroscope
    private var rawGyroData = FloatArray(3)
    private var rawGyroDataIndex = 0
    private var filtGyroData = FloatArray(3)
    private var filtGyroPrevData = FloatArray(3)
    private var rawGyroRecord = mutableListOf<SensorData>()
    private var filtGyroRecord = mutableListOf<SensorData>()

    // Location
    private var rawLocationRecord = mutableListOf<SensorData>()

    private var recordingJson = JSONObject()


    override fun onAttach(context: Context) {
        super.onAttach(context)
        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }


    private var startTimeMillis: Long = 0
    private var totalElapsedTimeMillis: Long = 0
    private var pausedElapsedTimeMillis: Long = 0


    /**
     * Observes the ViewModel's LiveData and updates the UI accordingly.
     */
    private fun observeViewModel() {
        viewModel.lastSavedUserLocation.collectWithLifecycle(viewLifecycleOwner) {
            binding.mapView.controller.setCenter(it)
        }
        viewModel.createMarkerState.collectWithLifecycle(viewLifecycleOwner) {
            when (it) {
                is CreateMarkerState.CreateMarker -> showCreateMarkerDialog(it.geoPoint)
                is CreateMarkerState.HideMarker -> hideCreateMarkerDialog()
                is CreateMarkerState.OnSaveError -> {
                    toastThrowableShort(it.throwable)
                    hideCreateMarkerDialog()
                }
            }
            Log.e(
                this::class.simpleName,
                "createMarkerState = ${getFormattedDate(Calendar.getInstance().timeInMillis)}"
            )
        }
        viewModel.pointsOfInterestState.observe(viewLifecycleOwner) {
            if (it.isEmpty()) return@observe
            it.forEach { pointOfInterest ->
                binding.mapView.overlays.add(pointOfInterest.toMarker(binding.mapView))
            }
        }
        viewModel.showButtonsBarState.collectWithLifecycle(viewLifecycleOwner) { show ->
            show?.let {
                binding.llExpandedBar.setHeightSmooth(newHeight = if (show) -2 else 0)
            }
        }


        viewLifecycleOwner.lifecycleScope.launch {
            recordingViewModel.elapsedTime.collect() {
                elapsedTime = it
            }
        }
    }

    /**
     * Initializes listeners for UI elements in the fragment.
     */
    private fun initListeners() {
        binding.btnShowHideBar.setOnClickListener {
            viewModel.showHideBar()
        }
    }

    /**
     * Called when the fragment's view is created. Initializes the map, requests location permissions,
     * and sets up the ViewModel observation.
     *
     * @param view The fragment's root view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous
     * saved state.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListeners()
        observeViewModel()
        initMap()
        requestLocationPermission()
        // Observes variable appPreferences from viewModel to check if user has logged in before
        viewModel.appPreferences.observe(this.viewLifecycleOwner){
            if (!it.alreadyLoggedIn){
                findNavController().navigate(R.id.action_action_map_to_productFrontPage)
                viewModel.updateAppPreferences(true)
            }
        }
        /**
         * Sets visbility and functionality for recording buttons
         *
         */

        binding.apply {
            btnStart.setOnClickListener {
                startRecording()
                it.visibility = View.GONE
                btnStop.visibility = View.VISIBLE
                btnPause.visibility = View.VISIBLE
                btnDelete.visibility = View.VISIBLE
            }
            btnStop.setOnClickListener {
                stopRecording()
                it.visibility = View.GONE
                btnStart.visibility = View.VISIBLE
                btnPause.visibility = View.GONE
                btnDelete.visibility = View.GONE
            }
            btnPause.setOnClickListener {
                pauseRecording()
                it.visibility = View.GONE
                btnResume.visibility = View.VISIBLE
            }
            btnResume.setOnClickListener {
                resumeRecording()
                it.visibility = View.GONE
                btnPause.visibility = View.VISIBLE
            }
            btnDelete.setOnClickListener {
                deleteRecording()
                it.visibility = View.GONE
                btnPause.visibility = View.GONE
                btnStop.visibility = View.GONE
                btnStart.visibility = View.VISIBLE
            }
        }

        accelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        gyroSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)!!
        magnetoSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)!!
    }

    /**
     * Requests location permissions using the AndroidX Activity Result API.
     */
    private fun requestLocationPermission() {
        requestLocationPermissionsLauncher.launch(locationPermissions)
    }


    private fun readJsonFromFile(): JSONArray {
        var jsonArray = JSONArray()
        try {
            val inputStream = context?.openFileInput("sensor_config.json")
            if (inputStream != null) {
                val reader = BufferedReader(InputStreamReader(inputStream, Charset.forName("UTF-8")))
                val line: String? = reader.readLine()
                jsonArray = JSONArray(line.toString())
                inputStream.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return jsonArray
    }

    private fun writeToLocationStringBuilder(gpsList: MutableList<SensorData>): StringBuilder {
        val stringBuilder = StringBuilder()
        var id = 0
        stringBuilder.append("ID, GPS_Timestamp(ms), Longitude, Latitude, Altitude\n")
        gpsList.forEach {
            // Longitude = x, Latitude = y, Altitude = z
            stringBuilder.append("$id, ${it.timestamp}, ${it.x}, ${it.y}, ${it.z}\n")
            id++
        }
        return stringBuilder

    }


    private fun writeToSensorStringBuilder(sensor: String, sensorList: MutableList<SensorData>): StringBuilder {
        val stringBuilder = StringBuilder()
        var id = 0
        stringBuilder.append("ID, ${sensor}_Timestamp(ms), ${sensor}_X, ${sensor}_Y, ${sensor}_Z\n")
        sensorList.forEach {
            stringBuilder.append("$id, ${it.timestamp}, ${it.x}, ${it.y}, ${it.z}\n")
            id++
        }
        return stringBuilder
    }

    private fun saveToFile(stringBuilder: StringBuilder, filtered: String, sensor: String, dateTime: LocalDateTime) {
        context?.openFileOutput("${dateTime}_${filtered}_${sensor}.csv", Context.MODE_PRIVATE).use {
            it?.write(stringBuilder.toString().toByteArray())
        }
    }

    private fun makeJSONObject(
        dateTime: LocalDateTime,
        filtered: String,
        sensor: String,
    ) {
        recordingJson.put("${filtered}_${sensor}", "${dateTime}_${filtered}tered_${sensor}.csv")
    }

    private fun saveToCSV() {

        var currentStringBuilder: StringBuilder

        val stopDateTime = LocalDateTime.now()

        recordingJson.put("name", stopDateTime)
        recordingJson.put("elapsed_time", elapsedTime)
        recordingJson.put("date", stopDateTime)

        // Location
        currentStringBuilder = writeToLocationStringBuilder(rawLocationRecord)
        saveToFile(currentStringBuilder, "unfiltered", "GPS", stopDateTime)
        makeJSONObject(stopDateTime, "unfil", "GPS")

        // Accelerometer

        currentStringBuilder = writeToSensorStringBuilder("accel", filtAcclRecord)
        saveToFile(currentStringBuilder, "filtered", "accel", stopDateTime)
        makeJSONObject(stopDateTime, "fil", "accel")

        currentStringBuilder = writeToSensorStringBuilder("accel", rawAcclRecord)
        saveToFile(currentStringBuilder, "unfiltered", "accel", stopDateTime)
        makeJSONObject(stopDateTime, "unfil", "accel")

        // Gyroscope

        currentStringBuilder = writeToSensorStringBuilder("gyro", filtGyroRecord)
        saveToFile(currentStringBuilder, "filtered", "gyro", stopDateTime)
        makeJSONObject(stopDateTime, "fil", "gyro")

        currentStringBuilder = writeToSensorStringBuilder("Gyro", rawGyroRecord)
        saveToFile(currentStringBuilder, "unfiltered", "gyro", stopDateTime)
        makeJSONObject(stopDateTime, "unfil", "gyro")


        val existingJSONArray = readJsonFromFile()

        val jsonArray: JSONArray = existingJSONArray

        jsonArray.put(recordingJson)

        context?.openFileOutput("sensor_config.json", Context.MODE_PRIVATE).use {
            it?.write(jsonArray.toString().toByteArray())
        }
    }


    override fun onResume() {
        super.onResume()
        startTimeMillis = SystemClock.elapsedRealtime()

        sensorManager.registerListener(
            this, accelSensor, SensorManager.SENSOR_DELAY_FASTEST
        )
        sensorManager.registerListener(
            this, gyroSensor, SensorManager.SENSOR_DELAY_FASTEST
        )
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.also { accelerometer ->
            sensorManager.registerListener(
                this,
                accelerometer,
                SensorManager.SENSOR_DELAY_NORMAL,
                SensorManager.SENSOR_DELAY_UI
            )
        }
        sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)?.also { magneticField ->
            sensorManager.registerListener(
                this,
                magneticField,
                SensorManager.SENSOR_DELAY_NORMAL,
                SensorManager.SENSOR_DELAY_UI
            )
        }
    }

    /**
     * Stops listening for sensorChanges
     */
    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    private fun startRecording() {
        recordingViewModel.startRecording()
        resetRecording()
        isRecording = true
        isTimerPaused = false

    }

    private fun stopRecording() {
        recordingViewModel.stopRecording()
        isRecording = false
        saveToCSV()
    }

    private fun pauseRecording() {
        recordingViewModel.pauseRecording()
        isRecording = false
        isTimerPaused = true
    }

    private fun resumeRecording() {
        recordingViewModel.resumeRecording()
        isRecording = true
        isTimerPaused = false
        startTimeMillis = SystemClock.elapsedRealtime()
    }

    private fun deleteRecording() {
        recordingViewModel.stopRecording()
        resetRecording()
    }

    fun resetRecording() {
        isRecording = false
        isTimerPaused = true

        rawAccData = FloatArray(3)
        rawAccDataIndex = 0
        filtAccData = FloatArray(3)
        filtAccPrevData = FloatArray(3)
        rawAcclRecord = mutableListOf<SensorData>()
        filtAcclRecord = mutableListOf<SensorData>()

        count = 0
        beginTime = System.nanoTime()
        rc = 0.002f

        // Gyroscope
        rawGyroData = FloatArray(3)
        rawGyroDataIndex = 0
        filtGyroData = FloatArray(3)
        filtGyroPrevData = FloatArray(3)
        rawGyroRecord = mutableListOf<SensorData>()
        filtGyroRecord = mutableListOf<SensorData>()

        // Location
        rawLocationRecord = mutableListOf<SensorData>()

        recordingJson = JSONObject()

        startTimeMillis = 0
        totalElapsedTimeMillis = 0
        pausedElapsedTimeMillis = 0


    }

    /**
     * Initializes the map settings and overlays.
     */
    private fun initMap() {
        Configuration.getInstance()
            .load(requireContext(), PreferenceManager.getDefaultSharedPreferences(requireContext()))
        binding.mapView.setTileSource(TileSourceFactory.MAPNIK)
        binding.mapView.isHorizontalMapRepetitionEnabled = false
        binding.mapView.isVerticalMapRepetitionEnabled = false
        binding.mapView.setScrollableAreaLimitLatitude(
            MapView.getTileSystem().maxLatitude,
            MapView.getTileSystem().minLatitude,
            0
        )
        binding.mapView.setScrollableAreaLimitLongitude(
            MapView.getTileSystem().minLongitude,
            MapView.getTileSystem().maxLongitude,
            0
        )
        binding.mapView.minZoomLevel = 4.0
        binding.mapView.controller.setZoom(15.0)
        binding.mapView.isTilesScaledToDpi = true
        binding.mapView.overlays.add(MapEventsOverlay(this))
        binding.mapView.setMultiTouchControls(true)
        binding.mapView.zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER) // hide zoom buttons
    }

    /**
     * Requests location updates from the LocationManager for both GPS and network providers.
     * Also enables and adds a MyLocationNewOverlay to the map.
     */
    @SuppressLint("MissingPermission")
    fun createLocationRequest() {
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 100f, this)
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 2000, 100f, this)
        val overlay = MyLocationNewOverlay(binding.mapView)
        overlay.enableMyLocation()
        overlay.enableFollowLocation()
        binding.mapView.overlays.add(overlay)
    }

    override fun onLocationChanged(location: Location) {
        viewModel.saveCurrentLocation(location)
        Log.e(this::class.simpleName, "location changed! lat = ${location.latitude} , long = ${location.longitude}")
        if (isRecording) {
            rawLocationRecord.add(
                SensorData(
                    location.time,
                    location.longitude.toFloat(),
                    location.latitude.toFloat(),
                    location.altitude.toFloat()
                )
            )
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.values != null) {
            if (event.sensor == accelSensor) {
                readAccSensorData(event)
                if (isRecording) {
                    rawAcclRecord.add(
                        SensorData(
                            event.timestamp, event.values[0], event.values[1], event.values[2]
                        )
                    )

                    filtAcclRecord.add(
                        SensorData(event.timestamp, filtAccData[0], filtAccData[1], filtAccData[2])
                    )
                }


            }

            if (event.sensor == gyroSensor) {
                readGyroSensorData(event)
                if (isRecording) {
                    rawGyroRecord.add(
                        SensorData(
                            event.timestamp, event.values[0], event.values[1], event.values[2]
                        )
                    )
                    filtGyroRecord.add(
                        SensorData(
                            event.timestamp, filtGyroData[0], filtGyroData[1], filtGyroData[2]

                        )
                    )
                }
            }
        }
    }

    private fun readAccSensorData(event: SensorEvent) {
        System.arraycopy(event.values, 0, rawAccData, rawAccDataIndex, 3)
        filterAccData()
    }

    private fun readGyroSensorData(event: SensorEvent) {
        System.arraycopy(event.values, 0, rawGyroData, rawGyroDataIndex, 3)
        filterGyroData()
    }


    private fun filterGyroData() {
        val tm = System.nanoTime()
        val dt = ((tm - beginTime) / 1000000000.0f) / count
        val alpha = rc / (rc + dt)
        val isStarted = true

        if (count == 0) {
            filtGyroPrevData[0] = (1 - alpha) * rawGyroData[0]
            filtGyroPrevData[1] = (1 - alpha) * rawGyroData[1]
            filtGyroPrevData[2] = (1 - alpha) * rawGyroData[2]
        } else {
            filtGyroPrevData[0] = alpha * filtGyroPrevData[0] + (1 - alpha) * rawGyroData[0]
            filtGyroPrevData[1] = alpha * filtGyroPrevData[1] + (1 - alpha) * rawGyroData[1]
            filtGyroPrevData[2] = alpha * filtGyroPrevData[2] + (1 - alpha) * rawGyroData[2]
        }
        if (isStarted) {
            filtGyroData[0] = filtGyroPrevData[0]
            filtGyroData[1] = filtGyroPrevData[1]
            filtGyroData[2] = filtGyroPrevData[2]

        }
    }


    private fun filterAccData() {
        val tm = System.nanoTime()
        val dt = ((tm - beginTime) / 1000000000.0f) / count
        val alpha = rc / (rc + dt)
        val isStarted = true

        if (count == 0) {
            filtAccPrevData[0] = (1 - alpha) * rawAccData[0]
            filtAccPrevData[1] = (1 - alpha) * rawAccData[1]
            filtAccPrevData[2] = (1 - alpha) * rawAccData[2]
        } else {
            filtAccPrevData[0] = alpha * filtAccPrevData[0] + (1 - alpha) * rawAccData[0]
            filtAccPrevData[1] = alpha * filtAccPrevData[1] + (1 - alpha) * rawAccData[1]
            filtAccPrevData[2] = alpha * filtAccPrevData[2] + (1 - alpha) * rawAccData[2]
        }
        if (isStarted) {
            filtAccData[0] = filtAccPrevData[0]
            filtAccData[1] = filtAccPrevData[1]
            filtAccData[2] = filtAccPrevData[2]
        }
        ++count
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        Log.d(this::class.simpleName, sensor.toString())
    }

    override fun onStop() {
        viewModel.saveLastKnownLocation()
        super.onStop()
    }

    override fun singleTapConfirmedHelper(geoPoint: GeoPoint?): Boolean {
        return false
    }

    override fun longPressHelper(geoPoint: GeoPoint?): Boolean {
        geoPoint?.let { viewModel.createMarker(it) }
        return false
    }

    private fun hideCreateMarkerDialog() {
        binding.tilMarkerName.isErrorEnabled = false
        binding.etMarkerName.setText("")
        binding.btnSave.setOnClickListener(null)
        binding.btnCancel.setOnClickListener(null)
        binding.flCreateMarkerLayout.visibility = View.GONE
    }

    private fun showCreateMarkerDialog(geoPoint: GeoPoint) {
        binding.flCreateMarkerLayout.visibility = View.VISIBLE
        binding.btnSave.setOnClickListener {
            viewModel.saveNewMarker(
                PointOfInterest(
                    latitude = geoPoint.latitude,
                    longitude = geoPoint.longitude,
                    name = binding.etMarkerName.text.toString(),
                    createdTimeStamp = Calendar.getInstance().timeInMillis
                )
            )
        }
        binding.btnCancel.setOnClickListener {
            viewModel.cancelMarkerCreation()
        }
    }

    /**
     * State sealed class representing the creation of a marker on the map.
     */
    sealed class CreateMarkerState {
        class CreateMarker(val geoPoint: GeoPoint) : CreateMarkerState()
        class OnSaveError(val throwable: Throwable) : CreateMarkerState()
        data object HideMarker : CreateMarkerState()

    }

}