package com.example.dashcarr.di.modules

import com.example.dashcarr.data.repository.DatabasesSyncRepository
import com.example.dashcarr.data.repository.FirebaseAuthRepository
import com.example.dashcarr.data.repository.FirebaseDBRepository
import com.example.dashcarr.data.repository.FriendsRepository
import com.example.dashcarr.data.repository.MessagesRepository
import com.example.dashcarr.data.repository.PointsOfInterestRepository
import com.example.dashcarr.data.repository.SentMessagesRepository
import com.example.dashcarr.domain.repository.IDatabasesSyncRepository
import com.example.dashcarr.domain.repository.IFirebaseAuthRepository
import com.example.dashcarr.domain.repository.IFirebaseDBRepository
import com.example.dashcarr.domain.repository.IFriendsRepository
import com.example.dashcarr.domain.repository.IMessagesRepository
import com.example.dashcarr.domain.repository.IPointsOfInterestRepository
import com.example.dashcarr.domain.repository.ISentMessagesRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Dagger Hilt module for providing repository-related dependencies.
 */
@InstallIn(SingletonComponent::class)
@Module
interface RepositoryModule {
    @Binds
    fun bindAuthRepository(repository: FirebaseAuthRepository): IFirebaseAuthRepository


    @Binds
    fun bindPointsOfInterestRepository(repository: PointsOfInterestRepository): IPointsOfInterestRepository

    @Binds
    fun bindDBRepository(repository: FirebaseDBRepository): IFirebaseDBRepository

    @Binds
    fun bindSentMessagesRepository(repository: SentMessagesRepository): ISentMessagesRepository

    @Binds
    fun bindFriendsRepository(repository: FriendsRepository): IFriendsRepository

    @Binds
    fun bindMessagesRepository(repository: MessagesRepository): IMessagesRepository

    @Binds
    fun bindSyncDatabasesRepository(repository: DatabasesSyncRepository): IDatabasesSyncRepository
}