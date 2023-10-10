package com.example.dashcarr.domain.preferences

import android.location.Location
import org.osmdroid.util.GeoPoint

interface IPreferences {

    fun getLastUserLocation(): GeoPoint?
    fun saveLastUserLocation(location: Location)
}