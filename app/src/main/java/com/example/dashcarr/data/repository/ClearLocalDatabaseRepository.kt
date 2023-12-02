package com.example.dashcarr.data.repository

import com.example.dashcarr.domain.preferences.IPreferences
import com.example.dashcarr.domain.repository.IClearLocalDatabaseRepository
import com.example.dashcarr.domain.repository.IFriendsRepository
import com.example.dashcarr.domain.repository.IMessagesRepository
import com.example.dashcarr.domain.repository.IPointsOfInterestRepository
import com.example.dashcarr.domain.repository.ISentMessagesRepository
import javax.inject.Inject

/**
 * Repository that manages clearing all local database tables.
 *
 * @param friendsRepository The repository that manages friends data.
 * @param messagesRepository The repository that manages messages data.
 * @param sentMessagesRepository The repository that manages sent messages data.
 * @param pointsOfInterestRepository The repository that manages points of interest data.
 * @param preferences The repository that manages preferences data.
 */
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