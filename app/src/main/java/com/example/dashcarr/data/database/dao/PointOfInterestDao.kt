package com.example.dashcarr.data.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import com.example.dashcarr.domain.entity.PointOfInterestEntity

/**
 * Data Access Object (DAO) for interacting with the "points_of_interest" table.
 *
 * @see BaseDao
 * @param PointOfInterestEntity The type of entity representing a Point of Interest.
 */
@Dao
interface PointOfInterestDao: BaseDao<PointOfInterestEntity> {

    /**
     * Retrieves a [LiveData] containing a list of all Points of Interest from the database.
     *
     * @return A [LiveData] containing a list of [PointOfInterestEntity].
     */
    @Query("SELECT * from points_of_interest")
    fun getAllPointsOfInterestLiveData(): LiveData<List<PointOfInterestEntity>>

}