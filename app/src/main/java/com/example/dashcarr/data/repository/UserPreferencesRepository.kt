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

/**
 * Repository class for managing user preferences using DataStore.
 *
 * @property dataStore The DataStore instance for storing preferences.
 */
class UserPreferencesRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    /**
     * Flow of LoggedInAndPowerModeValue which represents the user's login status and power save mode state.
     * It catches IOExceptions during data read and provides default values in case of an error.
     */
    val userPreferenceFlow: Flow<UserPreferenceDataClass> = dataStore.data
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
            val cameraDuration =
                preferences[DataStoreKey.CAMERA_DURATION] ?: 5
            UserPreferenceDataClass(alreadyLoggedIn, isPowerSaveModeOn, cameraDuration)
        }

    /**
     * Updates the user's login status in the preferences.
     * @param newValue The new value to set for the user's login status.
     */
    suspend fun updateAlreadyLoggedIn(newValue: Boolean) {
        dataStore.edit { preferences ->
            preferences[DataStoreKey.ALREADY_LOGGED_IN] = newValue
        }
    }

    /**
     * Updates the power save mode state in the preferences.
     * @param newValue The new value to set for the power save mode state.
     */
    suspend fun updateIsSaveModeOn(newValue: Int) {
        dataStore.edit { preferences ->
            preferences[DataStoreKey.IS_POWER_SAVE_MODE_ON] = newValue
        }
    }

    suspend fun updateCameraDuration(newValue: Int) {
        dataStore.edit { preferences ->
            preferences[DataStoreKey.CAMERA_DURATION] = newValue
        }
    }
}