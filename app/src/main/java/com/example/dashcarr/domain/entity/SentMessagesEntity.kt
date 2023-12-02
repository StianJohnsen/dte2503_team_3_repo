package com.example.dashcarr.domain.entity

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation
import java.io.Serializable

/**
 * Data class representing the final structure of a sent message, combining the sent message details,
 * associated message, and friend entity.
 *
 * @property sentMessage The embedded SentMessagesEntity representing the basic details of the sent message.
 * @property message The related MessagesEntity providing content details of the message.
 * @property friend An optional related FriendsEntity representing the friend associated with the message.
 */
data class SentMessageFinalEntity(
    @Embedded val sentMessage: SentMessagesEntity,
    @Relation(
        parentColumn = "messageId",
        entityColumn = "id",
        entity = MessagesEntity::class
    ) val message: MessagesEntity,
    @Relation(
        parentColumn = "friendId",
        entityColumn = "id",
        entity = FriendsEntity::class
    ) val friend: FriendsEntity?
)

@Entity(tableName = "sent_messages")
data class SentMessagesEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "messageId") val messageId: Long,
    @ColumnInfo(name = "friendId") val friendId: Int,
    @ColumnInfo("createdTimeStamp") val createdTimeStamp: Long
) : Serializable
