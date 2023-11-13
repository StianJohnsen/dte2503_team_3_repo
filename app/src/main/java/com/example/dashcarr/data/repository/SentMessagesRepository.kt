package com.example.dashcarr.data.repository

import androidx.lifecycle.LiveData
import com.example.dashcarr.data.datasource.messages.ISentMessagesLocalDataSource
import com.example.dashcarr.domain.entity.SentMessageFinalEntity
import com.example.dashcarr.domain.entity.SentMessagesEntity
import com.example.dashcarr.domain.repository.ISentMessagesRepository
import javax.inject.Inject

class SentMessagesRepository @Inject constructor(
    private val sendMessagesLocalDataSource: ISentMessagesLocalDataSource
): ISentMessagesRepository {
    override fun getAllSentMessagesLiveData(): LiveData<List<SentMessageFinalEntity>> =
        sendMessagesLocalDataSource.getAllSentMessagesLiveData()

    override suspend fun saveNewSentMessage(sentMessage: SentMessagesEntity): Result<Unit> =
        sendMessagesLocalDataSource.saveNewSentMessage(sentMessage)

    override suspend fun deleteSentMessage(sentMessage: SentMessagesEntity): Result<Unit> =
        sendMessagesLocalDataSource.deleteSentMessage(sentMessage)

}