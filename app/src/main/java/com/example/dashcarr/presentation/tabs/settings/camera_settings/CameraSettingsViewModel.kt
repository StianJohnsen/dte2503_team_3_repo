package com.example.dashcarr.presentation.tabs.settings.camera_settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.dashcarr.data.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CameraSettingsViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {
    val userPreferencesLiveData = userPreferencesRepository.userPreferenceFlow.asLiveData()

    fun updateDuration(newValue: Int) {
        viewModelScope.launch {
            userPreferencesRepository.updateCameraDuration(newValue)
        }
    }
}