package com.example.dashcarr.presentation.tabs.social.selectContact

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dashcarr.data.database.AppDatabase
import com.example.dashcarr.data.database.dao.FriendsDao
import com.example.dashcarr.domain.entity.FriendsEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SelectContactViewModel(friendsDao: FriendsDao) : ViewModel() {

    private var _friendsList = MutableLiveData<List<FriendsEntity>>()
    val friendsList: LiveData<List<FriendsEntity>> = _friendsList

    fun getAllFriends(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getInstance(context)
            _friendsList.postValue(db.FriendsDao().getAllFriends())
        }
    }

}