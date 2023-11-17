package com.example.dashcarr.domain.repository

interface IDatabasesSyncRepository {

    suspend fun syncDatabases()

}