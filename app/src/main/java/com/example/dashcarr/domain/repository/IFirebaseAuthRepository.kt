package com.example.dashcarr.domain.repository

import com.example.dashcarr.domain.repository.data.User


interface IFirebaseAuthRepository {

    fun getUser(): User?

    suspend fun logOutUser()

    suspend fun getUserId(): String?

}