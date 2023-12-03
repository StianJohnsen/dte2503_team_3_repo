package com.example.dashcarr.presentation.tabs.social.selectMessage

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dashcarr.domain.entity.FriendsEntity
import com.example.dashcarr.domain.entity.SentMessagesEntity
import com.example.dashcarr.domain.repository.IFriendsRepository
import com.example.dashcarr.domain.repository.IMessagesRepository
import com.example.dashcarr.domain.repository.ISentMessagesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the SelectMessageFragment. Handles data operations related to selecting and sending messages.
 *
 * @param messagesRepository Repository for accessing message data.
 * @param friendsRepository Repository for accessing friends data.
 * @param sentMessagesRepository Repository for managing sent messages.
 */
@HiltViewModel
class SelectMessageViewModel @Inject constructor(
    private val messagesRepository: IMessagesRepository,
    private val friendsRepository: IFriendsRepository,
    private val sentMessagesRepository: ISentMessagesRepository
) : ViewModel() {

    val messagesList = messagesRepository.getAllMessagesLiveData()

    private var _contactsList = MutableLiveData<FriendsEntity>()
    val contactsList: LiveData<FriendsEntity> = _contactsList

    fun getContact(id: Int?) {
        if (id == null) return
        viewModelScope.launch(Dispatchers.IO) {
            if (_contactsList.value != null) return@launch
            _contactsList.postValue(friendsRepository.getFriendById(id).value)
        }
    }

    fun insertIntoSentMessages(sentMessagesEntity: SentMessagesEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            sentMessagesRepository.saveNewSentMessage(sentMessagesEntity)
        }
    }
}