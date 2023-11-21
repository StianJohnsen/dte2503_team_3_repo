package com.example.dashcarr.data.repository

import com.example.dashcarr.data.constants.FirebaseTables
import com.example.dashcarr.domain.entity.FriendsEntity
import com.example.dashcarr.domain.entity.firebase.FirebaseFriendEntity
import com.example.dashcarr.domain.entity.firebase.GeoPointEntity
import com.example.dashcarr.domain.repository.IFirebaseAuthRepository
import com.example.dashcarr.domain.repository.IFirebaseDBRepository
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import javax.inject.Inject


/**
 * Implementation of [IFirebaseDBRepository] for managing interactions with Firebase.
 *
 * @param firebaseAuthRepository The repository for managing Firebase Authentication.
 */
class FirebaseDBRepository @Inject constructor(
    private val firebaseAuthRepository: IFirebaseAuthRepository
): IFirebaseDBRepository {

    private val db = Firebase.firestore

    override suspend fun saveGeoPoint(geoPoint: GeoPointEntity): Boolean {
        db.collection(firebaseAuthRepository.getUserId()!!)
            .document(FirebaseTables.GEO_POINT_DOCUMENT)
            .collection(FirebaseTables.GEO_POINT_COLLECTION)
            .add(
                mapOf(
                    Pair(FirebaseTables.GEO_POINT_ID_KEY, geoPoint.geoPointId),
                    Pair(FirebaseTables.TRIP_ID_KEY, geoPoint.tripId),
                    Pair(FirebaseTables.LATITUDE_KEY, geoPoint.latitude),
                    Pair(FirebaseTables.LONGITUDE_KEY, geoPoint.longitude),
                    Pair(FirebaseTables.STEP_NUM_KEY, geoPoint.stepNum)
                )
            )
            .await()
        return true
    }


    override suspend fun getAllGeoPoints(): List<GeoPointEntity> =
        db.collection(firebaseAuthRepository.getUserId()!!)
            .document(FirebaseTables.GEO_POINT_DOCUMENT)
            .collection(FirebaseTables.GEO_POINT_COLLECTION)
            .get()
            .await()
            .map {
                it.toObject()
            }

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

    override suspend fun updateFriend(friend: FriendsEntity): Result<Unit> {
        TODO("Not yet implemented")
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

    override suspend fun deleteAllFriends(): Any =
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

            ))
    }
}