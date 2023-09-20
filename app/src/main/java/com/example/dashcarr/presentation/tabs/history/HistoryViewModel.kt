package com.example.dashcarr.presentation.tabs.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dashcarr.domain.repository.IFirebaseDBRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val firebaseDBRepository: IFirebaseDBRepository
): ViewModel() {

    fun saveTest(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            firebaseDBRepository.saveAverageSpeed(10)
        }
    }
}