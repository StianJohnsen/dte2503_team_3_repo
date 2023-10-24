package com.example.dashcarr.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.dashcarr.domain.repository.IFirebaseAuthRepository
import com.example.dashcarr.presentation.tabs.map.UserPreferencesRepository
import com.example.dashcarr.presentation.tabs.settings.PowerSavingMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

/**
 * ViewModel for managing main screen logic.
 *
 * @param firebaseAuthRepository Repository for Firebase authentication operations.
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    private val firebaseAuthRepository: IFirebaseAuthRepository,
    val userPreferencesRepository: UserPreferencesRepository
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
        var powerSaveModeOn: Boolean
        runBlocking {
            powerSaveModeOn = userPreferencesRepository.appBoolFlow.first().isPowerSaveModeOn
        }
        PowerSavingMode.setAppPowerMode(if(powerSaveModeOn) PowerSavingMode.PowerState.ON else PowerSavingMode.PowerState.AUTO)
    }
}