package com.example.dashcarr.presentation.authentication

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dashcarr.R
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

    fun updateEmail(email: String) {
        viewModelScope.launch {
            if (email.isEmpty()) {
                _emailErrorState.emit(R.string.error_email_empty)
            }
        }
    }
}