package com.example.dashcarr.data.datasource

import android.content.Context
import android.location.Location
import com.example.dashcarr.domain.preferences.IPreferences
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import org.osmdroid.util.GeoPoint
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of [IPreferences] for managing application preferences.
 *
 * This class utilizes Android's SharedPreferences to store and retrieve user preferences,
 * including the last known user location.
 *
 * @property context The application context used to access SharedPreferences.
 */
@Singleton
class Preferences @Inject constructor(
    @ApplicationContext context: Context
): IPreferences {

    private val preferences = context.getSharedPreferences(
        SHARED_PREFERENCES_NAME,
        Context.MODE_PRIVATE
    )

    private val gson = Gson()

    override fun getLastUserLocation(): GeoPoint? {
        preferences.getString(KEY_USER_LAST_LOCATION, null)?.let {
            return gson.fromJson(it, GeoPoint::class.java)
        }
        return null
    }


    override fun saveLastUserLocation(location: Location) {
        preferences.edit()
            .putString(KEY_USER_LAST_LOCATION, gson.toJson(GeoPoint(location)))
            .apply()
    }

    override fun getLastLocalFriendsTableChangesTimestamp(): Long =
        preferences.getLong(KEY_LAST_LOCAL_FRIENDS_TABLE_CHANGES_TIMESTAMP, 0)

    override fun saveLastLocalFriendsTableChangesTimestamp(timeStamp: Long) {
        preferences.edit()
            .putLong(KEY_LAST_LOCAL_FRIENDS_TABLE_CHANGES_TIMESTAMP, timeStamp)
            .apply()
    }

    override fun getLastLocalMessagesTableChangesTimestamp(): Long =
        preferences.getLong(KEY_LAST_LOCAL_MESSAGES_TABLE_CHANGES_TIMESTAMP, 0)

    override fun saveLastLocalMessagesTableChangesTimestamp(timeStamp: Long) {
        preferences.edit()
            .putLong(KEY_LAST_LOCAL_MESSAGES_TABLE_CHANGES_TIMESTAMP, timeStamp)
            .apply()
    }

    override fun getLastLocalPointOfInterestTableChangesTimestamp(): Long =
        preferences.getLong(KEY_LAST_LOCAL_POINT_OF_INTEREST_TABLE_CHANGES_TIMESTAMP, 0)

    override fun saveLastLocalPointOfInterestTableChangesTimestamp(timeStamp: Long) {
        preferences.edit()
            .putLong(KEY_LAST_LOCAL_POINT_OF_INTEREST_TABLE_CHANGES_TIMESTAMP, timeStamp)
            .apply()
    }

    override fun getLastLocalSentMessagesTableChangesTimestamp(): Long =
        preferences.getLong(KEY_LAST_LOCAL_SENT_MESSAGES_TABLE_CHANGES_TIMESTAMP, 0)

    override fun saveLastLocalSentMessagesTableChangesTimestamp(timeStamp: Long) {
        preferences.edit()
            .putLong(KEY_LAST_LOCAL_SENT_MESSAGES_TABLE_CHANGES_TIMESTAMP, timeStamp)
            .apply()
    }

    companion object {
        const val SHARED_PREFERENCES_NAME = "DASH_CARR_PREFERENCES"
        const val KEY_USER_LAST_LOCATION = "USER_LAST_LOCATION"
        const val KEY_LAST_LOCAL_FRIENDS_TABLE_CHANGES_TIMESTAMP = "LAST_LOCAL_FRIENDS_TABLE_CHANGES_TIMESTAMP"
        const val KEY_LAST_LOCAL_MESSAGES_TABLE_CHANGES_TIMESTAMP = "LAST_LOCAL_MESSAGES_TABLE_CHANGES_TIMESTAMP"
        const val KEY_LAST_LOCAL_POINT_OF_INTEREST_TABLE_CHANGES_TIMESTAMP = "LAST_LOCAL_POINT_OF_INTEREST_TABLE_CHANGES_TIMESTAMP"
        const val KEY_LAST_LOCAL_SENT_MESSAGES_TABLE_CHANGES_TIMESTAMP = "LAST_LOCAL_SENT_MESSAGES_TABLE_CHANGES_TIMESTAMP"
    }
}