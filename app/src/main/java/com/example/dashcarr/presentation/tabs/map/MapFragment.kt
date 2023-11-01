package com.example.dashcarr.presentation.tabs.map

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.icu.util.Calendar
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.BatteryManager
import android.os.Bundle
import android.os.PowerManager
import android.os.SystemClock
import android.preference.PreferenceManager
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.dashcarr.R
import com.example.dashcarr.databinding.FragmentMapBinding
import com.example.dashcarr.extensions.collectWithLifecycle
import com.example.dashcarr.extensions.hideKeyboard
import com.example.dashcarr.extensions.locationPermissions
import com.example.dashcarr.extensions.setHeightSmooth
import com.example.dashcarr.extensions.toastThrowableShort
import com.example.dashcarr.presentation.core.BaseFragment
import com.example.dashcarr.presentation.mapper.toMarker
import com.example.dashcarr.presentation.tabs.map.data.PointOfInterest
import com.example.dashcarr.presentation.tabs.map.data.SensorData
import com.example.dashcarr.presentation.tabs.settings.PowerSavingMode
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
import java.time.format.DateTimeFormatter

/**
 * Fragment representing the map screen in the application. This fragment includes the map view,
 * handles location updates, and provides functionality to create markers on the map.
 *
 * @constructor Creates a new instance of MapFragment.
 */
