package com.example.dashcarr.domain.repository

/**
 * Interface for clearing all data from local database tables.
 * Defines a method for clearing all stored data in the local database.
 */
interface IClearLocalDatabaseRepository {

    suspend fun clearAllDatabaseTables()
}