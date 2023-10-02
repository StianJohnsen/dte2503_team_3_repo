package com.example.dashcarr.domain.repository

import com.example.dashcarr.data.entity.GeoPointEntity

interface IFirebaseDBRepository {

    suspend fun saveGeoPoint(geoPoint: GeoPointEntity): Boolean
    suspend fun getAllGeoPoints(): List<GeoPointEntity>
}