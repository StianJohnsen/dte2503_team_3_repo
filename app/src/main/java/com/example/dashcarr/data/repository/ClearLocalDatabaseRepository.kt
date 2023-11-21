package com.example.dashcarr.data.repository

import com.example.dashcarr.domain.preferences.IPreferences
import com.example.dashcarr.domain.repository.IClearLocalDatabaseRepository
import com.example.dashcarr.domain.repository.IFriendsRepository
import com.example.dashcarr.domain.repository.IMessagesRepository
import com.example.dashcarr.domain.repository.IPointsOfInterestRepository
import com.example.dashcarr.domain.repository.ISentMessagesRepository
import javax.inject.Inject

class ClearLocalDatabaseRepository @Inject constructor(
    private val friendsRepository: IFriendsRepository,
    private val messagesRepository: IMessagesRepository,
    private val sentMessagesRepository: ISentMessagesRepository,
    private val pointsOfInterestRepository: IPointsOfInterestRepository,
    private val preferences: IPreferences
) : IClearLocalDatabaseRepository {
    override suspend fun clearAllDatabaseTables() {
        friendsRepository.deleteAllData()
        messagesRepository.deleteAllData()
        sentMessagesRepository.deleteAllData()
        pointsOfInterestRepository.deleteAllData()
        preferences.deleteAllLocalChangeTimeStamps()
    }
}