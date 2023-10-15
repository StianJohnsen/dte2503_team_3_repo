package com.example.dashcarr.data.datasource.points

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import com.example.dashcarr.domain.entity.PointOfInterestEntity

/**
 * Interface defining local data source operations for Points of Interest in the application.
 */
interface IPointsOfInterestLocalDataSource {

    fun getAllPointsLiveData(): LiveData<List<PointOfInterestEntity>>

    @WorkerThread
    suspend fun saveNewPoint(point: PointOfInterestEntity): Result<Unit>

    @WorkerThread
    suspend fun deletePoint(point: PointOfInterestEntity): Result<Unit>

}