package com.example.dashcarr.data.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import com.example.dashcarr.domain.entity.PointOfInterestEntity

@Dao
interface PointOfInterestDao: BaseDao<PointOfInterestEntity> {

    @Query("SELECT * from points_of_interest")
    fun getAllPointsOfInterestLiveData(): LiveData<List<PointOfInterestEntity>>

}