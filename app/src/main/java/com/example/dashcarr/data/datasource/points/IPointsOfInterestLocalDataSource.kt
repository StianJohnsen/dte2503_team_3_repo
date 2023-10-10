package com.example.dashcarr.data.datasource.points

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import com.example.dashcarr.domain.entity.PointOfInterestEntity

interface IPointsOfInterestLocalDataSource {

    fun getAllPointsLiveData(): LiveData<List<PointOfInterestEntity>>

    @WorkerThread
    suspend fun saveNewPoint(point: PointOfInterestEntity): Result<Unit>

    @WorkerThread
    suspend fun deletePoint(point: PointOfInterestEntity): Result<Unit>

}