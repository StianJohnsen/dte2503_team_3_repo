package com.example.dashcarr.data.repository

import com.example.dashcarr.data.constants.FirebaseTables
import com.example.dashcarr.domain.entity.FriendsEntity
import com.example.dashcarr.domain.entity.MessagesEntity
import com.example.dashcarr.domain.entity.firebase.FirebaseFriendEntity
import com.example.dashcarr.domain.entity.firebase.FirebaseMessageEntity
import com.example.dashcarr.domain.repository.IFirebaseAuthRepository
import com.example.dashcarr.domain.repository.IFirebaseDBRepository
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/**
 * Repository for handling interactions with Firebase Firestore,
 * specifically for managing data related to friends and messages.
 *
 * @param firebaseAuthRepository The repository for Firebase Authentication, used for user identification.
 */
class FirebaseDBRepository @Inject constructor(
    private val firebaseAuthRepository: IFirebaseAuthRepository
): IFirebaseDBRepository {

    private val db = Firebase.firestore

    override suspend fun saveNewFriend(friend: FriendsEntity, timestamp: Long): Boolean {
        saveLastFriendChangesTimestamp(timestamp)
        db.collection(firebaseAuthRepository.getUserId()!!)
            .document(FirebaseTables.FRIENDS_DOCUMENT)
            .collection(FirebaseTables.FRIENDS_COLLECTION)
            .add(
                mapOf(
                    Pair(FirebaseTables.FRIEND_EMAIL_KEY, friend.email),
                    Pair(FirebaseTables.FRIEND_PHONE_KEY, friend.phone),
                    Pair(FirebaseTables.FRIEND_NAME_KEY, friend.name),
                    Pair(FirebaseTables.FRIEND_CREATED_TIMESTAMP_KEY, friend.createdTimeStamp)
                )
            )
        return true
    }

    override suspend fun getAllFriends(): List<FirebaseFriendEntity> =
        db.collection(firebaseAuthRepository.getUserId()!!)
            .document(FirebaseTables.FRIENDS_DOCUMENT)
            .collection(FirebaseTables.FRIENDS_COLLECTION)
            .get()
            .await()
            .map {
                it.toObject()
            }

    override suspend fun updateFriend(friend: FriendsEntity, oldFriendTimestamp: Long, timestamp: Long): Any? =
        db.collection(firebaseAuthRepository.getUserId()!!)
            .document(FirebaseTables.FRIENDS_DOCUMENT)
            .collection(FirebaseTables.FRIENDS_COLLECTION)
            .get()
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    it.result.documents.forEach { document ->
                        if (document.data?.get(FirebaseTables.FRIEND_CREATED_TIMESTAMP_KEY).toString() == oldFriendTimestamp.toString()) {
                            document.reference.update(
                                mapOf(
                                    Pair(FirebaseTables.FRIEND_EMAIL_KEY, friend.email),
                                    Pair(FirebaseTables.FRIEND_PHONE_KEY, friend.phone),
                                    Pair(FirebaseTables.FRIEND_NAME_KEY, friend.name),
                                    Pair(FirebaseTables.FRIEND_CREATED_TIMESTAMP_KEY, friend.createdTimeStamp)
                                )
                            )
                        }
                    }
                }
            }

    override suspend fun deleteFriend(friend: FriendsEntity, timestamp: Long) {
        saveLastFriendChangesTimestamp(timestamp)
        db.collection(firebaseAuthRepository.getUserId()!!)
            .document(FirebaseTables.FRIENDS_DOCUMENT)
            .collection(FirebaseTables.FRIENDS_COLLECTION)
            .get()
            .addOnCompleteListener { snapshot ->
                if (snapshot.isSuccessful) {
                    snapshot.result.documents.forEach { document ->
                        if (document.data?.
                            get(FirebaseTables.FRIEND_CREATED_TIMESTAMP_KEY).toString()
                            == friend.createdTimeStamp.toString()
                            )
                            document.reference.delete()
                    }
                }
            }
    }

    override suspend fun getLastFriendChangesTimestamp(): Task<DocumentSnapshot> =
        db.collection(firebaseAuthRepository.getUserId()!!)
            .document(FirebaseTables.LAST_CHANGES_DOCUMENT)
            .get()

    override suspend fun deleteAllFriends(): Any? =
        db.collection(firebaseAuthRepository.getUserId()!!)
            .document(FirebaseTables.FRIENDS_DOCUMENT)
            .delete()
            .await()

    override suspend fun saveFriends(friends: List<FriendsEntity>) {
        val mapOfFriends = mutableListOf<Map<String, Any>>()
        friends.forEach { friend ->
            mapOfFriends.add(
                mapOf(
                    Pair(FirebaseTables.FRIEND_EMAIL_KEY, friend.email),
                    Pair(FirebaseTables.FRIEND_PHONE_KEY, friend.phone),
                    Pair(FirebaseTables.FRIEND_NAME_KEY, friend.name),
                    Pair(FirebaseTables.FRIEND_CREATED_TIMESTAMP_KEY, friend.createdTimeStamp)
                )
            )
        }
        saveLastFriendChangesTimestamp(System.currentTimeMillis())
        db.collection(firebaseAuthRepository.getUserId()!!)
            .document(FirebaseTables.FRIENDS_DOCUMENT)
            .collection(FirebaseTables.FRIENDS_COLLECTION)
            .add(mapOfFriends)
            .await()
    }

    override suspend fun saveLastFriendChangesTimestamp(timestamp: Long) {
        db.collection(firebaseAuthRepository.getUserId()!!)
            .document(FirebaseTables.LAST_CHANGES_DOCUMENT)
            .set(mapOf(
                Pair(FirebaseTables.LAST_FRIENDS_CHANGES_TIMESTAMP_KEY, timestamp)
            ), SetOptions.merge())
    }

    override suspend fun saveNewMessage(message: MessagesEntity, timestamp: Long): Boolean {
        saveLastMessageChangesTimestamp(timestamp)
        db.collection(firebaseAuthRepository.getUserId()!!)
            .document(FirebaseTables.MESSAGES_DOCUMENT)
            .collection(FirebaseTables.MESSAGES_COLLECTION)
            .add(
                mapOf(
                    Pair(FirebaseTables.MESSAGE_CONTENT_KEY, message.content),
                    Pair(FirebaseTables.MESSAGE_IPHONE_KEY, message.isPhone),
                    Pair(FirebaseTables.MESSAGE_CREATED_TIMESTAMP_KEY, message.createdTimeStamp)
                )
            )
        return true
    }

    override suspend fun getAllMessages(): List<FirebaseMessageEntity> =
        db.collection(firebaseAuthRepository.getUserId()!!)
            .document(FirebaseTables.MESSAGES_DOCUMENT)
            .collection(FirebaseTables.MESSAGES_COLLECTION)
            .get()
            .await()
            .map {
                it.toObject()
            }

    override suspend fun deleteMessage(message: MessagesEntity, timestamp: Long) {
        saveLastMessageChangesTimestamp(timestamp)
        db.collection(firebaseAuthRepository.getUserId()!!)
            .document(FirebaseTables.MESSAGES_DOCUMENT)
            .collection(FirebaseTables.MESSAGES_COLLECTION)
            .get()
            .addOnCompleteListener { snapshot ->
                if (snapshot.isSuccessful) {
                    snapshot.result.documents.forEach { document ->
                        if (document.data?.
                            get(FirebaseTables.MESSAGE_CREATED_TIMESTAMP_KEY).toString()
                            == message.createdTimeStamp.toString()
                        )
                            document.reference.delete()
                    }
                }
            }
    }

    override suspend fun getLastMessageChangesTimestamp(): Task<DocumentSnapshot> =
        db.collection(firebaseAuthRepository.getUserId()!!)
            .document(FirebaseTables.LAST_CHANGES_DOCUMENT)
            .get()

    override suspend fun saveLastMessageChangesTimestamp(timestamp: Long) {
        db.collection(firebaseAuthRepository.getUserId()!!)
            .document(FirebaseTables.LAST_CHANGES_DOCUMENT)
            .set(mapOf(
                Pair(FirebaseTables.LAST_MESSAGES_CHANGES_TIMESTAMP_KEY, timestamp)
            ), SetOptions.merge())
    }

    override suspend fun deleteAllMessages(): Any? =
        db.collection(firebaseAuthRepository.getUserId()!!)
            .document(FirebaseTables.MESSAGES_DOCUMENT)
            .delete()
            .await()

    override suspend fun saveMessages(messages: List<MessagesEntity>) {
        val mapOfMessages = mutableListOf<Map<String, Any>>()
        messages.forEach { message ->
            mapOfMessages.add(
                mapOf(
                    Pair(FirebaseTables.MESSAGE_CONTENT_KEY, message.content),
                    Pair(FirebaseTables.MESSAGE_IPHONE_KEY, message.isPhone),
                    Pair(FirebaseTables.MESSAGE_CREATED_TIMESTAMP_KEY, message.createdTimeStamp)
                )
            )
        }
        saveLastMessageChangesTimestamp(System.currentTimeMillis())
        db.collection(firebaseAuthRepository.getUserId()!!)
            .document(FirebaseTables.MESSAGES_DOCUMENT)
            .collection(FirebaseTables.MESSAGES_COLLECTION)
            .add(mapOfMessages)
            .await()
    }

}