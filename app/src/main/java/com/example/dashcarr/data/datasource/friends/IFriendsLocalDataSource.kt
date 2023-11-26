package com.example.dashcarr.data.datasource.friends

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import com.example.dashcarr.domain.entity.FriendsEntity

interface IFriendsLocalDataSource {

    fun getAllFriendsLiveData(): LiveData<List<FriendsEntity>>
    fun getFriendByIdLiveData(id: Int): LiveData<FriendsEntity>
    suspend fun getFriendById(id: Int): Result<FriendsEntity>


    @WorkerThread
    suspend fun saveNewFriend(friend: FriendsEntity): Result<Unit>

    @WorkerThread
    suspend fun deleteFriend(friend: FriendsEntity, timestamp: Long): Result<Unit>

    @WorkerThread
    suspend fun updateFriend(friend: FriendsEntity): Result<Unit>

    suspend fun deleteAllFriends(): Result<Int>

    suspend fun saveFriends(friends: List<FriendsEntity>): Result<Unit>
    suspend fun deleteAllData(): Int

}