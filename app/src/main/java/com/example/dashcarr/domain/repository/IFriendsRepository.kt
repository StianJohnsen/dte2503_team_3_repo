package com.example.dashcarr.domain.repository

import androidx.lifecycle.LiveData
import com.example.dashcarr.domain.entity.FriendsEntity

/**
 * Interface defining the operations for managing friends data.
 * Provides methods for CRUD (Create, Read, Update, Delete) operations on friend entities.
 */
interface IFriendsRepository {

    fun getAllFriendsLiveData(): LiveData<List<FriendsEntity>>
    suspend fun saveNewFriend(friend: FriendsEntity): Boolean
    fun getFriendById(id: Int): LiveData<FriendsEntity>
    suspend fun updateFriend(friend: FriendsEntity): Result<Unit>
    suspend fun deleteFriend(friend: FriendsEntity): Result<Unit>
    suspend fun deleteFriendById(id: Int): Result<Unit>
    suspend fun deleteAllData(): Int

}