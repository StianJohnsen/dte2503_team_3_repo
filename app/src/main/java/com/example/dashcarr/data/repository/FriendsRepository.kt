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

    override suspend fun getAllFriends(): List<FriendsEntity> {
        val friends = firebaseDBRepository.getAllFriends()
        return friends.map { FriendsEntity(
            id = 0,
            name = it.name ?: "",
            phone = it.phone ?: "",
            email = it.email ?: "",
            createdTimeStamp = it.createdTimeStamp ?: 0
        ) }
    }

    override fun getFriendById(id: Int): LiveData<FriendsEntity> {
        return firebaseDBRepository.getFriendById(id)
    }

    override suspend fun updateFriend(friend: FriendsEntity): Result<Unit> =
        friendsLocalDataSource.updateFriend(friend)

    override suspend fun deleteFriend(friend: FriendsEntity): Result<Unit> =
        friendsLocalDataSource.deleteFriend(friend)

    override suspend fun deleteFriendById(id: Int): Int {
        TODO("Not yet implemented")
    }

    override fun getAllFriendsLiveData(): LiveData<List<FriendsEntity>> =
        friendsLocalDataSource.getAllFriendsLiveData()

}