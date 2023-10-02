package com.example.dashcarr.data.mapper

import com.example.dashcarr.domain.data.User
import com.google.firebase.auth.FirebaseUser

fun FirebaseUser.toUser() =
    User(name = displayName, email = email, uid = uid)