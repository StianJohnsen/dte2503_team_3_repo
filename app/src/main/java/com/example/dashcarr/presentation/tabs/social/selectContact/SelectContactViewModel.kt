package com.example.dashcarr.presentation.tabs.social.selectContact

import androidx.lifecycle.ViewModel
import com.example.dashcarr.domain.repository.IFriendsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * ViewModel for the Select Contact feature.
 *
 * This ViewModel provides data related to selecting contacts for messaging.
 *
 * @param friendsRepository The repository for managing friends' data.
 * @constructor Creates a new instance of [SelectContactViewModel].
 */
@HiltViewModel
class SelectContactViewModel @Inject constructor(
    friendsRepository: IFriendsRepository
) : ViewModel() {

    val friendsList = friendsRepository.getAllFriendsLiveData()

}