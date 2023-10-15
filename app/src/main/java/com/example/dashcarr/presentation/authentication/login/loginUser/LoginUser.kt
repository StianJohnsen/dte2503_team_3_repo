package com.example.dashcarr.presentation.authentication.login.loginUser

/**
 * Data class representing the credentials for user login.
 *
 * @param email The email address of the user.
 * @param password The password associated with the user's account.
 */
data class LoginUser (
    val email: String,
    val password: String
)