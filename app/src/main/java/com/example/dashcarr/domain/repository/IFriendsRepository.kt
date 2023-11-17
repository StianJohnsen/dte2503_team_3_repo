package com.example.dashcarr.domain.repository

import androidx.lifecycle.LiveData
import com.example.dashcarr.domain.entity.FriendsEntity

interface IFriendsRepository {

    fun getAllFriendsLiveData(): LiveData<List<FriendsEntity>>
    suspend fun saveNewFriend(friend: FriendsEntity): Boolean
    suspend fun getAllFriends(): List<FriendsEntity>
    fun getFriendById(id: Int): LiveData<FriendsEntity>
    suspend fun updateFriend(friend: FriendsEntity): Result<Unit>
    suspend fun deleteFriend(friend: FriendsEntity): Result<Unit>
    suspend fun deleteFriendById(id: Int): Int

}