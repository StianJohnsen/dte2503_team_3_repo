package com.example.dashcarr.data.repository


import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey


data class LoggedInAndPowerModeValue(
    val alreadyLoggedIn: Boolean,
    val isPowerSaveModeOn: Int
)


object DataStoreKey {
    val ALREADY_LOGGED_IN = booleanPreferencesKey("already_logged_in")
    val IS_POWER_SAVE_MODE_ON = intPreferencesKey("is_power_save_mode_on")
}
