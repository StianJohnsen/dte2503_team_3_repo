package com.example.dashcarr.data.repository

import com.example.dashcarr.data.mapper.toUser
import com.example.dashcarr.domain.repository.IFirebaseAuthRepository
import com.example.dashcarr.data.data.User
import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject

class FirebaseAuthRepository @Inject constructor(): IFirebaseAuthRepository {

    private val auth by lazy { FirebaseAuth.getInstance() }

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