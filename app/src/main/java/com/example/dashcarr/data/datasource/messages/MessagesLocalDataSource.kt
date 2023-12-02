package com.example.dashcarr.data.datasource.messages

import androidx.lifecycle.LiveData
import com.example.dashcarr.data.database.dao.MessagesDao
import com.example.dashcarr.data.mapper.safeCall
import com.example.dashcarr.domain.entity.MessagesEntity
import com.example.dashcarr.domain.preferences.IPreferences
import javax.inject.Inject

/**
 * Local data source for messages data.
 * @param messagesDao The DAO for messages data.
 * @param preferences The shared preferences for the app.
 */
class MessagesLocalDataSource @Inject constructor(
    private val messagesDao: MessagesDao,
    private val preferences: IPreferences
): IMessagesLocalDataSource{
    override fun getAllMessagesLiveData(): LiveData<List<MessagesEntity>> =
        messagesDao.getAllMessagesLiveData()

    override fun getMessagesByIdLiveData(id: Int): LiveData<MessagesEntity> {
        return messagesDao.getMessageByIdLiveData(id)
    }

    override suspend fun getMessagesById(id: Int): Result<MessagesEntity> = safeCall {
        Result.success(messagesDao.getMessageById(id))
    }

    override suspend fun saveNewMessage(message: MessagesEntity): Result<Unit> = safeCall {
        Result.success(messagesDao.insertData(message))
    }

    override suspend fun deleteMessage(message: MessagesEntity, timestamp: Long) = safeCall {
        preferences.saveLastLocalMessagesTableChangesTimestamp(timestamp)
        Result.success(messagesDao.deleteData(message))
    }


    override suspend fun updateMessage(message: MessagesEntity) = safeCall {
        Result.success(messagesDao.updateData(message))
    }

    override suspend fun deleteAllMessages(): Result<Int> = safeCall {
        Result.success(messagesDao.deleteAllData())
    }

    override suspend fun saveMessages(messages: List<MessagesEntity>): Result<Unit> = safeCall {
        Result.success(messagesDao.insertData(messages))
    }

    override suspend fun deleteAllData(): Int =
        messagesDao.deleteAllData()


}