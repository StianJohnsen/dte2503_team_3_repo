package com.example.dashcarr.data.datasource.sentmessages

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import com.example.dashcarr.domain.entity.SentMessageFinalEntity
import com.example.dashcarr.domain.entity.SentMessagesEntity

/**
 * Interface for the local data source that manages sent messages data.
 * Defines methods for retrieving, adding, updating, and deleting sent messages records.
 */
interface ISentMessagesLocalDataSource {

    fun getAllSentMessagesLiveData(): LiveData<List<SentMessageFinalEntity>>

    @WorkerThread
    suspend fun saveNewSentMessage(sentMessage: SentMessagesEntity): Result<Unit>
    @WorkerThread
    suspend fun deleteSentMessage(sentMessage: SentMessagesEntity): Result<Unit>
    suspend fun deleteAllData(): Int
}