package com.example.dashcarr.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dashcarr.domain.repository.IFirebaseAuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val firebaseAuthRepository: IFirebaseAuthRepository
) : ViewModel() {

    private val _isUserLoggedIn = Channel<Boolean>()
    val isUserLoggedIn = _isUserLoggedIn.receiveAsFlow()
    
    private fun checkAuthentication() {
        viewModelScope.launch {
            if (firebaseAuthRepository.getUser() != null) _isUserLoggedIn.send(true)
        }
    }

    init {
        checkAuthentication()
    }
}