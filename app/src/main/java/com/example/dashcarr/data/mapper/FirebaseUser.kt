package com.example.dashcarr.data.mapper

import com.example.dashcarr.domain.data.User
import com.google.firebase.auth.FirebaseUser

/**
 * Extension function to convert a [FirebaseUser] to a [User].
 *
 * @return A [User] object with properties populated from the [FirebaseUser].
 */
fun FirebaseUser.toUser() =
    User(name = displayName, email = email, uid = uid)