package com.example.dashcarr.data.repository


import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import com.example.dashcarr.presentation.tabs.settings.PowerSavingMode


data class LoggedInValue (
    val alreadyLoggedIn: Boolean,
    val isPowerSaveModeOn: Boolean
)


object DataStoreKey{
    val ALREADY_LOGGED_IN = booleanPreferencesKey("already_logged_in")
    val IS_POWER_SAVE_MODE_ON = booleanPreferencesKey("is_power_save_mode_on")
}
