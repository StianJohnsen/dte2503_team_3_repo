package com.example.dashcarr.domain.repository

import androidx.lifecycle.LiveData
import com.example.dashcarr.domain.entity.MessagesEntity

interface IMessagesRepository {

    fun getAllMessagesLiveData(): LiveData<List<MessagesEntity>>

    suspend fun saveNewMessage(message: MessagesEntity): Result<Unit>

    suspend fun deleteMessage(message: MessagesEntity): Result<Unit>

    suspend fun updateMessage(message: MessagesEntity): Result<Unit>
    suspend fun deleteAllData(): Int
}