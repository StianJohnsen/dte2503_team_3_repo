package com.example.dashcarr.di.modules

import com.example.dashcarr.data.datasource.friends.FriendsLocalDataSource
import com.example.dashcarr.data.datasource.friends.IFriendsLocalDataSource
import com.example.dashcarr.data.datasource.messages.IMessagesLocalDataSource
import com.example.dashcarr.data.datasource.messages.MessagesLocalDataSource
import com.example.dashcarr.data.datasource.points.IPointsOfInterestLocalDataSource
import com.example.dashcarr.data.datasource.points.PointsOfInterestLocalDataSource
import com.example.dashcarr.data.datasource.sentmessages.ISentMessagesLocalDataSource
import com.example.dashcarr.data.datasource.sentmessages.SentMessagesLocalDataSource
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

    @Binds
    fun bindSentMessagesLocalDataSource(sentMessagesLocalDataSource: SentMessagesLocalDataSource): ISentMessagesLocalDataSource

    @Binds
    fun bindFriendsLocalDataSource(friendsLocalDataSource: FriendsLocalDataSource): IFriendsLocalDataSource

    @Binds
    fun bindMessagesLocalDataSource(messagesLocalDataSource: MessagesLocalDataSource): IMessagesLocalDataSource

}