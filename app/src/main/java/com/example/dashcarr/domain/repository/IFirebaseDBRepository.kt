package com.example.dashcarr.domain.repository

import com.example.dashcarr.domain.entity.FriendsEntity
import com.example.dashcarr.domain.entity.MessagesEntity
import com.example.dashcarr.domain.entity.firebase.FirebaseFriendEntity
import com.example.dashcarr.domain.entity.firebase.FirebaseMessageEntity
import com.example.dashcarr.domain.entity.firebase.GeoPointEntity
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot

/**
 * Interface for managing operations related to Firebase Realtime Database.
 */
interface IFirebaseDBRepository {

    suspend fun saveGeoPoint(geoPoint: GeoPointEntity): Boolean
    suspend fun getAllGeoPoints(): List<GeoPointEntity>

    // FRIENDS
    suspend fun saveNewFriend(friend: FriendsEntity, timestamp: Long): Boolean
    suspend fun getAllFriends(): List<FirebaseFriendEntity>
    suspend fun updateFriend(friend: FriendsEntity): Result<Unit>
    suspend fun deleteFriend(friend: FriendsEntity, timestamp: Long)

    suspend fun getLastFriendChangesTimestamp(): Task<DocumentSnapshot>
    suspend fun saveLastFriendChangesTimestamp(timestamp: Long)

    suspend fun deleteAllFriends(): Any
    suspend fun saveFriends(friends: List<FriendsEntity>)

    // MESSAGES
    suspend fun saveNewMessage(message: MessagesEntity, timestamp: Long): Boolean
    suspend fun getAllMessages(): List<FirebaseMessageEntity>
    suspend fun updateMessage(message: MessagesEntity): Result<Unit>
    suspend fun deleteMessage(message: MessagesEntity, timestamp: Long)
    suspend fun getLastMessageChangesTimestamp(): Task<DocumentSnapshot>
    suspend fun saveLastMessageChangesTimestamp(timestamp: Long)
    suspend fun deleteAllMessages(): Any
    suspend fun saveMessages(messages: List<MessagesEntity>)


}