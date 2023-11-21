package com.example.dashcarr.data.datasource.messages

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import com.example.dashcarr.domain.entity.MessagesEntity

interface IMessagesLocalDataSource {

    fun getAllMessagesLiveData(): LiveData<List<MessagesEntity>>
    fun getMessagesByIdLiveData(id: Int): LiveData<MessagesEntity>
    suspend fun getMessagesById(id: Int): Result<MessagesEntity>

    @WorkerThread
    suspend fun saveNewMessage(message: MessagesEntity): Result<Unit>

    @WorkerThread
    suspend fun deleteMessage(message: MessagesEntity, timestamp: Long): Result<Unit>

    @WorkerThread
    suspend fun updateMessage(message: MessagesEntity): Result<Unit>

    suspend fun deleteAllMessages(): Result<Int>

    suspend fun saveMessages(messages: List<MessagesEntity>): Result<Unit>
    suspend fun deleteAllData(): Int
}