package com.example.dashcarr.presentation.tabs.social.selectMessage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.dashcarr.data.database.dao.MessagesDao

class SelectMessageViewModelFactory(private val dao: MessagesDao) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SelectMessageViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SelectMessageViewModel(dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}