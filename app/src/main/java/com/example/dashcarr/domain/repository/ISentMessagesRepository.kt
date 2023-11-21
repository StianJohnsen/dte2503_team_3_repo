package com.example.dashcarr.domain.repository

import androidx.lifecycle.LiveData
import com.example.dashcarr.domain.entity.SentMessageFinalEntity
import com.example.dashcarr.domain.entity.SentMessagesEntity

interface ISentMessagesRepository {
    fun getAllSentMessagesLiveData(): LiveData<List<SentMessageFinalEntity>>
    suspend fun saveNewSentMessage(sentMessage: SentMessagesEntity): Result<Unit>
    suspend fun deleteSentMessage(sentMessage: SentMessagesEntity): Result<Unit>
    suspend fun deleteAllData(): Int
}