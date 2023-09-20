package com.example.dashcarr.domain.repository

interface IFirebaseDBRepository {

    suspend fun saveAverageSpeed(speed: Int)
    suspend fun getAverageSpeed(): Int
}