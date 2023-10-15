package com.example.dashcarr.presentation.authentication.login

import android.util.Log
import android.util.Patterns
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dashcarr.R
import com.example.dashcarr.presentation.authentication.login.loginUser.LoginUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for handling user authentication in the login process.
 *
 * @constructor Injected constructor for Hilt DI.
 */
@HiltViewModel
class LoginViewModel @Inject constructor(

): ViewModel() {

    // Flow for emitting email error state
    @StringRes
    private val _emailErrorState = MutableSharedFlow<Int?>()
    val emailErrorState = _emailErrorState.asSharedFlow()

    // Flow for emitting password error state
    @StringRes
    private val _passwordErrorState = MutableSharedFlow<Int?>()
    val passwordErrorState = _passwordErrorState.asSharedFlow()

    // Flow for emitting login success or failure state
    private val _loginState = MutableSharedFlow<Boolean>()
    val loginState = _loginState.asSharedFlow()

    // Flow for initiating Google login
    private val _googleLoginState = MutableSharedFlow<Unit>()
    val googleLoginState = _googleLoginState.asSharedFlow()

    /**
     * Updates the email error state based on the validity of the email.
     *
     * @param email The user's email address.
     */
    fun updateEmail(email: String) {
        viewModelScope.launch {
            if (!isValidEmail(email)) {
                _emailErrorState.emit(R.string.error_invalid_email)
            } else {
                _emailErrorState.emit(null)
            }
        }
    }

    /**
     * Updates the password error state based on the validity of the password.
     *
     * @param password The user's password.
     */
    fun updatePassword(password: String) {
        viewModelScope.launch {
            isValidPassword(password)
        }
    }

    /**
     * Checks if the provided email is a valid email address.
     *
     * @param email The user's email address.
     * @return `true` if the email is valid, `false` otherwise.
     */
    private fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    /**
     * Checks if the provided password is valid based on specific criteria.
     *
     * @param password The user's password.
     * @return `true` if the password is valid, `false` otherwise.
     */
    private suspend fun isValidPassword(password: String): Boolean {
        if (password.length < 8) {
            _passwordErrorState.emit(R.string.error_too_short_password)
            return false
        }
        var containsUpperCase = false
        password.forEach {
            if (it.isUpperCase()) containsUpperCase = true
        }
        if (!containsUpperCase) {
            _passwordErrorState.emit(R.string.error_non_upper_case)
            return false
        }

        if (password.all { !it.isDigit() }) {
            _passwordErrorState.emit(R.string.error_non_digit)
            return false
        }

        _passwordErrorState.emit(null)
        return true
    }

    /**
     * Initiates the sign-in process with the provided email and password.
     *
     * @param email The user's email address.
     * @param password The user's password.
     */
    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            if (isValidEmail(email) && isValidPassword(password))
                logIn(LoginUser(email, password))
        }
    }

    /**
     * Performs the actual sign-in using Firebase Authentication.
     *
     * @param user The user's login credentials.
     */
    private fun logIn(user: LoginUser) {
        Firebase.auth.signInWithEmailAndPassword(user.email, user.password)
            .addOnCompleteListener { task ->
                viewModelScope.launch {
                    if (task.isSuccessful) {
                        _loginState.emit(true)
                        Log.e("WatchingSomeStuff", "Success Login!")
                    } else {
                        _loginState.emit(false)
                        Log.e("WatchingSomeStuff", "Failed Login!")
                    }
                }
            }
    }

    /**
     * Initiates the Google login process.
     */
    fun showGoogleLogin() {
        viewModelScope.launch {
            _googleLoginState.emit(Unit)
        }
    }
}