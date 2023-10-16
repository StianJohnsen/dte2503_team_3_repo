package com.example.dashcarr.data.repository


import androidx.datastore.preferences.core.booleanPreferencesKey



data class LoggedInValue (
    val alreadyLoggedIn: Boolean
)


object DataStoreKey{
    val ALREADY_LOGGED_IN = booleanPreferencesKey("already_logged_in")
}
