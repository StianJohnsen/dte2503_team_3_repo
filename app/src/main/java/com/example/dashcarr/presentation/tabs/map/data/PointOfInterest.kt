package com.example.dashcarr.presentation.tabs.map.data

/**
 * Data class representing a point of interest on the map.
 *
 * @param latitude The latitude coordinate of the point of interest.
 * @param longitude The longitude coordinate of the point of interest.
 * @param name The name or title associated with the point of interest.
 * @param createdTimeStamp The timestamp indicating when the point of interest was created.
 */
data class PointOfInterest(
    val latitude: Double,
    val longitude: Double,
    val name: String,
    val createdTimeStamp: Long
)