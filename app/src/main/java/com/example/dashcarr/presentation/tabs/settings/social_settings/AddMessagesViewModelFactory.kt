package com.example.dashcarr.presentation.tabs.settings.social_settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.dashcarr.data.database.dao.MessagesDao

class AddMessagesViewModelFactory(private val dao: MessagesDao) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddMessagesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AddMessagesViewModel(dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}