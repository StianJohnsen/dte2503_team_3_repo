package com.example.dashcarr.presentation.tabs.map

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.preference.PreferenceManager
import com.example.dashcarr.databinding.FragmentMapBinding
import com.example.dashcarr.extensions.collectWithLifecycle
import com.example.dashcarr.extensions.locationPermissions
import com.example.dashcarr.extensions.setHeightSmooth
import com.example.dashcarr.extensions.toastThrowableShort
import com.example.dashcarr.presentation.core.BaseFragment
import com.example.dashcarr.presentation.mapper.toMarker
import com.example.dashcarr.presentation.tabs.map.data.PointOfInterest
import dagger.hilt.android.AndroidEntryPoint
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay


@AndroidEntryPoint
class MapFragment : BaseFragment<FragmentMapBinding>(
    FragmentMapBinding::inflate,
    showBottomNavBar = true
), LocationListener, MapEventsReceiver {

    private val locationManager by lazy { requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager }

    private val requestLocationPermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionsResult ->
        if (permissionsResult.values.isEmpty()) return@registerForActivityResult
        if (permissionsResult.values.contains(false)) {
            Log.e("MapSomeStuff", "False location!")
        } else {
            Log.e("MapSomeStuff", "Got Location!")
            createLocationRequest()
        }
    }

    private val viewModel: MapViewModel by viewModels()
    override fun observeViewModel() {
        viewModel.lastSavedUserLocation.collectWithLifecycle(viewLifecycleOwner) {
            binding.mapView.controller.animateTo(it)
        }
        viewModel.currentLocation.collectWithLifecycle(viewLifecycleOwner) {
            it?.let {
                binding.mapView.controller.animateTo(GeoPoint(it))
            }
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
    }


    override fun initListeners() {
        binding.btnShowHideBar.setOnClickListener {
            viewModel.showHideBar()
        }

    }

//    override fun onResume() {
//        super.onResume()
//        //this will refresh the osmdroid configuration on resuming.
//        //if you make changes to the configuration, use
//        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
//        //Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
//        map.onResume() //needed for compass, my location overlays, v6.0.0 and up
//    }

//    override fun onPause() {
//        super.onPause()
//        //this will refresh the osmdroid configuration on resuming.
//        //if you make changes to the configuration, use
//        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
//        //Configuration.getInstance().save(this, prefs);
//        map.onPause()  //needed for compass, my location overlays, v6.0.0 and up
//    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListeners()
        observeViewModel()
        initMap()
        requestLocationPermission()
    }

    private fun requestLocationPermission() {
        requestLocationPermissionsLauncher.launch(locationPermissions)
    }

    private fun initMap() {
        Configuration.getInstance().load(requireContext(), PreferenceManager.getDefaultSharedPreferences(requireContext()))
        binding.mapView.setTileSource(TileSourceFactory.MAPNIK)
        binding.mapView.isHorizontalMapRepetitionEnabled = false
        binding.mapView.isVerticalMapRepetitionEnabled = false
        binding.mapView.setScrollableAreaLimitLatitude(MapView.getTileSystem().maxLatitude, MapView.getTileSystem().minLatitude, 0)
        binding.mapView.setScrollableAreaLimitLongitude(MapView.getTileSystem().minLongitude, MapView.getTileSystem().maxLongitude, 0)
        binding.mapView.minZoomLevel = 4.0
        binding.mapView.controller.setZoom(15.0)
        binding.mapView.isTilesScaledToDpi = true
//        binding.mapView.tilesScaleFactor = 3.0f
        binding.mapView.overlays.add(MapEventsOverlay(this))

    }

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
        Log.e("MapSomeStuff", "OnLocChanged")
        Log.e("MapSomeStuff", "location changed! lat = ${location.latitude} , long = ${location.longitude}")
    }

    override fun onStop() {
        viewModel.saveLastKnownLocation()
        super.onStop()
    }

    override fun singleTapConfirmedHelper(geoPoint: GeoPoint?): Boolean {
        return false
    }

    override fun longPressHelper(geoPoint: GeoPoint?): Boolean {
        Log.e("WatchingMapStuff", "Long press geo = $geoPoint")
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
        binding.etMarkerName.doAfterTextChanged {
            //@TODO check marker - send to viewModel - if wrong = show error
        }
        binding.btnSave.setOnClickListener {
            viewModel.saveNewMarker(PointOfInterest(
                latitude = geoPoint.latitude,
                longitude = geoPoint.longitude,
                name = binding.etMarkerName.text.toString()
            ))
        }
        binding.btnCancel.setOnClickListener {
            viewModel.cancelMarkerCreation()
        }
    }

    sealed class CreateMarkerState {
        class CreateMarker(val geoPoint: GeoPoint): CreateMarkerState()
        class OnSaveError(val throwable: Throwable): CreateMarkerState()
        data object HideMarker: CreateMarkerState()

    }

}