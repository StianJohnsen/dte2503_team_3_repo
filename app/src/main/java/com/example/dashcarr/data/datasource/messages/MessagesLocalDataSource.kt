package com.example.dashcarr.data.datasource.messages

import androidx.lifecycle.LiveData
import com.example.dashcarr.data.database.dao.MessagesDao
import com.example.dashcarr.data.mapper.safeCall
import com.example.dashcarr.domain.entity.MessagesEntity
import javax.inject.Inject

class MessagesLocalDataSource @Inject constructor(
    private val messagesDao: MessagesDao
): IMessagesLocalDataSource{
    override fun getAllMessagesLiveData(): LiveData<List<MessagesEntity>> =
        messagesDao.getAllMessagesLiveData()

    override suspend fun saveNewMessage(message: MessagesEntity): Result<Unit> = safeCall {
        Result.success(messagesDao.insertData(message))
    }

    override suspend fun deleteMessage(message: MessagesEntity) = safeCall {
        Result.success(messagesDao.deleteData(message))
    }

    override suspend fun updateMessage(message: MessagesEntity) = safeCall {
        Result.success(messagesDao.updateData(message))
    }

}