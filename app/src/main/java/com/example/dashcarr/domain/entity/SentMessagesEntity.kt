package com.example.dashcarr.domain.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "sent_messages")
data class SentMessagesEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "messageId") val messageId: Long,
    @ColumnInfo(name = "friendId") val friendId: Long
) : Serializable

