package com.example.dashcarr.data.entity

/**
 * Represents a geographical point entity with associated information.
 *
 * @property geoPointId The unique identifier for the geographical point.
 * @property tripId The identifier of the associated trip, if applicable.
 * @property latitude The latitude of the geographical point.
 * @property longitude The longitude of the geographical point.
 * @property stepNum The step number or order in the trip sequence.
 */
data class GeoPointEntity(
    val geoPointId: Int? = null,
    val tripId: Int? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val stepNum: Int? = null
)