@AndroidEntryPoint
class MapFragment : BaseFragment<FragmentMapBinding>(
    FragmentMapBinding::inflate, null
), LocationListener, MapEventsReceiver, SensorEventListener {

    private val mapViewModel: MapViewModel by viewModels()
    private val recordingViewModel: RecordingViewModel by viewModels()

    // Location manager for handling location updates
    private val locationManager by lazy { requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager }
    private lateinit var myLocationOverlay: MyLocationNewOverlay

    // Activity Result Launcher for requesting location permissions
    private val requestLocationPermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionsResult ->
        if (permissionsResult.values.isEmpty()) return@registerForActivityResult
        if (permissionsResult.values.contains(false)) {
            Log.e(this::class.simpleName, "False location!")
        } else {
            Log.d(this::class.simpleName, "Got Location!")
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
    private var rawAccSample = FloatArray(3)
    private var rawAccDataIndex = 0
    private var filtAccSample = FloatArray(3)
    private var filtAccPrevSample = FloatArray(3)
    private var rawAcclRecord = mutableListOf<SensorData>()
    private var filtAcclRecord = mutableListOf<SensorData>()

    private var stepNumber = 0

    // Gyroscope
    private var rawGyroSample = FloatArray(3)
    private var rawGyroDataIndex = 0
    private var filtGyroSample = FloatArray(3)
    private var filtGyroPrevSample = FloatArray(3)
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
        mapViewModel.lastSavedUserLocation.collectWithLifecycle(viewLifecycleOwner) {
            binding.mapView.controller.setCenter(it)
        }

        mapViewModel.createMarkerState.collectWithLifecycle(viewLifecycleOwner) {
            when (it) {
                is CreateMarkerState.CreateMarker -> showCreateMarkerDialog(it.geoPoint)
                is CreateMarkerState.HideMarker -> hideCreateMarkerDialog().also { hideKeyboard() }
                is CreateMarkerState.OnSaveError -> {
                    toastThrowableShort(it.throwable)
                    hideCreateMarkerDialog()
                }
            }
        }
        mapViewModel.pointsOfInterestState.observe(viewLifecycleOwner) {
            if (it.isEmpty()) return@observe
            it.forEach { pointOfInterest ->
                binding.mapView.overlays.add(pointOfInterest.toMarker(binding.mapView))
            }
        }
        mapViewModel.showButtonsBarState.collectWithLifecycle(viewLifecycleOwner) { show ->
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
            mapViewModel.showHideBar()
        }
        binding.btnCenterLocation.setOnClickListener {
            binding.mapView.controller.animateTo(myLocationOverlay.myLocation)
            binding.mapView.controller.setZoom(15.0)
        }
        binding.buttonMaximize.setOnClickListener {
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(R.id.nav_host_container, HudFragment())
            transaction.addToBackStack(null)
            transaction.commit()
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
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        val args: MapFragmentArgs by navArgs()
        showBottomNavigation(!args.isRideActivated)
        initListeners()
        observeViewModel()
        initMap()
        requestLocationPermission()
        if (PowerSavingMode.getPowerMode() && args.isRideActivated) {
            binding.llTrafficLight.visibility = View.VISIBLE
            setupBatteryStatus()
        } else {
            binding.llTrafficLight.visibility = View.GONE
        }

        val powerManager = requireContext().getSystemService(Context.POWER_SERVICE) as PowerManager

        PowerSavingMode.setPhonePowerMode(powerManager.isPowerSaveMode)
        // Observes variable appPreferences from viewModel to check if user has logged in before
        mapViewModel.appPreferences.observe(this.viewLifecycleOwner) {
            if (!it.alreadyLoggedIn) {
                findNavController().navigate(R.id.action_action_map_to_productFrontPage)
                mapViewModel.updateAppPreferences(true)
            }
        }

        Log.d(this::class.simpleName, "Current Power Mode: ${PowerSavingMode.getPowerMode()}")

        // sets visibility and functionality for recording buttons
        if (args.isRideActivated) {
            childFragmentManager.beginTransaction()
                .add(binding.hudView.id, HudFragment())
                .commit()
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
                    findNavController().navigate(R.id.action_action_map_to_action_history)
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
                    findNavController().navigate(R.id.action_action_map_to_action_history)
                }
            }
            accelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            gyroSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
            magnetoSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
            binding.btnStart.callOnClick()
        } else {
            binding.llRecordingButtons.visibility = View.GONE
        }
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
        val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy_HH-mm-ss")
        context?.openFileOutput("${dateTime.format(formatter)}_${filtered}_${sensor}.csv", Context.MODE_PRIVATE).use {
            it?.write(stringBuilder.toString().toByteArray())
        }
    }

    private fun makeJSONObject(
        dateTime: LocalDateTime,
        filtered: String,
        sensor: String,
    ) {
        val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy_HH-mm-ss")
        recordingJson.put("${filtered}_${sensor}", "${dateTime.format(formatter)}_${filtered}tered_${sensor}.csv")
    }

    private fun saveToCSV() {
        val stopDateTime = LocalDateTime.now()

        val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
        recordingJson.put("name", "Recording from ${stopDateTime.format(formatter)}")
        recordingJson.put("elapsed_time", elapsedTime)
        recordingJson.put("date", stopDateTime)

        // Location
        var currentStringBuilder: StringBuilder = writeToLocationStringBuilder(rawLocationRecord)
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
        Toast.makeText(context, "Saved", Toast.LENGTH_SHORT).show()
    }


    override fun onResume() {
        super.onResume()
        startTimeMillis = SystemClock.elapsedRealtime()

        val sensorSamplingRate: Int

        if (PowerSavingMode.getPowerMode()) {
            sensorSamplingRate = SensorManager.SENSOR_DELAY_NORMAL
        } else {
            sensorSamplingRate = SensorManager.SENSOR_DELAY_FASTEST
        }

        Log.d(this::class.simpleName, sensorSamplingRate.toString())



        sensorManager.registerListener(
            this, accelSensor, sensorSamplingRate
        )
        sensorManager.registerListener(
            this, gyroSensor, sensorSamplingRate
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
        Toast.makeText(context, "Recording Started", Toast.LENGTH_SHORT).show()
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

    private fun resetRecording() {
        isRecording = false
        isTimerPaused = true

        rawAccSample = FloatArray(3)
        rawAccDataIndex = 0
        filtAccSample = FloatArray(3)
        filtAccPrevSample = FloatArray(3)
        rawAcclRecord = mutableListOf<SensorData>()
        filtAcclRecord = mutableListOf<SensorData>()

        stepNumber = 0

        // Gyroscope
        rawGyroSample = FloatArray(3)
        rawGyroDataIndex = 0
        filtGyroSample = FloatArray(3)
        filtGyroPrevSample = FloatArray(3)
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
        myLocationOverlay = MyLocationNewOverlay(binding.mapView)
        myLocationOverlay.enableMyLocation()
        myLocationOverlay.enableFollowLocation()
        binding.mapView.overlays.add(myLocationOverlay)
    }

    override fun onLocationChanged(location: Location) {
        mapViewModel.saveCurrentLocation(location)
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
                        SensorData(event.timestamp, filtAccSample[0], filtAccSample[1], filtAccSample[2])
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
                            event.timestamp, filtGyroSample[0], filtGyroSample[1], filtGyroSample[2]

                        )
                    )
                }
            }
        }
    }

    private fun readAccSensorData(event: SensorEvent) {
        System.arraycopy(event.values, 0, rawAccSample, rawAccDataIndex, 3)
        filterAccData()
    }

    private fun readGyroSensorData(event: SensorEvent) {
        System.arraycopy(event.values, 0, rawGyroSample, rawGyroDataIndex, 3)
        filterGyroData()
    }


    private fun filterGyroData() {
        val alpha = 0.9F
        val isStarted = true

        if (stepNumber == 0) {
            filtGyroPrevSample[0] = rawGyroSample[0]
            filtGyroPrevSample[1] = rawGyroSample[1]
            filtGyroPrevSample[2] = rawGyroSample[2]
        } else {
            filtGyroPrevSample[0] = alpha * filtGyroPrevSample[0] + (1 - alpha) * rawGyroSample[0]
            filtGyroPrevSample[1] = alpha * filtGyroPrevSample[1] + (1 - alpha) * rawGyroSample[1]
            filtGyroPrevSample[2] = alpha * filtGyroPrevSample[2] + (1 - alpha) * rawGyroSample[2]
        }
        if (isStarted) {
            filtGyroSample[0] = filtGyroPrevSample[0]
            filtGyroSample[1] = filtGyroPrevSample[1]
            filtGyroSample[2] = filtGyroPrevSample[2]
        }
        ++stepNumber
    }


    private fun filterAccData() {
        val alpha = 0.9F
        val isStarted = true

        if (stepNumber == 0) {
            filtAccPrevSample[0] = rawAccSample[0]
            filtAccPrevSample[1] = rawAccSample[1]
            filtAccPrevSample[2] = rawAccSample[2]
        } else {
            filtAccPrevSample[0] = alpha * filtAccPrevSample[0] + (1 - alpha) * rawAccSample[0]
            filtAccPrevSample[1] = alpha * filtAccPrevSample[1] + (1 - alpha) * rawAccSample[1]
            filtAccPrevSample[2] = alpha * filtAccPrevSample[2] + (1 - alpha) * rawAccSample[2]
        }
        if (isStarted) {
            filtAccSample[0] = filtAccPrevSample[0]
            filtAccSample[1] = filtAccPrevSample[1]
            filtAccSample[2] = filtAccPrevSample[2]
        }
        ++stepNumber
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        Log.d(this::class.simpleName, sensor.toString())
    }

    override fun onStop() {
        mapViewModel.saveLastKnownLocation()
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()

    }

    override fun singleTapConfirmedHelper(geoPoint: GeoPoint?): Boolean {
        return false
    }

    override fun longPressHelper(geoPoint: GeoPoint?): Boolean {
        geoPoint?.let { mapViewModel.createMarker(it) }
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
            mapViewModel.saveNewMarker(
                PointOfInterest(
                    latitude = geoPoint.latitude,
                    longitude = geoPoint.longitude,
                    name = binding.etMarkerName.text.toString(),
                    createdTimeStamp = Calendar.getInstance().timeInMillis
                )
            )
        }
        binding.btnCancel.setOnClickListener {
            mapViewModel.cancelMarkerCreation()
        }
    }

    private fun setupBatteryStatus() {
        val redCircle: ImageView = view?.findViewById(R.id.redIndicator)!!
        val yellowCircle: ImageView = view?.findViewById(R.id.yellowIndicator)!!
        val greenCircle: ImageView = view?.findViewById(R.id.greenIndicator)!!

        val batteryStatusReceiver: BroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val level: Int = intent!!.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale: Int = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                val batteryPercent: Float = level * 100 / scale.toFloat()

                if (batteryPercent <= 15) {
                    greenCircle.setColorFilter(ContextCompat.getColor(requireContext(), R.color.dark_green))
                    yellowCircle.setColorFilter(ContextCompat.getColor(requireContext(), R.color.dark_yellow))
                    redCircle.setColorFilter(ContextCompat.getColor(requireContext(), R.color.red))
                } else if (batteryPercent <= 25) {
                    greenCircle.setColorFilter(ContextCompat.getColor(requireContext(), R.color.dark_green))
                    yellowCircle.setColorFilter(ContextCompat.getColor(requireContext(), R.color.yellow))
                    redCircle.setColorFilter(ContextCompat.getColor(requireContext(), R.color.dark_red))
                } else {
                    greenCircle.setColorFilter(ContextCompat.getColor(requireContext(), R.color.green))
                    yellowCircle.setColorFilter(ContextCompat.getColor(requireContext(), R.color.dark_yellow))
                    redCircle.setColorFilter(ContextCompat.getColor(requireContext(), R.color.dark_red))
                }
            }
        }

        val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        requireActivity().registerReceiver(batteryStatusReceiver, intentFilter)
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