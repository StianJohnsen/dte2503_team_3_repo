package com.example.dashcarr.data.repository

import com.example.dashcarr.data.mapper.toUser
import com.example.dashcarr.domain.data.User
import com.example.dashcarr.domain.repository.IFirebaseAuthRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

/**
 * Implementation of [IFirebaseAuthRepository] for managing Firebase Authentication.
 */
class FirebaseAuthRepository @Inject constructor(): IFirebaseAuthRepository {


    private val auth by lazy { FirebaseAuth.getInstance() }

    override fun getUserChangeFlow(): Flow<User?> = callbackFlow {
        val authListener = FirebaseAuth.AuthStateListener {
            trySendBlocking(it.currentUser?.toUser())
        }
        auth.addAuthStateListener(authListener)

        awaitClose {
            auth.removeAuthStateListener(authListener)
        }
    }

    override fun getUser(): User? {
        return auth.currentUser?.toUser()
    }

    override suspend fun logOutUser() {
        auth.signOut()
    }

    override suspend fun getUserId(): String? {
        return auth.currentUser?.uid
    }

}