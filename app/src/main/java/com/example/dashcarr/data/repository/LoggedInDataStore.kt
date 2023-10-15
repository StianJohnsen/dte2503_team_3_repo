package com.example.dashcarr.data.repository

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.internal.managers.ApplicationComponentManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException


data class LoggedInValue (
    val alreadyLoggedIn: Boolean
)


object DataStoreKey{
    val ALREADY_LOGGED_IN = booleanPreferencesKey("already_logged_in")
}


class LoggedInDataStore(private val dataStore: DataStore<Preferences>){
    val appBoolFlow: Flow<LoggedInValue> = dataStore.data
        .catch {
            if (it is IOException){
                it.printStackTrace()
                emit(emptyPreferences())
            }else{
                throw it
            }
        }.map { preferences ->
            val alreadyLoggedIn = preferences[DataStoreKey.ALREADY_LOGGED_IN] ?: false
            LoggedInValue(alreadyLoggedIn)
        }

    suspend fun updateAlreadyLoggedIn(newValue: Boolean){
        dataStore.edit { preferences ->
            preferences[DataStoreKey.ALREADY_LOGGED_IN] = newValue
        }
    }
}