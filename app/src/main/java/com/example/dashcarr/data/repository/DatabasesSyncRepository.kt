package com.example.dashcarr.data.repository

import com.example.dashcarr.data.constants.FirebaseTables
import com.example.dashcarr.data.datasource.friends.IFriendsLocalDataSource
import com.example.dashcarr.data.datasource.messages.IMessagesLocalDataSource
import com.example.dashcarr.data.mapper.toFriendEntity
import com.example.dashcarr.data.mapper.toMessageEntity
import com.example.dashcarr.domain.preferences.IPreferences
import com.example.dashcarr.domain.repository.IDatabasesSyncRepository
import com.example.dashcarr.domain.repository.IFirebaseAuthRepository
import com.example.dashcarr.domain.repository.IFirebaseDBRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Repository class responsible for synchronizing data between local databases and Firebase.
 *
 * @property firebaseDBRepository Interface to interact with Firebase database.
 * @property preferences Interface to interact with shared preferences.
 * @property friendsLocalDataSource Local data source for friends data.
 * @property firebaseAuthRepository Interface to interact with Firebase authentication.
 * @property messagesLocalDataSource Local data source for messages data.
 */
class DatabasesSyncRepository @Inject constructor(
    private val firebaseDBRepository: IFirebaseDBRepository,
    private val preferences: IPreferences,
    private val friendsLocalDataSource: IFriendsLocalDataSource,
    private val firebaseAuthRepository: IFirebaseAuthRepository,
    private val messagesLocalDataSource: IMessagesLocalDataSource
) : IDatabasesSyncRepository {

    /**
     * Synchronizes all data (friends and messages) between local databases and Firebase.
     */
    override suspend fun syncDatabases() {
        if (firebaseAuthRepository.getUserId().isNullOrEmpty()) return
        syncFriendsDatabases()
        syncMessagesDatabases()
    }

    /**
     * Synchronizes friends data between the local database and Firebase.
     */
    private suspend fun syncFriendsDatabases() {
        firebaseDBRepository.getLastFriendChangesTimestamp().addOnCompleteListener {
            CoroutineScope(Dispatchers.IO).launch {
                if (it.isSuccessful) {
                    val lastRemoteChangesTimestamp = it.result.getLong(FirebaseTables.LAST_FRIENDS_CHANGES_TIMESTAMP_KEY) ?: 0
                    val lastLocalChangesTimestamp = preferences.getLastLocalFriendsTableChangesTimestamp()

                    if (lastRemoteChangesTimestamp > lastLocalChangesTimestamp) {
                        val result = friendsLocalDataSource.deleteAllFriends()
                        result.onSuccess {
                            val remoteFriends = firebaseDBRepository.getAllFriends().map { it.toFriendEntity() }
                            friendsLocalDataSource.saveFriends(remoteFriends)
                            val currentTimestamp = System.currentTimeMillis()
                            preferences.saveLastLocalFriendsTableChangesTimestamp(currentTimestamp)
                            firebaseDBRepository.saveLastFriendChangesTimestamp(currentTimestamp)
                        }
                    }
                    else if (lastRemoteChangesTimestamp < lastLocalChangesTimestamp) {
                        firebaseDBRepository.deleteAllFriends()
                        val localFriends = friendsLocalDataSource.getAllFriendsLiveData().value
                        localFriends?.let { firebaseDBRepository.saveFriends(it) }
                        val currentTimestamp = System.currentTimeMillis()
                        preferences.saveLastLocalFriendsTableChangesTimestamp(currentTimestamp)
                        firebaseDBRepository.saveLastFriendChangesTimestamp(currentTimestamp)
                    }
                }
            }
        }
    }

    /**
     * Synchronizes messages data between the local database and Firebase.
     */
    private suspend fun syncMessagesDatabases() {
        firebaseDBRepository.getLastMessageChangesTimestamp().addOnCompleteListener {
            CoroutineScope(Dispatchers.IO).launch {
                if (it.isSuccessful) {
                    val lastRemoteChangesTimestamp = it.result.getLong(FirebaseTables.LAST_MESSAGES_CHANGES_TIMESTAMP_KEY) ?: 0
                    val lastLocalChangesTimestamp = preferences.getLastLocalMessagesTableChangesTimestamp()

                    if (lastRemoteChangesTimestamp > lastLocalChangesTimestamp) {
                        val result = messagesLocalDataSource.deleteAllMessages()
                        result.onSuccess {
                            val remoteMessages = firebaseDBRepository.getAllMessages().map { it.toMessageEntity() }
                            messagesLocalDataSource.saveMessages(remoteMessages)
                            val currentTimestamp = System.currentTimeMillis()
                            preferences.saveLastLocalMessagesTableChangesTimestamp(currentTimestamp)
                            firebaseDBRepository.saveLastMessageChangesTimestamp(currentTimestamp)
                        }
                    }
                    else if (lastRemoteChangesTimestamp < lastLocalChangesTimestamp) {
                        firebaseDBRepository.deleteAllMessages()
                        val localMessages = messagesLocalDataSource.getAllMessagesLiveData().value
                        localMessages?.let { firebaseDBRepository.saveMessages(it) }
                        val currentTimestamp = System.currentTimeMillis()
                        preferences.saveLastLocalMessagesTableChangesTimestamp(currentTimestamp)
                        firebaseDBRepository.saveLastMessageChangesTimestamp(currentTimestamp)
                    }
                }
            }
        }
    }

}