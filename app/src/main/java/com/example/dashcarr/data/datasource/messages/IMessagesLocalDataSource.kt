package com.example.dashcarr.data.datasource.messages

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import com.example.dashcarr.domain.entity.MessagesEntity

interface IMessagesLocalDataSource {

    fun getAllMessagesLiveData(): LiveData<List<MessagesEntity>>

    @WorkerThread
    suspend fun saveNewMessage(message: MessagesEntity): Result<Unit>

    @WorkerThread
    suspend fun deleteMessage(message: MessagesEntity): Result<Unit>

    @WorkerThread
    suspend fun updateMessage(message: MessagesEntity): Result<Unit>
    suspend fun deleteAllData(): Int
}