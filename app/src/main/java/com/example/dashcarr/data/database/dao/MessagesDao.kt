package com.example.dashcarr.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.example.dashcarr.domain.entity.MessagesEntity

@Dao
interface MessagesDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(socialSettings: MessagesEntity): Long

}