package com.example.dashcarr.data.repository.model

abstract class GeoPointTable {
    companion object {
        const val GEO_POINT_DOCUMENT = "GeoPointDocument"
        const val GEO_POINT_COLLECTION = "GeoPointCollection"

        const val GEO_POINT_ID_KEY = "geoPointId"
        const val TRIP_ID_KEY = "tripId"
        const val LATITUDE_KEY = "latitude"
        const val LONGITUDE_KEY = "longitude"
        const val STEP_NUM_KEY = "stepNum"
    }
}