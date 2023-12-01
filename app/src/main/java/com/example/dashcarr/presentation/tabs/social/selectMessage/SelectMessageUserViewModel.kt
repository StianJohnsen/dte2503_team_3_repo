package com.example.dashcarr.presentation.tabs.social.selectMessage

import androidx.lifecycle.ViewModel
import com.example.dashcarr.domain.repository.IFirebaseAuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SelectMessageUserViewModel @Inject constructor(
    private val firebaseAuthRepository: IFirebaseAuthRepository
) : ViewModel() {
    fun getUser() = firebaseAuthRepository.getUser()
}