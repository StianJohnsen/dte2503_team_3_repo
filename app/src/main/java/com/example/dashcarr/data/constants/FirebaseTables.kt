package com.example.dashcarr.data.constants

/**
 * Constants and keys for the GeoPoint data table.
 */
abstract class FirebaseTables {
    companion object {

        //GeoPoint
        const val GEO_POINT_DOCUMENT = "GeoPointDocument"
        const val GEO_POINT_COLLECTION = "GeoPointCollection"
        const val GEO_POINT_ID_KEY = "geoPointId"
        const val TRIP_ID_KEY = "tripId"
        const val LATITUDE_KEY = "latitude"
        const val LONGITUDE_KEY = "longitude"
        const val STEP_NUM_KEY = "stepNum"

        //Friend

        const val FRIENDS_DOCUMENT = "FriendsDocument"
        const val FRIENDS_COLLECTION = "FriendsCollection"
        const val FRIEND_NAME_KEY = "name"
        const val FRIEND_PHONE_KEY = "phone"
        const val FRIEND_EMAIL_KEY = "email"
        const val FRIEND_CREATED_TIMESTAMP_KEY = "createdTimeStamp"

        //Last changes timestamps
        const val LAST_CHANGES_DOCUMENT = "LastChangesDocument"
        const val LAST_CHANGES_COLLECTION = "LastChangesCollection"
        const val LAST_FRIENDS_CHANGES_TIMESTAMP_KEY = "lastFriendsChangesTimestamp"
    }
}