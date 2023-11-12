package com.example.dashcarr.data.datasource.messages

import androidx.lifecycle.LiveData
import com.example.dashcarr.data.database.dao.SentMessageDao
import com.example.dashcarr.data.mapper.safeCall
import com.example.dashcarr.domain.entity.SentMessagesEntity
import javax.inject.Inject

class SentMessagesLocalDataSource @Inject constructor(
    private val sentMessageDao: SentMessageDao
): ISentMessagesLocalDataSource {
    override fun getAllSentMessagesLiveData(): LiveData<List<SentMessagesEntity>> =
        sentMessageDao.getAllSentMessagesLiveData()

    override suspend fun saveNewSentMessage(sentMessage: SentMessagesEntity): Result<Unit> = safeCall {
        Result.success(sentMessageDao.insertData(sentMessage))
    }

    override suspend fun deleteSentMessage(sentMessage: SentMessagesEntity) = safeCall {
        Result.success(sentMessageDao.deleteData(sentMessage))
    }

}
