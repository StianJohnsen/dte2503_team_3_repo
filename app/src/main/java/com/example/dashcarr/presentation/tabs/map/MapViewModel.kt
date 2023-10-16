package com.example.dashcarr.presentation.tabs.map

import android.location.Location
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dashcarr.domain.preferences.IPreferences
import com.example.dashcarr.domain.repository.IPointsOfInterestRepository
import com.example.dashcarr.presentation.tabs.map.data.PointOfInterest
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.dashcarr.data.repository.DataStoreKey
import com.example.dashcarr.data.repository.LoggedInValue
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

/**
 * ViewModel for the MapFragment. Manages location updates, markers, and UI state related to the map.
 *
 * @property sp Preferences implementation for saving and retrieving user location.
 * @property pointsOfInterestRepository Repository for managing points of interest on the map.
 */
@HiltViewModel
class MapViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository

    private val sp: IPreferences,
    private val pointsOfInterestRepository: IPointsOfInterestRepository
): ViewModel() {

    val appPreferences = userPreferencesRepository.appBoolFlow.asLiveData()


    fun updateAppPreferences(bool:Boolean){
        viewModelScope.launch {
            userPreferencesRepository.updateAlreadyLoggedIn(bool)
        }
    }


    // Channel for communicating the last saved user location
    private val _lastSavedUserLocation = Channel<GeoPoint>()
    val lastSavedUserLocation = _lastSavedUserLocation.receiveAsFlow()

    // StateFlow for holding the current device location
    private val _currentLocation = MutableStateFlow<Location?>(null)

    // SharedFlow for managing the state of creating a new marker on the map
    private val _createMarkerState = MutableSharedFlow<MapFragment.CreateMarkerState>()
    val createMarkerState = _createMarkerState.asSharedFlow()

    // StateFlow for managing the visibility of buttons bar
    private val _showButtonsBarState = MutableStateFlow<Boolean?>(null)
    val showButtonsBarState = _showButtonsBarState.asStateFlow()

    // LiveData for observing points of interest on the map
    val pointsOfInterestState = pointsOfInterestRepository.getAllPointsLiveData()

    /**
     * Initializes the ViewModel by retrieving the last saved user location from preferences.
     */
    init {
        viewModelScope.launch {
            sp.getLastUserLocation()?.let {
                _lastSavedUserLocation.send(it)
            }
        }
    }

    /**
     * Saves the current location to the ViewModel's StateFlow.
     *
     * @param location The current location of the device.
     */
    fun saveCurrentLocation(location: Location) {
        viewModelScope.launch {
            _currentLocation.emit(location)
        }
    }

    /**
     * Saves the last known device location to preferences.
     */
    fun saveLastKnownLocation() {
        _currentLocation.value?.let { sp.saveLastUserLocation(it) }
    }

    /**
     * Initiates the process of creating a new marker on the map.
     *
     * @param point The GeoPoint where the marker is to be created.
     */
    fun createMarker(point: GeoPoint) {
        viewModelScope.launch {
            _createMarkerState.emit(MapFragment.CreateMarkerState.CreateMarker(point))
        }
    }

    /**
     * Saves a new marker to the repository and manages the UI state accordingly.
     *
     * @param pointOfInterest The details of the new point of interest to be saved.
     */
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

    /**
     * Cancels the process of creating a new marker and hides the UI components.
     */
    fun cancelMarkerCreation() {
        viewModelScope.launch {
            _createMarkerState.emit(MapFragment.CreateMarkerState.HideMarker)
        }
    }

    /**
     * Toggles the visibility of the buttons bar in the UI.
     */
    fun showHideBar() {
        viewModelScope.launch {
            val currentShowValue = _showButtonsBarState.value
            _showButtonsBarState.emit(if (currentShowValue == null) true else currentShowValue == false)
        }
    }
}

class UserPreferencesRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
){
    val appBoolFlow: Flow<LoggedInValue> = dataStore.data
        .catch {
            if (it is IOException){
                it.printStackTrace()
                emit(emptyPreferences())
            }else{
                throw it
            }
        }.map { preferences ->
            val alreadyLoggedIn = preferences[DataStoreKey.ALREADY_LOGGED_IN] ?: false
            LoggedInValue(alreadyLoggedIn)
        }

    suspend fun updateAlreadyLoggedIn(newValue: Boolean){
        dataStore.edit { preferences ->
            preferences[DataStoreKey.ALREADY_LOGGED_IN] = newValue
        }
    }
}

class TasksRepository @Inject constructor()