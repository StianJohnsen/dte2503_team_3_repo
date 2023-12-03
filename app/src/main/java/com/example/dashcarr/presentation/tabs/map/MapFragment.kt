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
import android.net.Uri
import android.os.BatteryManager
import android.os.Bundle
import android.os.PowerManager
import android.preference.PreferenceManager
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
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
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import android.content.res.Configuration as Configuration1


/**
 * Fragment representing the map screen in the DashCarr application.
 * Handles location updates, map interaction, recording management, and displays a dynamic map.
 *
 * Manages functionalities related to map interaction, such as creating markers, displaying routes,
 * and integrating with other features like weather data and battery status. It also handles sensor
 * recording for trips and provides navigation options for other features in the app.
 *
 * @property mapViewModel ViewModel associated with map functionalities.
 * @property sensorRecordingViewModel ViewModel for managing sensor recordings.
 */
@AndroidEntryPoint
class MapFragment : BaseFragment<FragmentMapBinding>(
    FragmentMapBinding::inflate, null
), LocationListener, MapEventsReceiver {

    private val mapViewModel: MapViewModel by viewModels()

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
            OSMFetcher.initFetcher(requireContext(), locationManager)
            if (isRideActivated) {
                addMapPositionChangedListener()
            }
        }
    }

    private val isRideActivated by lazy {
        val args: MapFragmentArgs by navArgs()
        args.isRideActivated
    }

    private var isRecordingLocation = false

    private var isRecording = false

    private var isPaused = false


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
                binding.mapView.overlays.add(pointOfInterest.toMarker(binding.mapView, requireContext()))
            }
        }
        mapViewModel.showButtonsBarState.collectWithLifecycle(viewLifecycleOwner) { show ->
            show?.let {
                binding.llExpandedBar.setHeightSmooth(newHeight = if (show) -2 else 0)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            sensorRecordingViewModel.isRecording.collect {
                isRecording = it
                isRecordingLocation = it
            }

        }

        viewLifecycleOwner.lifecycleScope.launch {
            sensorRecordingViewModel.isPaused.collect {
                isPaused = it
            }
        }


        viewLifecycleOwner.lifecycleScope.launch {
            sensorRecordingViewModel.sensorRecording.isBtnStopShowing.collect {
                binding.btnStop.visibility = it
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            sensorRecordingViewModel.sensorRecording.isBtnPauseShowing.collect {
                binding.btnPause.visibility = it
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            sensorRecordingViewModel.sensorRecording.isBtnResumeShowing.collect {
                binding.btnResume.visibility = it

            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            sensorRecordingViewModel.sensorRecording.isBtnDeleteShowing.collect {
                binding.btnDelete.visibility = it
            }
        }
    }

    /**
     * Initializes listeners for UI elements in the fragment.
     */
    private fun initListeners() {
        var originalLeftMargin: Int? = null
        binding.btnShowHideBar.setOnClickListener {
            val isLandscape = resources.configuration.orientation
            if (isLandscape == 2) {
                mapViewModel.showHideBar()

                if (originalLeftMargin == null) {
                    originalLeftMargin =
                        (binding.llTrafficLight.layoutParams as ViewGroup.MarginLayoutParams).leftMargin
                }

                val layoutParams = binding.llTrafficLight.layoutParams as ViewGroup.MarginLayoutParams

                if (layoutParams.leftMargin == 320) {
                    layoutParams.leftMargin = originalLeftMargin ?: 0
                } else {
                    layoutParams.leftMargin = 320
                }

                binding.llTrafficLight.layoutParams = layoutParams
            } else {
                mapViewModel.showHideBar()
            }
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

        binding.btnEmergencyCall?.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:112")
            }
            startActivity(intent)
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
        showBottomNavigation(!isRideActivated)
        initListeners()
        observeViewModel()
        initMap()
        requestLocationPermission()
        setupMapView()

        if (PowerSavingMode.getPowerMode() && isRideActivated) {
            binding.llTrafficLight.visibility = View.VISIBLE
            binding.llBattery.visibility = View.VISIBLE
            setupBatteryStatus()
        } else {
            binding.llTrafficLight.visibility = View.GONE
            binding.llBattery.visibility = View.GONE
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

        if (isRideActivated) {
            if (binding.hudView.getFragment<HudFragment?>() == null) {
                childFragmentManager.beginTransaction()
                    .add(binding.hudView.id, HudFragment())
                    .commit()
            }


            binding.apply {
                // sets visibility and functionality for recording buttons
                btnStop.setOnClickListener {
                    stopRecording()
                    findNavController().navigate(R.id.action_action_map_to_action_history)
                }
                btnPause.setOnClickListener {
                    pauseRecording()
                }
                btnResume.setOnClickListener {
                    resumeRecording()
                }

                btnDelete.setOnClickListener {
                    deleteRecording()
                    findNavController().navigate(R.id.action_action_map_to_action_history)
                }

                btnDashcam.setOnClickListener {
                    if (!DashcamFragment.exists()) {
                        val transaction = childFragmentManager.beginTransaction()
                        transaction.add(R.id.dashcam_view, DashcamFragment.getInstance())
                        transaction.disallowAddToBackStack()
                        transaction.commit()
                    } else {
                        DashcamFragment.getInstance().deactivate {}
                    }
                }
                btnOutOfCarMode.setOnClickListener {
                    findNavController().navigate(R.id.action_action_map_to_action_security_camera)
                }
                llWeather.visibility = View.VISIBLE
                val destinationChangedListener =
                    NavController.OnDestinationChangedListener { _, destination, args ->
                        if (destination.id != R.id.action_map && DashcamFragment.exists()) {
                            DashcamFragment.getInstance().deactivate()
                        }
                    }
                findNavController().addOnDestinationChangedListener(destinationChangedListener)
            }

            if (!isRecording && !isPaused) {
                startRecording()
            }

        } else {
            binding.llSideButtons.visibility = View.GONE
            binding.llRecordingButtons.visibility = View.GONE
            binding.llWeather.visibility = View.GONE
        }
    }


    /**
     * Requests location permissions using the AndroidX Activity Result API.
     */
    private fun requestLocationPermission() {
        requestLocationPermissionsLauncher.launch(locationPermissions)
    }

    private fun setupMapView() {
        val sharedPrefs = requireActivity().getSharedPreferences("MapPrefs", Context.MODE_PRIVATE)
        val tileNameResId = sharedPrefs.getInt("current_tile_name_res_id", R.string.mapnik)

        var tileName = getString(tileNameResId)
        tileName = getTileSourceFromName(tileName)

        val nightModeFlags = context?.resources?.configuration?.uiMode?.and(Configuration1.UI_MODE_NIGHT_MASK)
        val isDarkModeEnabled = nightModeFlags == Configuration1.UI_MODE_NIGHT_YES
        if (tileName == "MAPNIK" && isDarkModeEnabled) {
            binding.mapView.getOverlayManager().getTilesOverlay().setColorFilter(mapViewModel.getFilter())
        } else {
            when (tileName) {
                "MAPNIK" -> {
                    binding.mapView.setTileSource(TileSourceFactory.MAPNIK)
                }

                "USGS_SAT" -> {
                    binding.mapView.setTileSource(TileSourceFactory.USGS_SAT)
                }

                else -> {
                    binding.mapView.setTileSource(TileSourceFactory.MAPNIK)
                }
            }
        }
    }

    private fun getTileSourceFromName(tileName: String): String {
        return when (tileName) {
            getString(R.string.mapnik) -> "MAPNIK"
            getString(R.string.usgs_sat) -> "USGS_SAT"
            else -> "DEFAULT_TILE_SOURCE"
        }
    }

    override fun onResume() {
        super.onResume()
        sensorRecordingViewModel.sensorRecording.registerSensors()
    }

    override fun onPause() {
        super.onPause()
        sensorRecordingViewModel.sensorRecording.unregisterSensors()
    }

    /**
     * Stops listening for sensorChanges
     */
    private fun startRecording() {

        sensorRecordingViewModel.startRecording()

        Toast.makeText(requireContext(), "Recording Started", Toast.LENGTH_SHORT).show()
    }

    private fun stopRecording() {

        sensorRecordingViewModel.stopRecording(requireContext())

        /*
                viewLifecycleOwner.lifecycleScope.launch {
            sensorRecordingViewModel.isRecording.collect {
                isRecording = it
                isRecordingLocation = it
            }
        }
         */

        Toast.makeText(requireContext(), "Recording Stopped", Toast.LENGTH_SHORT).show()
    }

    private fun pauseRecording() {
        sensorRecordingViewModel.pauseRecording()
        Toast.makeText(requireContext(), "Recording Paused", Toast.LENGTH_SHORT).show()
    }

    private fun resumeRecording() {
        sensorRecordingViewModel.resumeRecording()
        Toast.makeText(requireContext(), "Recording Resumed", Toast.LENGTH_SHORT).show()
    }

    private fun deleteRecording() {
        sensorRecordingViewModel.deleteRecording()
        Toast.makeText(requireContext(), "Recording Deleted", Toast.LENGTH_SHORT).show()
    }

    private fun addMapPositionChangedListener() {
        OSMFetcher.getInstance()?.addMapPositionChangedListener(lifecycle, object : MapPositionChangedListener {
            override fun onSpeedLimitChanged(speedLimit: Int?) {
                if (speedLimit != null) {
                    binding.speedLimit.text = speedLimit.toString()
                    binding.speedLimit.visibility = View.VISIBLE
                    binding.trafficSign.visibility = View.VISIBLE
                    binding.speedLimit.layoutParams = binding.speedLimit.layoutParams
                } else {
                    binding.speedLimit.visibility = View.GONE
                    binding.trafficSign.visibility = View.GONE
                }
            }

            override fun onUnitChanged(speedUnit: OSMFetcher.SpeedUnit) {
                when (speedUnit) {
                    OSMFetcher.SpeedUnit.MILES_PER_HOUR -> {
                        binding.trafficSign.setImageDrawable(
                            ContextCompat.getDrawable(
                                requireContext(),
                                R.drawable.maximum_speed_usa
                            )!!
                        )
                        binding.speedLimit.layoutParams =
                            (binding.speedLimit.layoutParams as ViewGroup.MarginLayoutParams).also {
                                it.topMargin = 50
                            }
                    }

                    OSMFetcher.SpeedUnit.KILOMETERS_PER_HOUR -> {
                        binding.trafficSign.setImageDrawable(
                            ContextCompat.getDrawable(
                                requireContext(),
                                R.drawable.maximum_speed_europe
                            )!!
                        )
                        binding.speedLimit.layoutParams =
                            (binding.speedLimit.layoutParams as ViewGroup.MarginLayoutParams).also {
                                it.topMargin = 0
                            }
                    }

                    else -> {}
                }
            }
        })
    }

    /**
     * Initializes the map settings and overlays.
     */
    private fun initMap() {
        Configuration.getInstance()
            .load(requireContext(), PreferenceManager.getDefaultSharedPreferences(requireContext()))

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
            sensorRecordingViewModel.sensorRecording.insertIntoLocationList(location)
        }

        val latitude = location.latitude
        val longitude = location.longitude

        mapViewModel.getWeatherData(latitude, longitude) { response ->
            handleWeatherResponse(response)
        }
    }

    private fun handleWeatherResponse(response: String) {
        val (iconResourceId, weatherText) = when {
            response.contains("rain") -> Pair(R.drawable.ic_rain, "Rainy")
            response.contains("clear") -> Pair(R.drawable.ic_sun, "Sunny")
            response.contains("fair") -> Pair(R.drawable.ic_sun, "Fairy")
            response.contains("cloud") -> Pair(R.drawable.ic_cloud, "Cloudy")
            response.contains("snow") -> Pair(R.drawable.ic_snow, "Snowy")
            response.contains("sleet") -> Pair(R.drawable.ic_snow, "Sleety")
            response.contains("thunder") -> Pair(R.drawable.ic_thunder, "Thundery")
            else -> Pair(R.drawable.ic_cancel_small, "No Data")
        }
        if (isAdded) {
            binding.txtWeather.text = weatherText
            binding.iconWeather.setImageResource(iconResourceId)
        }
    }

    override fun onStop() {
        mapViewModel.saveLastKnownLocation()
        super.onStop()
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
                val batteryPct: Float = level * 100 / (scale.toFloat())
                val batteryInt: Int = batteryPct.toInt()
                val batteryString = "$batteryInt%"

                binding.batteryPercentage.text = batteryString
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