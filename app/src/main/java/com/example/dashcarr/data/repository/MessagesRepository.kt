package com.example.dashcarr.data.repository

import androidx.lifecycle.LiveData
import com.example.dashcarr.data.datasource.messages.IMessagesLocalDataSource
import com.example.dashcarr.domain.entity.MessagesEntity
import com.example.dashcarr.domain.preferences.IPreferences
import com.example.dashcarr.domain.repository.IFirebaseDBRepository
import com.example.dashcarr.domain.repository.IMessagesRepository
import javax.inject.Inject

/**
 * Repository class for managing message-related data, interfacing between local data sources and Firebase.
 *
 * @property messagesLocalDataSource Local data source for messages.
 * @property preference Repository for handling preferences.
 * @property firebaseDBRepository Repository for Firebase database interactions.
 */
class MessagesRepository @Inject constructor(
    private val messagesLocalDataSource: IMessagesLocalDataSource,
    private val preference: IPreferences,
    private val firebaseDBRepository: IFirebaseDBRepository
): IMessagesRepository {
    override fun getAllMessagesLiveData(): LiveData<List<MessagesEntity>> =
        messagesLocalDataSource.getAllMessagesLiveData()

    override suspend fun saveNewMessage(message: MessagesEntity): Boolean {
        val currentTimestamp = System.currentTimeMillis()
        preference.saveLastLocalMessagesTableChangesTimestamp(currentTimestamp)
        val remoteResult = firebaseDBRepository.saveNewMessage(message, currentTimestamp)
        val localResult = messagesLocalDataSource.saveNewMessage(message).isSuccess
        return remoteResult && localResult
    }

    override suspend fun getAllMessages(): List<MessagesEntity> {
        val messages = firebaseDBRepository.getAllMessages()
        return messages.map { MessagesEntity(
            id = 0,
            content = it.content ?: "",
            isPhone = it.isPhone ?: false,
            createdTimeStamp = it.createdTimeStamp ?: 0
        ) }
    }

    override fun getMessageById(id: Int): LiveData<MessagesEntity> {
        return messagesLocalDataSource.getMessagesByIdLiveData(id)
    }

    override suspend fun deleteMessageById(id: Int): Result<Unit> {
        val currentTimestamp = System.currentTimeMillis()
        val result = messagesLocalDataSource.getMessagesById(id)
        result.onSuccess {
            firebaseDBRepository.deleteMessage(it, currentTimestamp)
            return messagesLocalDataSource.deleteMessage(it, currentTimestamp)
        }
        return Result.success(Unit)
    }

    override suspend fun deleteMessage(message: MessagesEntity): Result<Unit> {
        return if (message.content.isEmpty()) {
            deleteMessageById(message.id.toInt())
            Result.success(Unit)
        } else {
            val currentTimestamp = System.currentTimeMillis()
            firebaseDBRepository.deleteMessage(message, currentTimestamp)
            messagesLocalDataSource.deleteMessage(message, currentTimestamp)
        }
    }

    override suspend fun updateMessage(message: MessagesEntity): Result<Unit> =
        messagesLocalDataSource.updateMessage(message)

    override suspend fun deleteAllData(): Int =
        messagesLocalDataSource.deleteAllData()
}