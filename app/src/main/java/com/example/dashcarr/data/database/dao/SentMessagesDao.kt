package com.example.dashcarr.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.dashcarr.domain.entity.SentMessagesEntity

@Dao
interface SentMessageDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(sentMessage: SentMessagesEntity): Long

    @Query("SELECT * FROM sent_messages WHERE friendId = :friendId")
    suspend fun getMessageByFriendId(friendId: Long): SentMessagesEntity?

    @Query("SELECT * FROM sent_messages WHERE messageId = :messageId")
    suspend fun getFriendsByMessageId(messageId: Long): List<SentMessagesEntity>

    @Delete
    suspend fun delete(sentMessage: SentMessagesEntity)

    @Query("DELETE FROM sent_messages WHERE friendId = :friendId")
    suspend fun deleteByFriendId(friendId: Long)

    @Query("DELETE FROM sent_messages WHERE messageId = :messageId")
    suspend fun deleteByMessageId(messageId: Long)
}