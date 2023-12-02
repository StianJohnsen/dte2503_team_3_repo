package com.example.dashcarr.domain.repository

import androidx.lifecycle.LiveData
import com.example.dashcarr.domain.entity.MessagesEntity

/**
 * Interface defining the operations for managing messages data.
 * Provides methods for CRUD (Create, Read, Update, Delete) operations on message entities.
 */
interface IMessagesRepository {

    fun getAllMessagesLiveData(): LiveData<List<MessagesEntity>>
    suspend fun saveNewMessage(message: MessagesEntity): Boolean
    suspend fun getAllMessages(): List<MessagesEntity>
    fun getMessageById(id: Int): LiveData<MessagesEntity>
    suspend fun deleteMessageById(id: Int): Result<Unit>
    suspend fun deleteMessage(message: MessagesEntity): Result<Unit>
    suspend fun updateMessage(message: MessagesEntity): Result<Unit>
    suspend fun deleteAllData(): Int
}