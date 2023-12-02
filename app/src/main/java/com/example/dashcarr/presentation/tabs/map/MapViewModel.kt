package com.example.dashcarr.presentation.tabs.map

import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.dashcarr.data.repository.UserPreferencesRepository
import com.example.dashcarr.domain.preferences.IPreferences
import com.example.dashcarr.domain.repository.IPointsOfInterestRepository
import com.example.dashcarr.presentation.tabs.map.data.PointOfInterest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.osmdroid.util.GeoPoint
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject

/**
 * ViewModel for the MapFragment. Manages location updates, markers, and UI state related to the map.
 *
 * @property userPreferencesRepository Repository for managing UserPreferences (If user already logged in before
 * @property sp Preferences implementation for saving and retrieving user location.
 * @property pointsOfInterestRepository Repository for managing points of interest on the map.
 */
@HiltViewModel
class MapViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val sp: IPreferences,
    private val pointsOfInterestRepository: IPointsOfInterestRepository
) : ViewModel() {

    val appPreferences = userPreferencesRepository.userPreferenceFlow.asLiveData()

    fun updateAppPreferences(bool: Boolean) {
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

    fun getWeatherData(latitude: Double, longitude: Double, callback: (String) -> Unit) {
        viewModelScope.launch {
            val response = withContext(Dispatchers.IO) {
                val urlString =
                    "https://api.met.no/weatherapi/locationforecast/2.0/compact?lat=$latitude&lon=$longitude"
                val url = URL(urlString)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.setRequestProperty(
                    "User-Agent",
                    "DashCarr/0.9 https://github.com/StianJohnsen/dte2503_team_3_repo"
                )

                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    val reader = BufferedReader(InputStreamReader(connection.inputStream))
                    val response = StringBuilder()
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        response.append(line)
                    }
                    reader.close()

                    val fileContent = response.toString()
                    val jsonObject = JSONObject(fileContent)
                    val timeseries = jsonObject.getJSONObject("properties")
                        .getJSONArray("timeseries")
                        .getJSONObject(0)
                        .getJSONObject("data")
                        .getJSONObject("next_1_hours")
                        .getJSONObject("summary")
                        .getString("symbol_code")

                    timeseries
                } else {
                    "Error: ${connection.responseCode}"
                }
            }
            callback(response)
        }
    }

    fun getFilter(): ColorFilter {
        val inverseMatrix = ColorMatrix(
            floatArrayOf(
                -1.0f, 0.0f, 0.0f, 0.0f, 255f,
                0.0f, -1.0f, 0.0f, 0.0f, 255f,
                0.0f, 0.0f, -1.0f, 0.0f, 255f,
                0.0f, 0.0f, 0.0f, 1.0f, 0.0f
            )
        )

        val destinationColor: Int = Color.parseColor("#FF2A2A2A")
        val lr: Float = (255.0f - Color.red(destinationColor)) / 255.0f
        val lg: Float = (255.0f - Color.green(destinationColor)) / 255.0f
        val lb: Float = (255.0f - Color.blue(destinationColor)) / 255.0f
        val grayscaleMatrix = ColorMatrix(
            floatArrayOf(
                lr, lg, lb, 0f, 0f,
                lr, lg, lb, 0f, 0f,
                lr, lg, lb, 0f, 0f,
                0f, 0f, 0f, 0f, 255f
            )
        )
        grayscaleMatrix.preConcat(inverseMatrix)
        val dr: Int = Color.red(destinationColor)
        val dg: Int = Color.green(destinationColor)
        val db: Int = Color.blue(destinationColor)
        val drf = dr / 255f
        val dgf = dg / 255f
        val dbf = db / 255f
        val tintMatrix = ColorMatrix(
            floatArrayOf(
                1f, 0f, 0f, 0f, 0f,
                0f, 1f, 0f, 0f, 0f,
                0f, 0f, 1f, 0f, 0f,
                0f, 0f, 0f, 1f, 0f
            )
        )
        tintMatrix.preConcat(grayscaleMatrix)
        val lDestination = drf * lr + dgf * lg + dbf * lb
        val scale = 1f - lDestination
        val translate = 1 - scale * 0.5f
        val scaleMatrix = ColorMatrix(
            floatArrayOf(
                scale, 0f, 0f, 0f, dr * translate,
                0f, scale, 0f, 0f, dg * translate,
                0f, 0f, scale, 0f, db * translate,
                0f, 0f, 0f, 1f, 0f
            )
        )
        scaleMatrix.preConcat(tintMatrix)

        return ColorMatrixColorFilter(scaleMatrix)
    }

}