package com.example.dashcarr.data.mapper

import com.example.dashcarr.domain.entity.FriendsEntity
import com.example.dashcarr.domain.entity.firebase.FirebaseFriendEntity

/**
 * Extension function to convert a [FirebaseFriendEntity] to a [FriendsEntity].
 *
 * @return A [FriendsEntity] object with properties populated from the [FirebaseFriendEntity].
 */
fun FirebaseFriendEntity.toFriendEntity() =
    FriendsEntity(
        id = 0,
        name = name ?: "",
        email = email ?: "",
        phone = phone ?: "",
        createdTimeStamp = createdTimeStamp ?: 0
    )