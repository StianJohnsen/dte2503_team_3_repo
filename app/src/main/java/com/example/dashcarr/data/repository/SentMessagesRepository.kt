package com.example.dashcarr.data.repository

import androidx.lifecycle.LiveData
import com.example.dashcarr.data.datasource.sentmessages.ISentMessagesLocalDataSource
import com.example.dashcarr.domain.entity.SentMessageFinalEntity
import com.example.dashcarr.domain.entity.SentMessagesEntity
import com.example.dashcarr.domain.repository.ISentMessagesRepository
import javax.inject.Inject

/**
 * Repository class for managing sent messages data, interfacing with the local data source.
 *
 * @property sendMessagesLocalDataSource Local data source for sent messages.
 */
class SentMessagesRepository @Inject constructor(
    private val sendMessagesLocalDataSource: ISentMessagesLocalDataSource
): ISentMessagesRepository {
    override fun getAllSentMessagesLiveData(): LiveData<List<SentMessageFinalEntity>> =
        sendMessagesLocalDataSource.getAllSentMessagesLiveData()

    override suspend fun saveNewSentMessage(sentMessage: SentMessagesEntity): Result<Unit> =
        sendMessagesLocalDataSource.saveNewSentMessage(sentMessage)

    override suspend fun deleteSentMessage(sentMessage: SentMessagesEntity): Result<Unit> =
        sendMessagesLocalDataSource.deleteSentMessage(sentMessage)

    override suspend fun deleteAllData(): Int =
        sendMessagesLocalDataSource.deleteAllData()

}