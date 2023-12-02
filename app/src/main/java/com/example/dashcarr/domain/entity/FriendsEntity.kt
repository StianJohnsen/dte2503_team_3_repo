package com.example.dashcarr.domain.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

/**
 * Data class representing a friend entity in the local database.
 * This class is marked as Serializable for easy data transmission and storage.
 * It is annotated as an Entity for Room database with the specified table name.
 *
 * @property id Unique identifier for the friend entity. It is the primary key and is auto-generated.
 * @property name Name of the friend.
 * @property phone Phone number of the friend.
 * @property email Email address of the friend.
 * @property createdTimeStamp Timestamp indicating when the friend entity was created.
 */
@Entity(tableName = "friends_entity")
data class FriendsEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val phone: String,
    val email: String,
    val createdTimeStamp: Long
) : Serializable
