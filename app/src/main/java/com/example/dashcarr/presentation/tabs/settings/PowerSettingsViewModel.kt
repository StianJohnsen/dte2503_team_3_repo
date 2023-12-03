package com.example.dashcarr.presentation.tabs.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dashcarr.data.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the PowerSettingsFragment.
 *
 * @property userPreferencesRepository
 * @constructor Create empty Power settings view model
 */
@HiltViewModel
class PowerSettingsViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    fun setPowerSetting(powerSettingInt: Int) {

        viewModelScope.launch {
            userPreferencesRepository.updateIsSaveModeOn(powerSettingInt)
        }

        PowerSavingMode.setAppPowerMode(PowerSavingMode.PowerState.values()[powerSettingInt])
    }

    fun getLostBatteryPercentages(currentCapacity: Int): Int {
        return PowerSavingMode.getInitialBatteryCapacity() - currentCapacity
    }
}
