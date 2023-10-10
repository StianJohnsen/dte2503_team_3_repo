package com.example.dashcarr.di.modules

import com.example.dashcarr.data.repository.FirebaseAuthRepository
import com.example.dashcarr.data.repository.FirebaseDBRepository
import com.example.dashcarr.data.repository.PointsOfInterestRepository
import com.example.dashcarr.domain.repository.IFirebaseAuthRepository
import com.example.dashcarr.domain.repository.IFirebaseDBRepository
import com.example.dashcarr.domain.repository.IPointsOfInterestRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@Module
interface RepositoryModule {
    @Binds
    fun bindAuthRepository(repository: FirebaseAuthRepository): IFirebaseAuthRepository


    @Binds
    fun bindPointsOfInterestRepository(repository: PointsOfInterestRepository): IPointsOfInterestRepository

    @Binds
    fun bindDBRepository(repository: FirebaseDBRepository): IFirebaseDBRepository
}