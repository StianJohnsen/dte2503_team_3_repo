package com.example.dashcarr.domain.repository

interface IClearLocalDatabaseRepository {

    suspend fun clearAllDatabaseTables()
}