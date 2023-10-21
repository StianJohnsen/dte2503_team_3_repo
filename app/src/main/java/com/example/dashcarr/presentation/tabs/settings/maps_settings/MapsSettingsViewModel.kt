package com.example.dashcarr.presentation.tabs.settings.maps_settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dashcarr.data.repository.PointsOfInterestRepository
import com.example.dashcarr.domain.entity.PointOfInterestEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapsSettingsViewModel @Inject constructor(
    private val pointsOfInterestRepository: PointsOfInterestRepository
) : ViewModel() {

    // LiveData to observe points of interest
    val pointsOfInterest = pointsOfInterestRepository.getAllPointsLiveData()
    private var currentPoint : PointOfInterestEntity? = null

    private val _showDeleteDialog = MutableSharedFlow<Boolean>()
    val showDeleteDialog = _showDeleteDialog.asSharedFlow()
    private val _showRenameDialog = MutableSharedFlow<Boolean>()
    val showRenameDialog = _showRenameDialog.asSharedFlow()

    private val _onDeletePointSuccess = MutableSharedFlow<Unit>()
    val onDeletePointSuccess = _onDeletePointSuccess.asSharedFlow()
    private val _onDeletePointFailure = MutableSharedFlow<Throwable>()
    val onDeletePointFailure = _onDeletePointFailure.asSharedFlow()
    private val _onRenamePointSuccess = MutableSharedFlow<Unit>()
    val onRenamePointSuccess = _onRenamePointSuccess.asSharedFlow()
    private val _onRenamePointFailure = MutableSharedFlow<Throwable>()
    val onRenamePointFailure = _onRenamePointFailure.asSharedFlow()

    // Function to rename a point of interest
    fun updatePoint(newName: String) {
        currentPoint?.let {
            viewModelScope.launch {
                val result = pointsOfInterestRepository.updatePoint(currentPoint!!.copy(name = newName))
                _showRenameDialog.emit(false)
                result.onSuccess {
                    _onRenamePointSuccess.emit(Unit)
                }
                result.onFailure {
                    _onRenamePointFailure.emit(it)
                }
            }
        }
    }

    // Function to delete a point of interest
    fun deletePoint() {
        currentPoint?.let {
            viewModelScope.launch {
                val result = pointsOfInterestRepository.deletePoint(currentPoint!!)
                _showDeleteDialog.emit(false)
                result.onSuccess {
                    _onDeletePointSuccess.emit(Unit)
                }
                result.onFailure {
                    _onDeletePointFailure.emit(it)
                }
            }
        }

    }

    fun showConfirmDeleteDialog(point: PointOfInterestEntity) {
        viewModelScope.launch {
            currentPoint = point
            _showDeleteDialog.emit(true)
        }
    }

    fun showRenameDialog(point: PointOfInterestEntity) {
        viewModelScope.launch {
            currentPoint = point
            _showRenameDialog.emit(true)
        }
    }

    fun hideDeleteDialog() {
        viewModelScope.launch {
            _showDeleteDialog.emit(false)
        }
    }

    fun hideRenameDialog() {
        viewModelScope.launch {
            _showRenameDialog.emit(false)
        }
    }

}