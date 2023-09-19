package com.example.dashcarr.domain.repository

import com.example.dashcarr.data.data.User


interface IFirebaseAuthRepository {

    fun getUser(): User?

    suspend fun logOutUser()

    suspend fun getUserId(): String?

}