package com.example.dashcarr.data.datasource.messages

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import com.example.dashcarr.domain.entity.SentMessageFinalEntity
import com.example.dashcarr.domain.entity.SentMessagesEntity

interface ISentMessagesLocalDataSource {

    fun getAllSentMessagesLiveData(): LiveData<List<SentMessageFinalEntity>>

    @WorkerThread
    suspend fun saveNewSentMessage(sentMessage: SentMessagesEntity): Result<Unit>
    @WorkerThread
    suspend fun deleteSentMessage(sentMessage: SentMessagesEntity): Result<Unit>
}