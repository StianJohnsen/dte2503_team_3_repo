package com.example.dashcarr.extensions

import android.Manifest

/**
 * Array of location-related permissions required for accessing fine and coarse location services.
 *
 * The permissions included are [Manifest.permission.ACCESS_FINE_LOCATION] and
 * [Manifest.permission.ACCESS_COARSE_LOCATION].
 */
val locationPermissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)