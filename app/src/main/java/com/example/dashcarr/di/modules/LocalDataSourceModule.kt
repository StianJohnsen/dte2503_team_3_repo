package com.example.dashcarr.di.modules

import com.example.dashcarr.data.datasource.points.IPointsOfInterestLocalDataSource
import com.example.dashcarr.data.datasource.points.PointsOfInterestLocalDataSource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Dagger Hilt module for providing local data source dependencies.
 */
@InstallIn(SingletonComponent::class)
@Module
interface LocalDataSourceModule {

    @Binds
    fun bindPointsOfInterestLocalDataSource(pointsOfInterestLocalDataSource: PointsOfInterestLocalDataSource): IPointsOfInterestLocalDataSource

}