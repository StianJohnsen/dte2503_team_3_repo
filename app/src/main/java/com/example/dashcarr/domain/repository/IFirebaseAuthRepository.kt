package com.example.dashcarr.domain.repository

import com.example.dashcarr.domain.data.User
import kotlinx.coroutines.flow.Flow

/**
 * Interface for managing Firebase Authentication-related operations.
 */
interface IFirebaseAuthRepository {

    fun getUserChangeFlow(): Flow<User?>

    fun getUser(): User?

    suspend fun logOutUser()

    suspend fun getUserId(): String?

}