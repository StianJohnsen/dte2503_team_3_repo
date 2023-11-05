package com.example.dashcarr.presentation.tabs.settings.social_settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.dashcarr.data.database.dao.FriendsDao

class SocialSettingsViewModelFactory(
    private val dao: FriendsDao
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SocialSettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SocialSettingsViewModel(dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}