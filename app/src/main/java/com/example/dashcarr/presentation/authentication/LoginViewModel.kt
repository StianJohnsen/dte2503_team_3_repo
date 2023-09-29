package com.example.dashcarr.presentation.authentication

import android.util.Log
import android.util.Patterns
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dashcarr.R
import com.example.dashcarr.domain.data.User
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(

): ViewModel() {

    @StringRes
    private val _emailErrorState = MutableSharedFlow<Int?>()
    val emailErrorState = _emailErrorState.asSharedFlow()

    @StringRes
    private val _passwordErrorState = MutableSharedFlow<Int?>()
    val passwordErrorState = _passwordErrorState.asSharedFlow()

    private val _loginState = MutableSharedFlow<Boolean>()
    val loginState = _loginState.asSharedFlow()

    private val _googleLoginState = MutableSharedFlow<Unit>()
    val googleLoginState = _googleLoginState.asSharedFlow()


    fun updateEmail(email: String) {
        viewModelScope.launch {
            if (!isValidEmail(email)) {
                _emailErrorState.emit(R.string.error_invalid_email)
            }
            else _emailErrorState.emit(null)
        }
    }

    fun updatePassword(password: String) {
        viewModelScope.launch {
            isValidPassword(password)
        }
    }


    private fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

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

     suspend fun validateFields(email: String, password: String): Boolean {
        var isValid = true

        if (!isValidEmail(email)) {
            isValid = false
            _passwordErrorState.emit(R.string.error_invalid_email)
        } else {
            _passwordErrorState.emit(null)
        }

        if (!isValidPassword(password)) {
            isValid = false
            _passwordErrorState.emit(R.string.error_invalid_password)
        } else {
            _passwordErrorState.emit(null)
        }

        return isValid
    }

    fun signIn(email: String,password: String) {
        viewModelScope.launch {
            if(isValidEmail(email) && isValidPassword(password))
                logIn(User(email, password))
        }
    }

    private fun logIn(user: User) {
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

    fun showGoogleLogin() {
        viewModelScope.launch {
            _googleLoginState.emit(Unit)
        }
    }

}
