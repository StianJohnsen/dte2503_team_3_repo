package com.example.dashcarr.domain.repository

/**
 * Interface for synchronizing local and remote databases.
 * Provides a method to synchronize data across different data sources.
 */
interface IDatabasesSyncRepository {

    suspend fun syncDatabases()

}