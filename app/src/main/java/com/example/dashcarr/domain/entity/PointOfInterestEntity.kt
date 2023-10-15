package com.example.dashcarr.domain.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

/**
 * Represents a point of interest.
 *
 * @property id The unique identifier for the point of interest (auto-generated).
 * @property name The name or title of the point of interest.
 * @property latitude The latitude coordinate of the point of interest.
 * @property longitude The longitude coordinate of the point of interest.
 * @property createdTimeStamp The timestamp indicating when the point of interest was created.
 */
@Entity(tableName = "points_of_interest")
data class PointOfInterestEntity (
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val createdTimeStamp: Long,
): Serializable