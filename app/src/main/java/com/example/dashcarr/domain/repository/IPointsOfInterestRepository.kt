package com.example.dashcarr.domain.repository

import androidx.lifecycle.LiveData
import com.example.dashcarr.domain.entity.PointOfInterestEntity
import com.example.dashcarr.presentation.tabs.map.data.PointOfInterest

/**
 * Interface for managing operations related to Points of Interest.
 */
interface IPointsOfInterestRepository {
    fun getAllPointsLiveData(): LiveData<List<PointOfInterestEntity>>

    suspend fun saveNewPoint(point: PointOfInterest): Result<Unit>

    suspend fun deletePoint(point: PointOfInterestEntity): Result<Unit>

}