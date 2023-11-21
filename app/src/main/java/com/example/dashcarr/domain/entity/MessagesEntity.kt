package com.example.dashcarr.domain.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "messages_entity")
data class MessagesEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val content: String,
    //TODO Use enum instead?
    val isPhone: Boolean,
    val createdTimeStamp: Long
) : Serializable