package com.example.dashcarr.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import com.example.dashcarr.presentation.tabs.settings.PowerSavingMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject

class UserPreferencesRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    val appBoolFlow: Flow<LoggedInAndPowerModeValue> = dataStore.data
        .catch {
            if (it is IOException) {
                it.printStackTrace()
                emit(emptyPreferences())
            } else {
                throw it
            }
        }.map { preferences ->
            val alreadyLoggedIn = preferences[DataStoreKey.ALREADY_LOGGED_IN] ?: false
            val isPowerSaveModeOn =
                preferences[DataStoreKey.IS_POWER_SAVE_MODE_ON] ?: PowerSavingMode.PowerState.AUTO.ordinal
            LoggedInAndPowerModeValue(alreadyLoggedIn, isPowerSaveModeOn)
        }

    suspend fun updateAlreadyLoggedIn(newValue: Boolean) {
        dataStore.edit { preferences ->
            preferences[DataStoreKey.ALREADY_LOGGED_IN] = newValue
        }
    }

    suspend fun updateIsSaveModeOn(newValue: Int) {
        dataStore.edit { preferences ->
            preferences[DataStoreKey.IS_POWER_SAVE_MODE_ON] = newValue
        }
    }
}