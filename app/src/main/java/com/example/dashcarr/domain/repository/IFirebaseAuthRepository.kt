package com.example.dashcarr.domain.repository

import com.example.dashcarr.domain.data.User

/**
 * Interface for managing Firebase Authentication-related operations.
 */
interface IFirebaseAuthRepository {

    fun getUser(): User?

    suspend fun logOutUser()

    suspend fun getUserId(): String?

}