package com.example.dashcarr.domain.entity.firebase

import java.io.Serializable

/**
 * Class representing a message entity as stored in Firebase.
 * This class is marked as Serializable to facilitate easy data transmission and storage.
 *
 * @property content Optional content of the message, nullable.
 * @property isPhone Optional flag indicating if the message was sent from a phone, nullable.
 * @property createdTimeStamp Optional timestamp indicating when the message was created, nullable.
 */
class FirebaseMessageEntity(
    val content: String? = null,
    val isPhone: Boolean? = null,
    val createdTimeStamp: Long? = null
    ): Serializable