package com.example.dashcarr.data.repository

import android.util.Log
import com.example.dashcarr.domain.repository.IFirebaseAuthRepository
import com.example.dashcarr.domain.repository.IFirebaseDBRepository
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import javax.inject.Inject

private const val GEO_POINT_TABLE_NAME = "GeoPoint"

class FirebaseDBRepository @Inject constructor(
    private val firebaseAuthRepository: IFirebaseAuthRepository
): IFirebaseDBRepository {

    private val db = Firebase.firestore
    override suspend fun saveAverageSpeed(speed: Int) {
        db.collection(firebaseAuthRepository.getUserId()!!.plus(GEO_POINT_TABLE_NAME))
            .add(Pair("latitude" to 15,5))
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.e("WatchingSomeStuff", "Save to DB is Successfult!")
                }
                else {

                    Log.e("WatchingSomeStuff", "Save to DB is FAILURE!@!")
                }

            }
    }

    override suspend fun getAverageSpeed(): Int {
        TODO("Not yet implemented")
    }

}