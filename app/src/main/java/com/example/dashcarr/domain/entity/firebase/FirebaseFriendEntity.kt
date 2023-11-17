package com.example.dashcarr.domain.entity.firebase

import java.io.Serializable

class FirebaseFriendEntity(
    val name: String? = null,
    val phone: String? = null,
    val email: String? = null,
    val createdTimeStamp: Long? = null
): Serializable