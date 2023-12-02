package com.example.dashcarr.data.mapper

import com.example.dashcarr.domain.entity.MessagesEntity
import com.example.dashcarr.domain.entity.firebase.FirebaseMessageEntity

/**
 * Extension function to convert a [FirebaseMessageEntity] to a [MessagesEntity].
 *
 * @return A [MessagesEntity] object with properties populated from the [FirebaseMessageEntity].
 */
fun FirebaseMessageEntity.toMessageEntity() =
    MessagesEntity(
        id = 0,
        content = content ?: "",
        isPhone = isPhone ?: false,
        createdTimeStamp = createdTimeStamp ?: 0
    )