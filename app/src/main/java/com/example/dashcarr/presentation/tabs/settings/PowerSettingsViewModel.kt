package com.example.dashcarr.presentation.tabs.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dashcarr.presentation.tabs.map.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

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
