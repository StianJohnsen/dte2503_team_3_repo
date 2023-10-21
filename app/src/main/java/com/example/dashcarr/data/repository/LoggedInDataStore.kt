package com.example.dashcarr.data.repository


import androidx.datastore.preferences.core.booleanPreferencesKey



data class LoggedInValue (
    val alreadyLoggedIn: Boolean,
    val isPowerSaveModeOn: Boolean
)


object DataStoreKey{
    val ALREADY_LOGGED_IN = booleanPreferencesKey("already_logged_in")
    val IS_POWER_SAVE_MODE_ON = booleanPreferencesKey("is_power_save_mode_on")
}
