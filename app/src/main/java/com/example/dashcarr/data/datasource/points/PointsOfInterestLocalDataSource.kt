package com.example.dashcarr.data.datasource.points

import androidx.lifecycle.LiveData
import com.example.dashcarr.data.database.dao.PointOfInterestDao
import com.example.dashcarr.data.mapper.safeCall
import com.example.dashcarr.domain.entity.PointOfInterestEntity
import javax.inject.Inject

/**
 * Local data source for Points of Interest using Room Database.
 *
 * @param pointsDao The Data Access Object (DAO) for Point of Interest entities.
 */
class PointsOfInterestLocalDataSource @Inject constructor(
    private val pointsDao: PointOfInterestDao
): IPointsOfInterestLocalDataSource {
    override fun getAllPointsLiveData(): LiveData<List<PointOfInterestEntity>> =
        pointsDao.getAllPointsOfInterestLiveData()

    override suspend fun saveNewPoint(point: PointOfInterestEntity): Result<Unit> = safeCall {
        Result.success(pointsDao.insertData(point))
    }

    override suspend fun deletePoint(point: PointOfInterestEntity) = safeCall {
        Result.success(pointsDao.deleteData(point))
    }

    override suspend fun updatePoint(point: PointOfInterestEntity) = safeCall {
        Result.success(pointsDao.updateData(point))
    }
}