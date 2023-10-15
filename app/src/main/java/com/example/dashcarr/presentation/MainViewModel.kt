package com.example.dashcarr.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dashcarr.domain.repository.IFirebaseAuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for managing main screen logic.
 *
 * @param firebaseAuthRepository Repository for Firebase authentication operations.
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    private val firebaseAuthRepository: IFirebaseAuthRepository
) : ViewModel() {

    private val _isUserLoggedIn = Channel<Boolean>()
    val isUserLoggedIn = _isUserLoggedIn.receiveAsFlow()

    /**
     * Checks the authentication status and updates [isUserLoggedIn] accordingly.
     */
    private fun checkAuthentication() {
        viewModelScope.launch {
            if (firebaseAuthRepository.getUser() != null) _isUserLoggedIn.send(true)
        }
    }

    /**
     * Initialization block to check the authentication status when the ViewModel is created.
     */
    init {
        checkAuthentication()
    }
}