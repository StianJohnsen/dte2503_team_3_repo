package com.example.dashcarr.presentation.tabs.map.OSM

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException
import org.json.JSONObject
import java.time.Duration
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import kotlin.math.roundToInt

class OSMFetcher(
    context: Context,
    locationManager: LocationManager
) : LocationListener {
    companion object {
        private const val MIN_DISTANCE_CHANGE_FOR_UPDATES = 1F
        private const val MIN_TIME_BETWEEN_UPDATES = 1000L

        @Volatile
        private var instance: OSMFetcher? = null
        fun initFetcher(
            context: Context,
            locationManager: LocationManager
        ) {
            synchronized(this) {
                if (instance == null) {
                    instance = OSMFetcher(context, locationManager)
                }
            }
        }

        fun getInstance() = instance
    }

    private val queue = Volley.newRequestQueue(context)
    private val listeners = emptyList<MapPositionChangedListener>().toMutableList()
    private var lastLocation: Location? = null
    private var lastRequestTime = LocalDateTime.now().minusHours(1)

    private data class Buffer(var speedLimit: Int?, var speed: Float?, var streetName: String?)

    private val cache: Buffer = Buffer(null, null, null)

    val sharedPref = context.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
    var isMphSelected = sharedPref.getBoolean("DisplayInMph", false)

    init {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
            || ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                MIN_TIME_BETWEEN_UPDATES,
                MIN_DISTANCE_CHANGE_FOR_UPDATES,
                this
            )
        } else {
            Log.e(this::class.simpleName, "Location permissions are needed for location updates")
        }
    }

    fun toggleSpeedUnit(context: Context) {
        isMphSelected = !isMphSelected

        // Save preference in SharedPreferences
        val sharedPref = context.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putBoolean("DisplayInMph", isMphSelected)
            apply()
        }
        listeners.forEach { it.onUnitChanged(isMphSelected = isMphSelected) }
    }

    fun addMapPositionChangedListener(lifecycle: Lifecycle, listener: MapPositionChangedListener) {
        listeners.add(listener)
        Log.e("testata", "add call ${listeners.size}")
        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onDestroy(owner: LifecycleOwner) {
                listeners.remove(listener)
                Log.e("testata", "remove Destroy ${listeners.size}")
            }

            override fun onPause(owner: LifecycleOwner) {
                listeners.remove(listener)
                Log.e("testata", "remove Pause ${listeners.size}")
            }
        })
        listener.onSpeedLimitChanged(cache.speedLimit)
        if (cache.speed != null) {
            listener.onSpeedChanged(cache.speed!!)
        }
        if (cache.streetName != null) {
            listener.onStreetChangedListener(cache.streetName!!)
        }
        listener.onUnitChanged(isMphSelected)
    }

    private fun convertUnit(speed: Float): Float = speed * if (isMphSelected) 2.237F else 3.6F

    private fun fetchSpeedLimit(wayId: Int) {
        val speedLimitUrl = "https://overpass-api.de/api/interpreter?data=[out:json];way($wayId);out;"
        val request = JsonObjectRequest(
            Request.Method.GET, speedLimitUrl, null,
            { addressResponse ->
                try {
                    val node = addressResponse.getJSONArray("elements").get(0) as JSONObject
                    var speedLimit = node.getJSONObject("tags").getInt("maxspeed")
                    speedLimit = convertUnit(speedLimit.toFloat()).roundToInt()
                    listeners.forEach {
                        it.onSpeedLimitChanged(speedLimit)
                    }
                    cache.speedLimit = speedLimit
                    Log.d(this::class.simpleName, "max speed: $speedLimit")
                } catch (e: JSONException) {
                    listeners.forEach {
                        it.onSpeedLimitChanged(null)
                    }
                    cache.speedLimit = null
                    Log.d(
                        this::class.simpleName,
                        "No Speed limit available for way id: $wayId, exception: ${e.stackTraceToString()}"
                    )
                }
            },
            { addressError ->
                Log.e(this::class.simpleName, "Can't access Url: $speedLimitUrl with error $addressError")
            }
        )
        queue.add(request)
    }

    /**
     * Sends a new API Request to the nominatim server, to resolve the provided coordinates into street names.
     * The nomination guidelines don't allow more than one request per second, otherwise your IP might get banned.
     *
     * @param location location with GPS coordinates
     */
    private fun fetchStreetName(location: Location) {
        val addressUrl =
            "https://nominatim.openstreetmap.org/reverse?format=jsonv2&lat=${location.latitude}&lon=${location.longitude}&layer=address"
        val addressRequest = JsonObjectRequest(
            Request.Method.GET, addressUrl, null,
            { response ->
                Log.d(this::class.simpleName, "OSM Request successful! Content: $response")
                if (response.getString("osm_type") == "way") {
                    fetchSpeedLimit(response.getInt("osm_id"))
                }
                val address = response.getJSONObject("address")
                var road = ""
                if (address.has("road")) {
                    road = address.get("road").toString()
                }
                val town = when {
                    address.has("town") -> address.get("town")
                    address.has("municipality") -> address.get("municipality")
                    address.has("city") -> address.get("city")
                    address.has("region") -> address.get("region")
                    address.has("neighbourhood") -> address.get("neighbourhood")
                    address.has("farm") -> address.get("farm")
                    address.has("county") -> address.get("county")
                    else -> ""
                }.toString()
                val street = if (road.isEmpty() || town.isEmpty()) {
                    town + road
                } else {
                    "${road}, $town"
                }
                listeners.forEach {
                    it.onStreetChangedListener(street)
                }
                cache.streetName = street
            },
            { error ->
                Log.e(this::class.simpleName, "Can't access Url: $addressUrl with error $error")
                lastLocation = null
            }
        )
        queue.add(addressRequest)
    }

    override fun onLocationChanged(location: Location) {
        if (listeners.isEmpty()) {
            return
        }
        if (Duration.between(lastRequestTime, LocalDateTime.now())
                .get(ChronoUnit.SECONDS) > 2 && (this.lastLocation == null || this.lastLocation!!.distanceTo(location) > 100)
        ) {
            lastLocation = location
            lastRequestTime = LocalDateTime.now()
            fetchStreetName(location)
        }

        val speed = convertUnit(location.speed)
        listeners.forEach {
            it.onSpeedChanged(speed)
        }
        cache.speed = speed
    }
}

interface MapPositionChangedListener {
    fun onStreetChangedListener(streetName: String) {}

    fun onSpeedChanged(speed: Float) {}

    fun onSpeedLimitChanged(speedLimit: Int?) {}

    fun onUnitChanged(isMphSelected: Boolean) {}
}