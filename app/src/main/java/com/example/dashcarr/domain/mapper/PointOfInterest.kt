package com.example.dashcarr.domain.mapper

import com.example.dashcarr.domain.entity.PointOfInterestEntity
import com.example.dashcarr.presentation.tabs.map.data.PointOfInterest

fun PointOfInterest.toEntity() =
    PointOfInterestEntity(
        latitude = latitude,
        longitude = longitude,
        name = name
    )