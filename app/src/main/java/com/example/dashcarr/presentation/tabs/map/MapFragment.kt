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
import androidx.fragment.app.viewModels
import androidx.preference.PreferenceManager
import com.example.dashcarr.databinding.FragmentMapBinding
import com.example.dashcarr.extensions.locationPermissions
import com.example.dashcarr.presentation.core.BaseFragment
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay


class MapFragment : BaseFragment<FragmentMapBinding>(
    FragmentMapBinding::inflate,
    showBottomNavBar = true
), LocationListener {

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

    val gpsLocationListener: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            Log.e("MapSomeStuff", "OnLocationChanged 1 = ${location.latitude}, long = ${location.longitude}")
        }
    }
    private val REQUEST_PERMISSIONS_REQUEST_CODE = 1
    private lateinit var map : MapView

    private val viewModel: MapViewModel by viewModels()
    override fun observeViewModel() {
    }

    override fun initListeners() {
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //handle permissions first, before map is created. not depicted here

        //load/initialize the osmdroid configuration, this can be done
        // This won't work unless you have imported this: org.osmdroid.config.Configuration.*

        //setting this before the layout is inflated is a good idea
        //it 'should' ensure that the map has a writable location for the map cache, even without permissions
        //if no tiles are displayed, you can try overriding the cache path using Configuration.getInstance().setCachePath
        //see also StorageUtils
        //note, the load method also sets the HTTP User Agent to your application's package name, if you abuse osm's
        //tile servers will get you banned based on this string.

//        map = binding.mapView
//        map.setTileSource(TileSourceFactory.MAPNIK)
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

//    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
//        val permissionsToRequest = ArrayList<String>()
//        var i = 0
//        while (i < grantResults.size) {
//            permissionsToRequest.add(permissions[i])
//            i++
//        }
//        if (permissionsToRequest.size > 0) {
//            ActivityCompat.requestPermissions(
//                requireActivity(),
//                permissionsToRequest.toTypedArray(),
//                REQUEST_PERMISSIONS_REQUEST_CODE)
//        }
//    }


    /*private fun requestPermissionsIfNecessary(String[] permissions) {
        val permissionsToRequest = ArrayList<String>();
        permissions.forEach { permission ->
        if (ContextCompat.checkSelfPermission(this, permission)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            permissionsToRequest.add(permission);
        }
    }
        if (permissionsToRequest.size() > 0) {
            ActivityCompat.requestPermissions(
                    this,
                    permissionsToRequest.toArray(new String[0]),
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }*/


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListeners()
        observeViewModel()
        initMap()
        requestLocationPermission()
//        initMap()
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


        //mMapView.getOverlays().add(this.mLocationOverlay)

        //binding.mapView.


        //val llp = LocationListenerProxy(requireContext().getSystemService(LOCATION_SERVICE))
        //llp.startListening(gpsLocationListener, 1, 1f)

    }

    @SuppressLint("MissingPermission")
    fun createLocationRequest() {
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5, 0f, this);
        val overlay = MyLocationNewOverlay(binding.mapView)
        overlay.enableMyLocation()
        binding.mapView.overlays.add(overlay)
    }

    override fun onLocationChanged(location: Location) {
        Log.e("MapSomeStuff", "OnLocChanged")
        Log.e("MapSomeStuff", "location changed! lat = ${location.latitude} , long = ${location.longitude}")
        binding.mapView.controller.animateTo(GeoPoint(location))
    }

}