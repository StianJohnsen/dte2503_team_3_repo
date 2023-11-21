package com.example.dashcarr.domain.entity.firebase

import java.io.Serializable

class FirebaseMessageEntity(
    val content: String? = null,
    val isPhone: Boolean? = null,
    val createdTimeStamp: Long? = null
    ): Serializable