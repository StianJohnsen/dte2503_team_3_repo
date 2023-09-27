package com.example.dashcarr.presentation.tabs.history

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dashcarr.data.entity.GeoPointEntity
import com.example.dashcarr.domain.repository.IFirebaseDBRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val firebaseDBRepository: IFirebaseDBRepository
): ViewModel() {

    fun saveTest(geoPoint: GeoPointEntity) {
        viewModelScope.launch {
            val result = firebaseDBRepository.saveGeoPoint(geoPoint)
        }
    }

    fun getGeoPoints() {
        viewModelScope.launch {
            val result = firebaseDBRepository.getAllGeoPoints()
            Log.e("WatchingSomeStuff", "We got result in view model result = $result")
        }
    }
}