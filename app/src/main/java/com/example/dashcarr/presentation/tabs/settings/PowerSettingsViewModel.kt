package com.example.dashcarr.presentation.tabs.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.dashcarr.presentation.tabs.map.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PowerSettingsViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
    ): ViewModel(){

     val appPreferencesPower = userPreferencesRepository.appBoolFlow.asLiveData()

    fun setPowerSetting(boolVal: Boolean){
        viewModelScope.launch {
            userPreferencesRepository.updateIsSaveModeOn(boolVal)
        }
    }



}
