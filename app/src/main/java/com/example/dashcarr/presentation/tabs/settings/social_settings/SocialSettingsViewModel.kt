package com.example.dashcarr.presentation.tabs.settings.social_settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dashcarr.data.repository.SentMessagesRepository
import com.example.dashcarr.domain.entity.FriendsEntity
import com.example.dashcarr.domain.entity.SentMessagesEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SocialSettingsViewModel @Inject constructor(
    private val sentMessagesRepository: SentMessagesRepository
    ): ViewModel() {


    // LiveData to observe sent messages
    val sentMessages = sentMessagesRepository.getAllSentMessagesLiveData()
    private var currentMessage : SentMessagesEntity? = null

    private val _showDeleteDialog = MutableSharedFlow<Boolean>()
    val showDeleteDialog = _showDeleteDialog.asSharedFlow()

    private val _onDeleteMessageSuccess = MutableSharedFlow<Unit>()
    val onDeleteMessageSuccess = _onDeleteMessageSuccess.asSharedFlow()
    private val _onDeleteMessageFailure = MutableSharedFlow<Throwable>()
    val onDeleteMessageFailure = _onDeleteMessageFailure.asSharedFlow()

    /**
     * Function to delete a sent message.
     *
     * @param message The message to delete.
     */

    fun deleteMessage() {
        currentMessage?.let {
            viewModelScope.launch {
                val result = sentMessagesRepository.deleteSentMessage(currentMessage!!)
                _showDeleteDialog.emit(false)
                result.onSuccess {
                    _onDeleteMessageSuccess.emit(Unit)
                }
                result.onFailure {
                    _onDeleteMessageFailure.emit(it)
                }
            }
        }
    }

    /**
     * Show a confirmation dialog for deleting a message.
     *
     * @param message The message to be deleted.
     */
    fun showConfirmDeleteDialog(message: SentMessagesEntity) {
        viewModelScope.launch {
            currentMessage = message
            _showDeleteDialog.emit(true)
        }
    }

    /**
     * Hide the delete confirmation dialog.
     */
    fun hideDeleteDialog() {
        viewModelScope.launch {
            _showDeleteDialog.emit(false)
        }
    }

    fun hasPhoneNumber(friend: FriendsEntity): Boolean {
        return friend.phone.isNotEmpty()
    }

    fun hasEmail(friend: FriendsEntity): Boolean {
        return friend.email.isNotEmpty()
    }
}