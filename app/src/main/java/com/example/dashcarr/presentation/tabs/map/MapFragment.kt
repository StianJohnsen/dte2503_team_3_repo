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
import android.preference.PreferenceManager
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.dashcarr.R
import com.example.dashcarr.databinding.FragmentMapBinding
import com.example.dashcarr.extensions.checkLocationPermissions
import com.example.dashcarr.extensions.collectWithLifecycle
import com.example.dashcarr.extensions.hideKeyboard
import com.example.dashcarr.extensions.locationPermissions
import com.example.dashcarr.extensions.setHeightSmooth
import com.example.dashcarr.extensions.toastShort
import com.example.dashcarr.extensions.toastThrowableShort
import com.example.dashcarr.presentation.core.BaseFragment
import com.example.dashcarr.presentation.mapper.toMarker
import com.example.dashcarr.presentation.tabs.camera.dashcam.DashcamFragment
import com.example.dashcarr.presentation.tabs.map.data.PointOfInterest
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
    FragmentMapBinding::inflate,
    showBottomNavBar = true
), LocationListener, MapEventsReceiver {

    private val mapViewModel: MapViewModel by viewModels()
    private val recordingViewModel: RecordingViewModel by viewModels()
    private val sensorRecordingViewModel by lazy {
        ViewModelProvider(this)[SensorRecordingViewModel::class.java]
    }

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


    private var isTimerPaused = true

    private var elapsedTime = ""

    private var isRecordingLocation = false

    private var recordingJson = JSONObject()


    /**
     * Observes the ViewModel's LiveData and updates the UI accordingly.
     */
    private fun observeViewModel() {
        mapViewModel.lastSavedUserLocation.collectWithLifecycle(viewLifecycleOwner) {
            binding.mapView.controller.setCenter(it)
        }
        sensorRecordingViewModel.rpmLiveData.isRecording.observe(viewLifecycleOwner) {
            isRecordingLocation = it
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

        sensorRecordingViewModel.rpmLiveData.isBtnStartShowing.observe(viewLifecycleOwner) {
            binding.btnStart.visibility = it
        }
        sensorRecordingViewModel.rpmLiveData.isBtnStopShowing.observe(viewLifecycleOwner) {
            binding.btnStop.visibility = it
        }

        sensorRecordingViewModel.rpmLiveData.isBtnPauseShowing.observe(viewLifecycleOwner) {
            binding.btnPause.visibility = it
        }

        sensorRecordingViewModel.rpmLiveData.isBtnResumeShowing.observe(viewLifecycleOwner) {
            binding.btnResume.visibility = it
        }

        sensorRecordingViewModel.rpmLiveData.isBtnDeleteShowing.observe(viewLifecycleOwner) {
            binding.btnDelete.visibility = it
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
            if (!requireContext().checkLocationPermissions()) {
                toastShort(getString(R.string.some_permissions_missing))
                return@setOnClickListener
            }
            binding.mapView.controller.animateTo(myLocationOverlay.myLocation)
            binding.mapView.controller.setZoom(15.0)
            createLocationRequest()
        }
        binding.buttonMaximize.setOnClickListener {
            findNavController().navigate(R.id.action_action_map_to_action_hud)
        }

        binding.btnSendMessage.setOnClickListener {
            findNavController().navigate(R.id.action_action_map_to_selectContactFragment)
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
        binding.apply {
            btnStart.setOnClickListener {
                startRecording()
                sensorRecordingViewModel.rpmLiveData.setIsRecordingButtonsShowing("Start", false)
                sensorRecordingViewModel.rpmLiveData.setIsRecordingButtonsShowing("Stop", true)
                sensorRecordingViewModel.rpmLiveData.setIsRecordingButtonsShowing("Pause", true)
                sensorRecordingViewModel.rpmLiveData.setIsRecordingButtonsShowing("Delete", true)
            }
            btnStop.setOnClickListener {
                stopRecording()
                sensorRecordingViewModel.rpmLiveData.setIsRecordingButtonsShowing("Stop", false)
                sensorRecordingViewModel.rpmLiveData.setIsRecordingButtonsShowing("Start", true)
                sensorRecordingViewModel.rpmLiveData.setIsRecordingButtonsShowing("Pause", false)
                sensorRecordingViewModel.rpmLiveData.setIsRecordingButtonsShowing("Delete", false)
                sensorRecordingViewModel.rpmLiveData.setIsRecordingButtonsShowing("Resume", false)
            }
            btnPause.setOnClickListener {
                pauseRecording()
                sensorRecordingViewModel.rpmLiveData.setIsRecordingButtonsShowing("Pause", false)
                sensorRecordingViewModel.rpmLiveData.setIsRecordingButtonsShowing("Resume", true)
            }
            btnResume.setOnClickListener {
                resumeRecording()
                sensorRecordingViewModel.rpmLiveData.setIsRecordingButtonsShowing("Resume", false)
                sensorRecordingViewModel.rpmLiveData.setIsRecordingButtonsShowing("Pause", true)
            }
            btnDelete.setOnClickListener {
                deleteRecording()
                sensorRecordingViewModel.rpmLiveData.setIsRecordingButtonsShowing("Delete", false)
                sensorRecordingViewModel.rpmLiveData.setIsRecordingButtonsShowing("Pause", false)
                sensorRecordingViewModel.rpmLiveData.setIsRecordingButtonsShowing("Stop", false)
                sensorRecordingViewModel.rpmLiveData.setIsRecordingButtonsShowing("Start", true)
                sensorRecordingViewModel.rpmLiveData.setIsRecordingButtonsShowing("Resume", false)
            }
        }


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
                btnDashcam.setOnClickListener {
                    if (!DashcamFragment.exists()) {
                        val transaction = parentFragmentManager.beginTransaction()
                        transaction.add(R.id.nav_host_container, DashcamFragment.getInstance())
                        transaction.disallowAddToBackStack()
                        transaction.commit()
                    } else {
                        DashcamFragment.getInstance().saveRecording()
                    }
                }
                btnOutOfCarMode.setOnClickListener {
                    findNavController().navigate(R.id.action_action_map_to_action_security_camera)
                }
            }
            accelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            gyroSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
            magnetoSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
            binding.btnStart.callOnClick()
        } else {
            binding.llSideButtons.visibility = View.GONE
            binding.llRecordingButtons.visibility = View.GONE
        }
    }

    /**
     * Requests location permissions using the AndroidX Activity Result API.
     */
    private fun requestLocationPermission() {
        requestLocationPermissionsLauncher.launch(locationPermissions)
    }

    override fun onResume() {
        super.onResume()
        sensorRecordingViewModel.rpmLiveData.registerSensors()

        val sensorSamplingRate: Int

        if (PowerSavingMode.getPowerMode()) {
            sensorSamplingRate = SensorManager.SENSOR_DELAY_NORMAL
        } else {
            sensorSamplingRate = SensorManager.SENSOR_DELAY_FASTEST
        }

        Log.d(this::class.simpleName, sensorSamplingRate.toString())


    }

    override fun onPause() {
        super.onPause()
        sensorRecordingViewModel.rpmLiveData.unregisterSensors()
    }

    /**
     * Stops listening for sensorChanges
     */


    private fun startRecording() {
        recordingViewModel.startRecording()
        sensorRecordingViewModel.rpmLiveData.setIsRecording(true)
        isTimerPaused = false
        Toast.makeText(context, "Recording Started", Toast.LENGTH_SHORT).show()
    }

    private fun stopRecording() {
        recordingViewModel.stopRecording()
        sensorRecordingViewModel.rpmLiveData.setIsRecording(false)
        saveToCSV()
    }

    private fun saveToFile(stringBuilder: StringBuilder, filtered: String, sensor: String, dateTime: LocalDateTime) {
        val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy_HH-mm-ss")
        context?.openFileOutput("${dateTime.format(formatter)}_${filtered}_${sensor}.csv", Context.MODE_PRIVATE).use {
            it?.write(stringBuilder.toString().toByteArray())
        }
    }


    private fun saveToCSV() {
        val stopDateTime = LocalDateTime.now()

        val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
        recordingJson.put("name", "Recording from ${stopDateTime.format(formatter)}")
        recordingJson.put("elapsed_time", elapsedTime)
        recordingJson.put("date", stopDateTime)
        var locationStringBuilder = StringBuilder()

        // Location
        sensorRecordingViewModel.rpmLiveData.unfilteredLocationList.observe(viewLifecycleOwner) {
            locationStringBuilder = sensorRecordingViewModel.rpmLiveData.buildSensorStringBuilder(it)

        }
        saveToFile(locationStringBuilder, "unfiltered", "GPS", stopDateTime)
        makeJSONObject(stopDateTime, "unfil", "GPS")


        // Accelerometer
        var currentStringBuilder = StringBuilder()
        sensorRecordingViewModel.rpmLiveData.filteredAccelerometerList.observe(viewLifecycleOwner) {
            currentStringBuilder = sensorRecordingViewModel.rpmLiveData.buildSensorStringBuilder(it)

        }
        saveToFile(currentStringBuilder, "filtered", "accel", stopDateTime)
        makeJSONObject(stopDateTime, "fil", "accel")

        sensorRecordingViewModel.rpmLiveData.unfilteredAccelerometerList.observe(viewLifecycleOwner) {
            currentStringBuilder = sensorRecordingViewModel.rpmLiveData.buildSensorStringBuilder(it)

        }
        saveToFile(currentStringBuilder, "unfiltered", "accel", stopDateTime)
        makeJSONObject(stopDateTime, "unfil", "accel")

        // Gyroscope
        sensorRecordingViewModel.rpmLiveData.filteredGyroScopeList.observe(viewLifecycleOwner) {
            currentStringBuilder = sensorRecordingViewModel.rpmLiveData.buildSensorStringBuilder(it)
        }
        saveToFile(currentStringBuilder, "filtered", "gyro", stopDateTime)
        makeJSONObject(stopDateTime, "fil", "gyro")

        sensorRecordingViewModel.rpmLiveData.unfilteredGyroscopeList.observe(viewLifecycleOwner) {
            currentStringBuilder = sensorRecordingViewModel.rpmLiveData.buildSensorStringBuilder(it)
        }

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

    private fun makeJSONObject(
        dateTime: LocalDateTime,
        filtered: String,
        sensor: String,
    ) {
        val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy_HH-mm-ss")
        recordingJson.put("${filtered}_${sensor}", "${dateTime.format(formatter)}_${filtered}tered_${sensor}.csv")
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


    private fun pauseRecording() {
        recordingViewModel.pauseRecording()
        sensorRecordingViewModel.rpmLiveData.setIsRecording(false)
        isTimerPaused = true
    }

    private fun resumeRecording() {
        recordingViewModel.resumeRecording()
        sensorRecordingViewModel.rpmLiveData.setIsRecording(true)
        isTimerPaused = false
    }

    private fun deleteRecording() {
        recordingViewModel.stopRecording()
        sensorRecordingViewModel.rpmLiveData.setIsRecording(false)
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

        if (isRecordingLocation) {
            sensorRecordingViewModel.rpmLiveData.insertIntoLocationList(location)
        }


    }


    override fun onStop() {
        mapViewModel.saveLastKnownLocation()
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isRecording) {
            stopRecording()
        }
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
                    redCircle.setColorFilter(ContextCompat.getColor(requireContext(), R.color.error_color))
                } else if (batteryPercent <= 25) {
                    greenCircle.setColorFilter(ContextCompat.getColor(requireContext(), R.color.dark_green))
                    yellowCircle.setColorFilter(ContextCompat.getColor(requireContext(), R.color.rename_button))
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