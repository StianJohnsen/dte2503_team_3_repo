package com.example.dashcarr.data.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.dashcarr.domain.entity.MessagesEntity


@Dao
interface MessagesDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(socialSettings: MessagesEntity): Long

    @Query("SELECT * FROM messages_entity")
    suspend fun getAllMessages(): List<MessagesEntity>


    @Query("SELECT * FROM messages_entity WHERE id=:id")
    fun getMesssageById(id: Int): LiveData<MessagesEntity>
}