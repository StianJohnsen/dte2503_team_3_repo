package com.example.dashcarr.domain.data

/**
 * Represents user information.
 *
 * @property name The name of the user.
 * @property email The email address of the user.
 * @property uid The unique identifier (UID) of the user.
 */
data class User(
    val name: String?,
    val email: String?,
    val uid: String?
)