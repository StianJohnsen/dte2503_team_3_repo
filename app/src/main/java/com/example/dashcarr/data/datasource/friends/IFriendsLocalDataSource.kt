package com.example.dashcarr.data.datasource.friends

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import com.example.dashcarr.domain.entity.FriendsEntity

/**
 * Interface for the local data source that manages friends data.
 * Defines methods for retrieving, adding, updating, and deleting friend records.
 */
interface IFriendsLocalDataSource {

    /**
     * Retrieves all friend records as LiveData.
     * @return LiveData containing a list of FriendsEntity.
     */
    fun getAllFriendsLiveData(): LiveData<List<FriendsEntity>>

    /**
     * Retrieves a specific friend record by ID as LiveData.
     * @param id The ID of the friend to retrieve.
     * @return LiveData containing the FriendsEntity.
     */
    fun getFriendByIdLiveData(id: Int): LiveData<FriendsEntity>

    /**
     * Retrieves a specific friend record by ID.
     * @param id The ID of the friend to retrieve.
     * @return A Result wrapper containing the FriendsEntity.
     */
    suspend fun getFriendById(id: Int): Result<FriendsEntity>

    /**
     * Saves a new friend record to the local data source.
     * This function should be called on a worker thread.
     * @param friend The FriendsEntity to save.
     * @return A Result wrapper indicating success.
     */
    @WorkerThread
    suspend fun saveNewFriend(friend: FriendsEntity): Result<Unit>

    /**
     * Deletes a friend record from the local data source.
     * This function should be called on a worker thread.
     * @param friend The FriendsEntity to delete.
     * @param timestamp The timestamp when the deletion occurs.
     * @return A Result wrapper indicating success.
     */
    @WorkerThread
    suspend fun deleteFriend(friend: FriendsEntity, timestamp: Long): Result<Unit>

    /**
     * Updates a friend record in the local data source.
     * This function should be called on a worker thread.
     * @param friend The FriendsEntity to update.
     * @return A Result wrapper indicating success.
     */
    @WorkerThread
    suspend fun updateFriend(friend: FriendsEntity): Result<Unit>

    /**
     * Deletes all friend records from the local data source.
     * @return A Result wrapper containing the number of rows affected.
     */
    suspend fun deleteAllFriends(): Result<Int>

    /**
     * Saves a list of friend records to the local data source.
     * @param friends The list of FriendsEntity to save.
     * @return A Result wrapper indicating success.
     */
    suspend fun saveFriends(friends: List<FriendsEntity>): Result<Unit>

    /**
     * Deletes all friend records from the local data source.
     * @return The number of rows affected.
     */
    suspend fun deleteAllData(): Int
}
