package com.example.dashcarr.presentation.tabs.map

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ActivityInfo
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
import org.json.JSONObject
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.time.LocalDateTime

/**
 * Fragment representing the map screen in the application. This fragment includes the map view,
 * handles location updates, and provides functionality to create markers on the map.
 *
 * @constructor Creates a new instance of MapFragment.
 */
@AndroidEntryPoint
class MapFragment : BaseFragment<FragmentMapBinding>(
    FragmentMapBinding::inflate, null
), LocationListener, MapEventsReceiver {

    private val mapViewModel: MapViewModel by viewModels()

    //private val recordingViewModel: RecordingViewModel by viewModels()
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


    private var elapsedTime = ""

    private var isRecordingLocation = false

    private var isRecording = false

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
            sensorRecordingViewModel.recordViewModel.elapsedTime.collect {
                Log.d("timer", it)
                elapsedTime = it
            }
        }


        viewLifecycleOwner.lifecycleScope.launch {
            sensorRecordingViewModel.rpmLiveData.isBtnStopShowing.collect {
                binding.btnStop.visibility = it
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            sensorRecordingViewModel.rpmLiveData.isBtnPauseShowing.collect {
                binding.btnPause.visibility = it
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            sensorRecordingViewModel.rpmLiveData.isBtnResumeShowing.collect {
                binding.btnResume.visibility = it
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            sensorRecordingViewModel.rpmLiveData.isBtnDeleteShowing.collect {
                binding.btnDelete.visibility = it
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            sensorRecordingViewModel.rpmLiveData.isRecording.collect {
                isRecordingLocation = it
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            sensorRecordingViewModel.rpmLiveData.isBtnStartShowing.collect {
                binding.btnStart.visibility = it
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            sensorRecordingViewModel.rpmLiveData.isRecording.collect {
                isRecording = it
                Log.d("stian", it.toString())
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
                viewLifecycleOwner.lifecycleScope.launch {
                    sensorRecordingViewModel.rpmLiveData.setIsRecordingButtonsShowing("Start", false)
                    sensorRecordingViewModel.rpmLiveData.setIsRecordingButtonsShowing("Stop", true)
                    sensorRecordingViewModel.rpmLiveData.setIsRecordingButtonsShowing("Pause", true)
                    sensorRecordingViewModel.rpmLiveData.setIsRecordingButtonsShowing("Delete", true)
                }
                startRecording()


            }
            btnStop.setOnClickListener {
                stopRecording()
                viewLifecycleOwner.lifecycleScope.launch {
                    sensorRecordingViewModel.rpmLiveData.setIsRecordingButtonsShowing("Stop", false)
                    sensorRecordingViewModel.rpmLiveData.setIsRecordingButtonsShowing("Start", true)
                    sensorRecordingViewModel.rpmLiveData.setIsRecordingButtonsShowing("Pause", false)
                    sensorRecordingViewModel.rpmLiveData.setIsRecordingButtonsShowing("Delete", false)
                    sensorRecordingViewModel.rpmLiveData.setIsRecordingButtonsShowing("Resume", false)
                }
                findNavController().navigate(R.id.action_action_map_to_action_history)


            }
            btnPause.setOnClickListener {
                pauseRecording()
                viewLifecycleOwner.lifecycleScope.launch {
                    sensorRecordingViewModel.rpmLiveData.setIsRecordingButtonsShowing("Pause", false)
                    sensorRecordingViewModel.rpmLiveData.setIsRecordingButtonsShowing("Resume", true)
                }


            }
            btnResume.setOnClickListener {
                resumeRecording()
                viewLifecycleOwner.lifecycleScope.launch {
                    sensorRecordingViewModel.rpmLiveData.setIsRecordingButtonsShowing("Resume", false)
                    sensorRecordingViewModel.rpmLiveData.setIsRecordingButtonsShowing("Pause", true)
                }
            }

            btnDelete.setOnClickListener {
                deleteRecording()
                viewLifecycleOwner.lifecycleScope.launch {
                    sensorRecordingViewModel.rpmLiveData.setIsRecordingButtonsShowing("Delete", false)
                    sensorRecordingViewModel.rpmLiveData.setIsRecordingButtonsShowing("Pause", false)
                    sensorRecordingViewModel.rpmLiveData.setIsRecordingButtonsShowing("Stop", false)
                    sensorRecordingViewModel.rpmLiveData.setIsRecordingButtonsShowing("Start", true)
                    sensorRecordingViewModel.rpmLiveData.setIsRecordingButtonsShowing("Resume", false)
                }
                findNavController().navigate(R.id.action_action_map_to_action_history)


            }
        }


        if (args.isRideActivated) {
            childFragmentManager.beginTransaction()
                .add(binding.hudView.id, HudFragment())
                .commit()

            binding.apply {
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
    }

    override fun onPause() {
        super.onPause()
        sensorRecordingViewModel.rpmLiveData.unregisterSensors()
    }

    /**
     * Stops listening for sensorChanges
     */


    private fun startRecording() {
        viewLifecycleOwner.lifecycleScope.launch {
            sensorRecordingViewModel.recordViewModel.startRecording()
        }
        viewLifecycleOwner.lifecycleScope.launch {
            sensorRecordingViewModel.rpmLiveData.setIsRecording(true)
        }
        Toast.makeText(context, "Recording Started", Toast.LENGTH_SHORT).show()
    }

    private fun stopRecording() {
        //recordingViewModel.stopRecording()
        viewLifecycleOwner.lifecycleScope.launch {
            sensorRecordingViewModel.recordViewModel.stopRecording()
        }
        viewLifecycleOwner.lifecycleScope.launch {
            sensorRecordingViewModel.rpmLiveData.setIsRecording(false)
        }
        saveToCSV()
    }

    private fun saveToCSV() {
        val stopDateTime = LocalDateTime.now()

        var currentSensorStringObject = StringBuilder()

        val startJSONObject = sensorRecordingViewModel.rpmLiveData.makeStartJsonObject(elapsedTime)

        val unfilteredGpsJSONObject =
            sensorRecordingViewModel.rpmLiveData.makeJsonObject(stopDateTime, "unfil", "GPS", startJSONObject)

        val unfilAccelJsonObject =
            sensorRecordingViewModel.rpmLiveData.makeJsonObject(stopDateTime, "unfil", "accel", startJSONObject)
        val filAccelJsonObject =
            sensorRecordingViewModel.rpmLiveData.makeJsonObject(stopDateTime, "fil", "accel", startJSONObject)

        val unfilGyroJsonObject =
            sensorRecordingViewModel.rpmLiveData.makeJsonObject(stopDateTime, "unfil", "gyro", startJSONObject)
        val filGyroJsonObject =
            sensorRecordingViewModel.rpmLiveData.makeJsonObject(stopDateTime, "fil", "gyro", startJSONObject)

        val allSensorJsonObject = JSONObject()

        var keys = unfilteredGpsJSONObject.keys()

        var key: String
        var value: Any
        while (keys.hasNext()) {
            key = keys.next() as String
            value = unfilteredGpsJSONObject[key]
            allSensorJsonObject.put(key, value)
        }
        keys = unfilAccelJsonObject.keys()
        while (keys.hasNext()) {
            key = keys.next() as String
            value = unfilAccelJsonObject[key]
            allSensorJsonObject.put(key, value)
        }
        keys = filAccelJsonObject.keys()
        while (keys.hasNext()) {
            key = keys.next() as String
            value = filAccelJsonObject[key]
            allSensorJsonObject.put(key, value)
        }
        keys = unfilGyroJsonObject.keys()
        while (keys.hasNext()) {
            key = keys.next() as String
            value = unfilGyroJsonObject[key]
            allSensorJsonObject.put(key, value)
        }
        keys = filGyroJsonObject.keys()
        while (keys.hasNext()) {
            key = keys.next() as String
            value = filGyroJsonObject[key]
            allSensorJsonObject.put(key, value)
        }


        // Location
        viewLifecycleOwner.lifecycleScope.launch {
            sensorRecordingViewModel.rpmLiveData.unfilteredLocationList.collect {
                currentSensorStringObject =
                    sensorRecordingViewModel.rpmLiveData.buildSensorStringBuilder(it)

            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            context?.let {
                sensorRecordingViewModel.rpmLiveData.saveToFile(
                    it,
                    currentSensorStringObject,
                    "unfiltered",
                    "GPS",
                    stopDateTime
                )

            }
        }


        // Accelerometer

        viewLifecycleOwner.lifecycleScope.launch {
            sensorRecordingViewModel.rpmLiveData.filteredAccelerometerList.collect {
                currentSensorStringObject =
                    sensorRecordingViewModel.rpmLiveData.buildSensorStringBuilder(it)
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            context?.let {
                sensorRecordingViewModel.rpmLiveData.saveToFile(
                    it,
                    currentSensorStringObject,
                    "filtered",
                    "accel",
                    stopDateTime
                )
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            sensorRecordingViewModel.rpmLiveData.unfilteredAccelerometerList.collect {
                currentSensorStringObject =
                    sensorRecordingViewModel.rpmLiveData.buildSensorStringBuilder(it)
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            context?.let {
                sensorRecordingViewModel.rpmLiveData.saveToFile(
                    it,
                    currentSensorStringObject,
                    "unfiltered",
                    "accel",
                    stopDateTime
                )
            }
        }

        // GyroScope

        viewLifecycleOwner.lifecycleScope.launch {
            sensorRecordingViewModel.rpmLiveData.filteredGyroScopeList.collect {
                currentSensorStringObject =
                    sensorRecordingViewModel.rpmLiveData.buildSensorStringBuilder(it)
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            context?.let {
                sensorRecordingViewModel.rpmLiveData.saveToFile(
                    it,
                    currentSensorStringObject,
                    "filtered",
                    "gyro",
                    stopDateTime
                )
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            sensorRecordingViewModel.rpmLiveData.unfilteredGyroscopeList.collect {
                currentSensorStringObject =
                    sensorRecordingViewModel.rpmLiveData.buildSensorStringBuilder(it)
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            context?.let {
                sensorRecordingViewModel.rpmLiveData.saveToFile(
                    it,
                    currentSensorStringObject,
                    "unfiltered",
                    "gyro",
                    stopDateTime
                )
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            val existingJsonArray = context?.let { sensorRecordingViewModel.rpmLiveData.readJsonFromFile(it) }
            context?.let {
                if (existingJsonArray != null) {
                    sensorRecordingViewModel.rpmLiveData.writeToJsonFile(it, existingJsonArray, allSensorJsonObject)
                }
            }
        }

        Toast.makeText(context, "Recording Saved", Toast.LENGTH_SHORT).show()
    }

    private fun pauseRecording() {
        //recordingViewModel.pauseRecording()
        viewLifecycleOwner.lifecycleScope.launch {
            sensorRecordingViewModel.recordViewModel.pauseRecording()
        }
        viewLifecycleOwner.lifecycleScope.launch {
            sensorRecordingViewModel.rpmLiveData.setIsRecording(false)
        }
        Toast.makeText(context, "Recording Paused", Toast.LENGTH_SHORT).show()

    }

    private fun resumeRecording() {
        //recordingViewModel.resumeRecording()
        viewLifecycleOwner.lifecycleScope.launch {
            sensorRecordingViewModel.recordViewModel.resumeRecording()
        }
        viewLifecycleOwner.lifecycleScope.launch {
            sensorRecordingViewModel.rpmLiveData.setIsRecording(true)

        }
        Toast.makeText(context, "Recording Resumed", Toast.LENGTH_SHORT).show()

    }

    private fun deleteRecording() {
        //recordingViewModel.stopRecording()
        viewLifecycleOwner.lifecycleScope.launch {
            sensorRecordingViewModel.recordViewModel.stopRecording()
        }
        viewLifecycleOwner.lifecycleScope.launch {
            sensorRecordingViewModel.rpmLiveData.setIsRecording(false)

        }
        Toast.makeText(context, "Recording Deleted", Toast.LENGTH_SHORT).show()

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