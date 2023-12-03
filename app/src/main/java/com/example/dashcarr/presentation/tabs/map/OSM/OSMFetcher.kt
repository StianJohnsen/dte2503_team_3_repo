package com.example.dashcarr.presentation.tabs.map.OSM

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.util.Log
import androidx.core.app.ActivityCompat
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException
import org.json.JSONObject
import java.time.Duration
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

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

        fun getInstance() = instance!!
    }

    private val queue = Volley.newRequestQueue(context)
    private val listeners = emptyList<MapPositionChangedListener>().toMutableList()
    private var lastLocation: Location? = null
    private var lastRequestTime = LocalDateTime.now().minusHours(1)

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

    fun addMapPositionChangedListener(listener: MapPositionChangedListener) {
        listeners.add(listener)
    }

    private fun fetchSpeedLimit(wayId: Int) {
        val speedLimitUrl = "https://overpass-api.de/api/interpreter?data=[out:json];way($wayId);out;"
        val request = JsonObjectRequest(
            Request.Method.GET, speedLimitUrl, null,
            { addressResponse ->
                try {
                    val node = addressResponse.getJSONArray("elements").get(0) as JSONObject
                    val speedLimit = node.getJSONObject("tags").getInt("maxspeed")
                    listeners.forEach {
                        it.onSpeedLimitChanged(speedLimit)
                    }
                    Log.d(this::class.simpleName, "max speed: $speedLimit")
                } catch (e: JSONException) {
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
            },
            { error ->
                Log.e(this::class.simpleName, "Can't access Url: $addressUrl with error $error")
                lastLocation = null
            }
        )
        queue.add(addressRequest)
    }

    override fun onLocationChanged(location: Location) {
        if (Duration.between(lastRequestTime, LocalDateTime.now())
                .get(ChronoUnit.SECONDS) > 2 && (this.lastLocation == null || this.lastLocation!!.distanceTo(location) > 100)
        ) {
            lastLocation = location
            lastRequestTime = LocalDateTime.now()
            fetchStreetName(location)
        }

        listeners.forEach {
            it.onSpeedChanged(location.speed)
        }
    }
}

interface MapPositionChangedListener {
    fun onStreetChangedListener(streetName: String)

    fun onSpeedChanged(speed: Float)

    fun onSpeedLimitChanged(speedLimit: Int)
}