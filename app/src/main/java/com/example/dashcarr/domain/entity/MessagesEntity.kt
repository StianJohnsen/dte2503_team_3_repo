package com.example.dashcarr.domain.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

/**
 * Data class representing a message entity in the local database.
 * This class is marked as Serializable for ease of data transmission and storage.
 * Annotated as an Entity for Room database, it maps to the specified table name.
 *
 * @property id Unique identifier for the message entity. It is the primary key and is auto-generated.
 * @property content The textual content of the message.
 * @property isPhone Flag indicating whether the message was sent from a phone.
 * @property createdTimeStamp Timestamp indicating when the message was created.
 */
@Entity(tableName = "messages_entity")
data class MessagesEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val content: String,
    val isPhone: Boolean,
    val createdTimeStamp: Long
) : Serializable