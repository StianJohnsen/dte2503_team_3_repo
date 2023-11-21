package com.example.dashcarr.data.repository

import androidx.lifecycle.LiveData
import com.example.dashcarr.data.datasource.messages.IMessagesLocalDataSource
import com.example.dashcarr.domain.entity.MessagesEntity
import com.example.dashcarr.domain.repository.IMessagesRepository
import javax.inject.Inject

class MessagesRepository @Inject constructor(
    private val messagesLocalDataSource: IMessagesLocalDataSource
): IMessagesRepository {
    override fun getAllMessagesLiveData(): LiveData<List<MessagesEntity>> =
        messagesLocalDataSource.getAllMessagesLiveData()

    override suspend fun saveNewMessage(message: MessagesEntity): Result<Unit> =
        messagesLocalDataSource.saveNewMessage(message)

    override suspend fun deleteMessage(message: MessagesEntity): Result<Unit> =
        messagesLocalDataSource.deleteMessage(message)

    override suspend fun updateMessage(message: MessagesEntity): Result<Unit> =
        messagesLocalDataSource.updateMessage(message)

    override suspend fun deleteAllData(): Int =
        messagesLocalDataSource.deleteAllData()
}