package com.example.dashcarr.presentation.tabs.social.selectContact

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dashcarr.domain.entity.FriendsEntity
import com.example.dashcarr.domain.repository.IFriendsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SelectContactViewModel @Inject constructor(
    private val friendsRepository: IFriendsRepository
) : ViewModel() {

    private var _friendsList = MutableLiveData<List<FriendsEntity>>()
    val friendsList: LiveData<List<FriendsEntity>> = _friendsList

    fun getAllFriends(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val db = friendsRepository.getAllFriends()
            _friendsList.postValue(friendsRepository.getAllFriends())
        }
    }

}