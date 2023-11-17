package com.example.dashcarr.data.mapper

import com.example.dashcarr.domain.entity.FriendsEntity
import com.example.dashcarr.domain.entity.firebase.FirebaseFriendEntity

fun FirebaseFriendEntity.toFriendEntity() =
    FriendsEntity(
        id = 0,
        name = name ?: "",
        email = email ?: "",
        phone = phone ?: "",
        createdTimeStamp = createdTimeStamp ?: 0
    )