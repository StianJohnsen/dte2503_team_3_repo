package com.example.dashcarr.data.repository

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey

/**
 * Data class representing the state of the user's login status and power save mode.
 *
 * @property alreadyLoggedIn Boolean indicating whether the user is already logged in.
 * @property isPowerSaveModeOn Integer representing the state of power save mode.
 */
data class LoggedInAndPowerModeValue(
    val alreadyLoggedIn: Boolean,
    val isPowerSaveModeOn: Int
)

/**
 * Singleton object containing keys for data store preferences.
 */
object DataStoreKey {
    val ALREADY_LOGGED_IN = booleanPreferencesKey("already_logged_in")
    val IS_POWER_SAVE_MODE_ON = intPreferencesKey("is_power_save_mode_on")
}