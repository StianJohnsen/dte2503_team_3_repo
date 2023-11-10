package com.example.dashcarr.presentation.tabs.social.selectContact

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.dashcarr.data.database.dao.FriendsDao

class SelectContactViewModelFactory(private val dao: FriendsDao) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SelectContactViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SelectContactViewModel(dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

}