package com.example.dashcarr.data.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.dashcarr.domain.entity.FriendsEntity

@Dao
interface FriendsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(socialSettings: FriendsEntity): Long

    @Query("SELECT * FROM friends_entity")
    suspend fun getAllFriends(): List<FriendsEntity>

    @Query("SELECT * FROM friends_entity WHERE id = :id")
    fun getFriendById(id: Int): LiveData<FriendsEntity>

    @Update
    suspend fun update(friend: FriendsEntity): Int

    @Query("DELETE FROM friends_entity WHERE id = :id")
    suspend fun deleteById(id: Int): Int
}