package com.example.dashcarr.domain.mapper

import com.example.dashcarr.domain.entity.PointOfInterestEntity
import com.example.dashcarr.presentation.tabs.map.data.PointOfInterest

/**
 * Converts a [PointOfInterest] object to its corresponding [PointOfInterestEntity].
 *
 * @return The [PointOfInterestEntity] representation of the current [PointOfInterest].
 */
fun PointOfInterest.toEntity() =
    PointOfInterestEntity(
        latitude = latitude,
        longitude = longitude,
        name = name,
        createdTimeStamp = createdTimeStamp
    )