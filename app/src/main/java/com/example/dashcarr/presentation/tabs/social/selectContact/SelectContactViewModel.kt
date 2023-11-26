package com.example.dashcarr.presentation.tabs.social.selectContact

import androidx.lifecycle.ViewModel
import com.example.dashcarr.domain.repository.IFriendsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SelectContactViewModel @Inject constructor(
    private val friendsRepository: IFriendsRepository
) : ViewModel() {

    val friendsList = friendsRepository.getAllFriendsLiveData()

}