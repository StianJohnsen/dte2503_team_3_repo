package com.example.dashcarr.data.repository

import androidx.lifecycle.LiveData
import com.example.dashcarr.data.datasource.friends.IFriendsLocalDataSource
import com.example.dashcarr.domain.entity.FriendsEntity
import com.example.dashcarr.domain.preferences.IPreferences
import com.example.dashcarr.domain.repository.IFirebaseDBRepository
import com.example.dashcarr.domain.repository.IFriendsRepository
import javax.inject.Inject

class FriendsRepository @Inject constructor(
    private val firebaseDBRepository: IFirebaseDBRepository,
    private val preference: IPreferences,
    private val friendsLocalDataSource: IFriendsLocalDataSource
) : IFriendsRepository {

    override suspend fun saveNewFriend(friend: FriendsEntity): Boolean {
        val currentTimestamp = System.currentTimeMillis()
        preference.saveLastLocalFriendsTableChangesTimestamp(currentTimestamp)
        val remoteResult = firebaseDBRepository.saveNewFriend(friend, currentTimestamp)
        val localResult = friendsLocalDataSource.saveNewFriend(friend).isSuccess
        return remoteResult && localResult
    }

    override suspend fun getAllFriends(): List<FriendsEntity> =
        friendsLocalDataSource.getAllFriendsLiveData().value ?: emptyList()


    override fun getFriendById(id: Int): LiveData<FriendsEntity> {
        return friendsLocalDataSource.getFriendByIdLiveData(id)
    }

    override suspend fun updateFriend(friend: FriendsEntity): Result<Unit> {
        val timestamp = System.currentTimeMillis()
        val oldFriend = friendsLocalDataSource.getFriendById(friend.id).getOrThrow()
        preference.saveLastLocalFriendsTableChangesTimestamp(timestamp)
        firebaseDBRepository.updateFriend(
            friend = friend,
            oldFriendTimestamp = oldFriend.createdTimeStamp,
            timestamp = timestamp)
        return friendsLocalDataSource.updateFriend(friend)
    }

    override suspend fun deleteFriend(friend: FriendsEntity): Result<Unit> {
        return if (friend.name.isEmpty()) {
            deleteFriendById(friend.id)
            Result.success(Unit)
        } else {
            val currentTimestamp = System.currentTimeMillis()
            firebaseDBRepository.deleteFriend(friend, currentTimestamp)
            friendsLocalDataSource.deleteFriend(friend, currentTimestamp)
        }
    }

    override suspend fun deleteFriendById(id: Int): Result<Unit> {
        val currentTimestamp = System.currentTimeMillis()
        val result = friendsLocalDataSource.getFriendById(id)
        result.onSuccess {
            firebaseDBRepository.deleteFriend(it, currentTimestamp)
            return friendsLocalDataSource.deleteFriend(it, currentTimestamp)
        }
        return Result.success(Unit)
    }

    override fun getAllFriendsLiveData(): LiveData<List<FriendsEntity>> =
        friendsLocalDataSource.getAllFriendsLiveData()

    override suspend fun deleteAllData(): Int =
        friendsLocalDataSource.deleteAllData()

}