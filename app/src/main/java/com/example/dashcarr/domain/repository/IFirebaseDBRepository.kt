package com.example.dashcarr.domain.repository

import com.example.dashcarr.data.entity.GeoPointEntity

/**
 * Interface for managing operations related to Firebase Realtime Database.
 */
interface IFirebaseDBRepository {

    suspend fun saveGeoPoint(geoPoint: GeoPointEntity): Boolean
    suspend fun getAllGeoPoints(): List<GeoPointEntity>
}