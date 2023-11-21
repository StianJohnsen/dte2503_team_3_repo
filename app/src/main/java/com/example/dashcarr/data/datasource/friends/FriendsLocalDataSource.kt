package com.example.dashcarr.data.datasource.friends

import androidx.lifecycle.LiveData
import com.example.dashcarr.data.database.dao.FriendsDao
import com.example.dashcarr.data.mapper.safeCall
import com.example.dashcarr.domain.entity.FriendsEntity
import com.example.dashcarr.domain.preferences.IPreferences
import javax.inject.Inject

class FriendsLocalDataSource @Inject constructor(
    private val friendsDao: FriendsDao,
    private val preferences: IPreferences
): IFriendsLocalDataSource {
    override fun getAllFriendsLiveData(): LiveData<List<FriendsEntity>> =
        friendsDao.getAllFriendsLiveData()

    override fun getFriendByIdLiveData(id: Int): LiveData<FriendsEntity> {
        return friendsDao.getFriendByIdLiveData(id)
    }

    override suspend fun getFriendById(id: Int): Result<FriendsEntity> = safeCall {
        Result.success(friendsDao.getFriendById(id))
    }

    override suspend fun saveNewFriend(friend: FriendsEntity): Result<Unit> =
        Result.success(friendsDao.insertData(friend))

    override suspend fun deleteFriend(friend: FriendsEntity, timestamp: Long) = safeCall {
        preferences.saveLastLocalFriendsTableChangesTimestamp(timestamp)
        Result.success(friendsDao.deleteData(friend))
    }

    override suspend fun updateFriend(friend: FriendsEntity) = safeCall {
        Result.success(friendsDao.updateData(friend))
    }

    override suspend fun deleteAllFriends(): Result<Int> = safeCall {
        Result.success(friendsDao.deleteAllData())
    }

    override suspend fun saveFriends(friends: List<FriendsEntity>): Result<Unit> = safeCall {
        Result.success(friendsDao.insertData(friends))
    }

    override suspend fun deleteAllData(): Int =
        friendsDao.deleteAllData()
}