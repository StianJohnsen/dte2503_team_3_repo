package com.example.dashcarr.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dashcarr.data.repository.UserPreferencesRepository
import com.example.dashcarr.domain.repository.IDatabasesSyncRepository
import com.example.dashcarr.domain.repository.IFirebaseAuthRepository
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
    private val userPreferencesRepository: UserPreferencesRepository,
    private val syncDatabasesRepository: IDatabasesSyncRepository
) : ViewModel() {

    private val _isUserLoggedIn = Channel<Boolean>()
    val isUserLoggedIn = _isUserLoggedIn.receiveAsFlow()

    val userState = firebaseAuthRepository.getUserChangeFlow()

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
        syncRemoteAndLocalDB()
        checkAuthentication()
        var powerSaveModeOn: Int
        runBlocking {
            powerSaveModeOn = userPreferencesRepository.appBoolFlow.first().isPowerSaveModeOn
        }
        PowerSavingMode.setAppPowerMode(PowerSavingMode.PowerState.values()[powerSaveModeOn])
    }

    fun syncRemoteAndLocalDB() {
        viewModelScope.launch {
            syncDatabasesRepository.syncDatabases()
        }
    }

}