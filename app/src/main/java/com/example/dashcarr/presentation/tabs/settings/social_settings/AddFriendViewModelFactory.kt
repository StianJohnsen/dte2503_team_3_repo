package com.example.dashcarr.presentation.tabs.settings.social_settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.dashcarr.data.database.dao.FriendsDao

class AddFriendViewModelFactory(private val dao: FriendsDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddFriendViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AddFriendViewModel(dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}