package com.example.dashcarr.data.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.dashcarr.domain.entity.SentMessageFinalEntity
import com.example.dashcarr.domain.entity.SentMessagesEntity

/**
 * A Data Access Object (DAO) for [SentMessagesEntity] entity.
 * Provides CRUD operations for [SentMessagesEntity] entity with [androidx.room.RoomDatabase].
 */
@Dao
interface SentMessageDao: BaseDao<SentMessagesEntity> {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(sentMessage: SentMessagesEntity): Long

    @Query("SELECT * FROM sent_messages WHERE friendId = :friendId")
    suspend fun getMessageByFriendId(friendId: Int): SentMessagesEntity?

    @Delete
    suspend fun delete(sentMessage: SentMessagesEntity)

    @Query("DELETE FROM sent_messages WHERE friendId = :friendId")
    suspend fun deleteByFriendId(friendId: Int)

    @Query("DELETE FROM sent_messages WHERE messageId = :messageId")
    suspend fun deleteByMessageId(messageId: Long)

    @Query("SELECT * FROM sent_messages")
    fun getAllSentMessagesLiveData(): LiveData<List<SentMessageFinalEntity>>

    @Query("DELETE FROM sent_messages")
    suspend fun deleteAllData(): Int
}