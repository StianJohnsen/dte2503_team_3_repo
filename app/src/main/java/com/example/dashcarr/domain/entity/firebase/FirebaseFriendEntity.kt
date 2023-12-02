package com.example.dashcarr.domain.entity.firebase

import java.io.Serializable

/**
 * Class representing a friend entity as stored in Firebase.
 * Implements Serializable for easy data passing and storage.
 *
 * @property name Optional name of the friend, nullable.
 * @property phone Optional phone number of the friend, nullable.
 * @property email Optional email address of the friend, nullable.
 * @property createdTimeStamp Optional timestamp indicating when the friend entity was created, nullable.
 */
class FirebaseFriendEntity(
    val name: String? = null,
    val phone: String? = null,
    val email: String? = null,
    val createdTimeStamp: Long? = null
): Serializable