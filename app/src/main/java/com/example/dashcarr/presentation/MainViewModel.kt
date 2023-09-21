package com.example.dashcarr.presentation

import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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
    
    fun checkAuthentication() {
        Log.e("WatchingSomeStuff", "ChecKAuth")
        Log.e("WatchingSomeStuff", "GetUser = ${firebaseAuthRepository.getUser()}")
        viewModelScope.launch {
            if (firebaseAuthRepository.getUser() == null) _isUserLoggedIn.send(false)
        }
    }
}