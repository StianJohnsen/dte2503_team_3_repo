package com.example.dashcarr.data.repository

import com.example.dashcarr.data.entity.GeoPointEntity
import com.example.dashcarr.data.repository.model.GeoPointTable
import com.example.dashcarr.domain.repository.IFirebaseAuthRepository
import com.example.dashcarr.domain.repository.IFirebaseDBRepository
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import javax.inject.Inject


class FirebaseDBRepository @Inject constructor(
    private val firebaseAuthRepository: IFirebaseAuthRepository
): IFirebaseDBRepository {

    private val db = Firebase.firestore

    override suspend fun saveGeoPoint(geoPoint: GeoPointEntity): Boolean {
        db.collection(firebaseAuthRepository.getUserId()!!)
            .document(GeoPointTable.GEO_POINT_DOCUMENT)
            .collection(GeoPointTable.GEO_POINT_COLLECTION)
            .add(
                mapOf(
                    Pair(GeoPointTable.GEO_POINT_ID_KEY, geoPoint.geoPointId),
                    Pair(GeoPointTable.TRIP_ID_KEY, geoPoint.tripId),
                    Pair(GeoPointTable.LATITUDE_KEY, geoPoint.latitude),
                    Pair(GeoPointTable.LONGITUDE_KEY, geoPoint.longitude),
                    Pair(GeoPointTable.STEP_NUM_KEY, geoPoint.stepNum)
                )
            )
            .await()
        return true
    }


    override suspend fun getAllGeoPoints(): List<GeoPointEntity> =
        db.collection(firebaseAuthRepository.getUserId()!!)
            .document(GeoPointTable.GEO_POINT_DOCUMENT)
            .collection(GeoPointTable.GEO_POINT_COLLECTION)
            .get()
            .await()
            .map {
                it.toObject()
            }

}