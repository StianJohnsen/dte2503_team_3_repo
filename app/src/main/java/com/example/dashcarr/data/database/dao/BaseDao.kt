package com.example.dashcarr.data.database.dao

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Update


interface BaseDao<in T> {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertData(vararg: T)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertData(list: List<T>)

    @Delete
    suspend fun deleteData(vararg: T)

    @Delete
    suspend fun deleteData(list: List<T>)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateData(vararg: T)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateData(list: List<T>)

}