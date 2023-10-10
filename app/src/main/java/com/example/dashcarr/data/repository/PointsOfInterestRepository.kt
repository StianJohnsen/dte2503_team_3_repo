package com.example.dashcarr.data.repository

import androidx.lifecycle.LiveData
import com.example.dashcarr.data.datasource.points.IPointsOfInterestLocalDataSource
import com.example.dashcarr.domain.entity.PointOfInterestEntity
import com.example.dashcarr.domain.mapper.toEntity
import com.example.dashcarr.domain.repository.IPointsOfInterestRepository
import com.example.dashcarr.presentation.tabs.map.data.PointOfInterest
import javax.inject.Inject

class PointsOfInterestRepository @Inject constructor(
    private val pointsOfInterestLocalDataSource: IPointsOfInterestLocalDataSource
): IPointsOfInterestRepository {
    override fun getAllPointsLiveData(): LiveData<List<PointOfInterestEntity>> =
        pointsOfInterestLocalDataSource.getAllPointsLiveData()

    override suspend fun saveNewPoint(point: PointOfInterest): Result<Unit> =
        pointsOfInterestLocalDataSource.saveNewPoint(point.toEntity())

    override suspend fun deletePoint(point: PointOfInterestEntity): Result<Unit> =
        pointsOfInterestLocalDataSource.deletePoint(point)

}