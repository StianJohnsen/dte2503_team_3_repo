package com.example.dashcarr.presentation.tabs.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dashcarr.data.repository.FirebaseAuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(

) : ViewModel() {

    private val _logOutState = MutableSharedFlow<Unit>()
    val logOutState = _logOutState.asSharedFlow()
    fun logOut() {
        viewModelScope.launch {
            FirebaseAuthRepository().logOutUser()
            _logOutState.emit(Unit)
        }
    }
}