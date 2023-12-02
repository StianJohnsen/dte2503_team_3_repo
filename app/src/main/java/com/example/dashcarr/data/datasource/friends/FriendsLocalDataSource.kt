package com.example.dashcarr.data.datasource.friends

import androidx.lifecycle.LiveData
import com.example.dashcarr.data.database.dao.FriendsDao
import com.example.dashcarr.data.mapper.safeCall
import com.example.dashcarr.domain.entity.FriendsEntity
import com.example.dashcarr.domain.preferences.IPreferences
import javax.inject.Inject

/**
 * Local data source for managing friends data.
 * This class provides methods to interact with the local database through the FriendsDao.
 *
 * @property friendsDao DAO for accessing friends data in the local database.
 * @property preferences Interface for accessing shared preferences.
 */
class FriendsLocalDataSource @Inject constructor(
    private val friendsDao: FriendsDao,
    private val preferences: IPreferences
): IFriendsLocalDataSource {

    /**
     * Retrieves all friends from the local database as LiveData.
     * @return LiveData containing a list of FriendsEntity.
     */
    override fun getAllFriendsLiveData(): LiveData<List<FriendsEntity>> =
        friendsDao.getAllFriendsLiveData()

    /**
     * Retrieves a specific friend by ID as LiveData.
     * @param id The ID of the friend to retrieve.
     * @return LiveData containing the FriendsEntity.
     */
    override fun getFriendByIdLiveData(id: Int): LiveData<FriendsEntity> {
        return friendsDao.getFriendByIdLiveData(id)
    }

    /**
     * Retrieves a specific friend by ID.
     * @param id The ID of the friend to retrieve.
     * @return A Result wrapper containing the FriendsEntity.
     */
    override suspend fun getFriendById(id: Int): Result<FriendsEntity> = safeCall {
        Result.success(friendsDao.getFriendById(id))
    }

    /**
     * Saves a new friend to the local database.
     * @param friend The FriendsEntity to save.
     * @return A Result wrapper indicating success.
     */
    override suspend fun saveNewFriend(friend: FriendsEntity): Result<Unit> =
        Result.success(friendsDao.insertData(friend))

    /**
     * Deletes a friend from the local database and updates the last change timestamp.
     * @param friend The FriendsEntity to delete.
     * @param timestamp The timestamp of the deletion operation.
     */
    override suspend fun deleteFriend(friend: FriendsEntity, timestamp: Long) = safeCall {
        preferences.saveLastLocalFriendsTableChangesTimestamp(timestamp)
        Result.success(friendsDao.deleteData(friend))
    }

    /**
     * Updates a friend's information in the local database.
     * @param friend The FriendsEntity to update.
     * @return A Result wrapper indicating success.
     */
    override suspend fun updateFriend(friend: FriendsEntity) = safeCall {
        Result.success(friendsDao.updateData(friend))
    }

    /**
     * Deletes all friends from the local database.
     * @return A Result wrapper containing the number of rows affected.
     */
    override suspend fun deleteAllFriends(): Result<Int> = safeCall {
        Result.success(friendsDao.deleteAllData())
    }

    /**
     * Saves a list of friends to the local database.
     * @param friends The list of FriendsEntity to save.
     * @return A Result wrapper indicating success.
     */
    override suspend fun saveFriends(friends: List<FriendsEntity>): Result<Unit> = safeCall {
        Result.success(friendsDao.insertData(friends))
    }

    /**
     * Deletes all friends data from the local database.
     * @return The number of rows affected.
     */
    override suspend fun deleteAllData(): Int =
        friendsDao.deleteAllData()
}
