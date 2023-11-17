package com.example.dashcarr.domain.preferences

import android.location.Location
import org.osmdroid.util.GeoPoint

/**
 * Interface for managing application preferences.
 */
interface IPreferences {

    fun getLastUserLocation(): GeoPoint?
    fun saveLastUserLocation(location: Location)

    fun getLastLocalFriendsTableChangesTimestamp(): Long
    fun saveLastLocalFriendsTableChangesTimestamp(timeStamp: Long)

    fun getLastLocalMessagesTableChangesTimestamp(): Long

    fun saveLastLocalMessagesTableChangesTimestamp(timeStamp: Long)

    fun getLastLocalPointOfInterestTableChangesTimestamp(): Long
    fun saveLastLocalPointOfInterestTableChangesTimestamp(timeStamp: Long)

    fun getLastLocalSentMessagesTableChangesTimestamp(): Long

    fun saveLastLocalSentMessagesTableChangesTimestamp(timeStamp: Long)



}