package com.example.dashcarr.presentation.tabs.history

import androidx.lifecycle.ViewModel
import com.example.dashcarr.domain.repository.IFirebaseDBRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    //private val firebaseDBRepository: IFirebaseDBRepository
): ViewModel() {

    /*fun saveGeoPointEntity(geoPoint: GeoPointEntity) {
        viewModelScope.launch {
            val result = firebaseDBRepository.saveGeoPoint(geoPoint)
            Log.e("WatchingSomeStuff", "We save result in view model result = $result")
        }
    }

    fun getGeoPoints() {
        viewModelScope.launch {
            val result = firebaseDBRepository.getAllGeoPoints()
            Log.e("WatchingSomeStuff", "We got result in view model result = $result")
        }
    }*/
}