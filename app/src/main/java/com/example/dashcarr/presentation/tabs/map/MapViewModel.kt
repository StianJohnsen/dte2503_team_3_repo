package com.example.dashcarr.presentation.tabs.map

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dashcarr.domain.preferences.IPreferences
import com.example.dashcarr.domain.repository.IPointsOfInterestRepository
import com.example.dashcarr.presentation.tabs.map.data.PointOfInterest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val sp: IPreferences,
    private val pointsOfInterestRepository: IPointsOfInterestRepository
): ViewModel() {

    private val _lastSavedUserLocation = Channel<GeoPoint>()
    val lastSavedUserLocation = _lastSavedUserLocation.receiveAsFlow()
    private val _currentLocation = MutableStateFlow<Location?>(null)
    private val _createMarkerState = MutableSharedFlow<MapFragment.CreateMarkerState>()
    val createMarkerState = _createMarkerState.asSharedFlow()
    private val _showButtonsBarState = MutableStateFlow<Boolean?>(null)
    val showButtonsBarState = _showButtonsBarState.asStateFlow()

    val pointsOfInterestState = pointsOfInterestRepository.getAllPointsLiveData()

    init {
        viewModelScope.launch {
            sp.getLastUserLocation()?.let {
                _lastSavedUserLocation.send(it)
            }
        }
    }

    fun saveCurrentLocation(location: Location) {
        viewModelScope.launch {
            _currentLocation.emit(location)
        }
    }

    fun saveLastKnownLocation() {
        _currentLocation.value?.let { sp.saveLastUserLocation(it) }
    }

    fun createMarker(point: GeoPoint) {
        viewModelScope.launch {
            _createMarkerState.emit(MapFragment.CreateMarkerState.CreateMarker(point))
        }
    }

    fun saveNewMarker(pointOfInterest: PointOfInterest) {
        viewModelScope.launch {
            val result = pointsOfInterestRepository.saveNewPoint(pointOfInterest)
            result.onSuccess {
                _createMarkerState.emit(MapFragment.CreateMarkerState.HideMarker)
            }
            result.onFailure {
                _createMarkerState.emit(MapFragment.CreateMarkerState.OnSaveError(it))
            }
        }
    }

    fun cancelMarkerCreation() {
        viewModelScope.launch {
            _createMarkerState.emit(MapFragment.CreateMarkerState.HideMarker)
        }
    }

    fun showHideBar() {
        viewModelScope.launch {
            val currentShowValue = _showButtonsBarState.value
            _showButtonsBarState.emit(if (currentShowValue == null) true else currentShowValue == false)
        }
    }
}