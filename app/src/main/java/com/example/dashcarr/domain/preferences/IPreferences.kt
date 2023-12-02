package com.example.dashcarr.domain.preferences

import android.location.Location
import org.osmdroid.util.GeoPoint

/**
 * Interface for managing various user preferences and local data change timestamps.
 * Provides functionality to get and set various preferences and timestamps related to different entities.
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

    fun deleteAllLocalChangeTimeStamps()
}