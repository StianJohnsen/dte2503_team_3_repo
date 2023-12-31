package com.example.dashcarr.data.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.dashcarr.domain.entity.MessagesEntity

/**
 * A Data Access Object (DAO) for [MessagesEntity] entity.
 * Provides CRUD operations for [MessagesEntity] entity with [androidx.room.RoomDatabase].
 */
@Dao
interface MessagesDao: BaseDao<MessagesEntity> {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(socialSettings: MessagesEntity): Long

    @Query("SELECT * FROM messages_entity")
    suspend fun getAllMessages(): List<MessagesEntity>


    @Query("SELECT * FROM messages_entity WHERE id=:id")
    fun getMessageByIdLiveData(id: Int): LiveData<MessagesEntity>

    @Query("SELECT * FROM messages_entity WHERE id=:id")
    suspend fun getMessageById(id: Int): MessagesEntity

    @Query("SELECT * FROM messages_entity")
    fun getAllMessagesLiveData(): LiveData<List<MessagesEntity>>

    @Query("DELETE FROM messages_entity")
    suspend fun deleteAllData(): Int

    @Update
    suspend fun update(message: MessagesEntity): Int

    @Query("DELETE FROM messages_entity WHERE id = :id")
    suspend fun deleteById(id: Int): Int
}