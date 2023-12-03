package com.example.dashcarr.presentation.tabs.settings.social_settings

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dashcarr.data.database.AppDatabase
import com.example.dashcarr.data.repository.MessagesRepository
import com.example.dashcarr.domain.entity.MessagesEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * ViewModel for handling add, update, and delete operations for messages within the application's social settings.
 * This ViewModel utilizes [MessagesRepository] to interact with the underlying database and perform CRUD operations.
 */
@HiltViewModel
class AddMessagesViewModel @Inject constructor(
    private val messagesRepository: MessagesRepository
) : ViewModel() {

    fun addToDatabase(context: Context, messageContent: String) {
        val newMessage =
            MessagesEntity(
                content = messageContent,
                isPhone = true,
                createdTimeStamp = System.currentTimeMillis()
            )

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val db = AppDatabase.getInstance(context)
                val newMessageRowId = db.MessagesDao().insert(newMessage)
                withContext(Dispatchers.Main) {
                    if (newMessageRowId > 0) {
                        Toast.makeText(context, "Added Message", Toast.LENGTH_SHORT).show()
                    } else
                        Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show()


                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

    }

    fun getMessageById(id: Int): LiveData<MessagesEntity> {
        return messagesRepository.getMessageById(id)
    }

    fun updateMessage(context: Context, id: Int, content: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val updatedFriend = MessagesEntity(
                    id = id.toLong(),
                    content = content,
                    isPhone = false,
                    createdTimeStamp = System.currentTimeMillis()
                )
                val result = messagesRepository.updateMessage(updatedFriend)
                withContext(Dispatchers.Main) {
                    if (result.isSuccess) {
                        Toast.makeText(context, "Updated", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Error updating", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun deleteMessage(context: Context, id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val messageToDelete = MessagesEntity(
                    id = id.toLong(),
                    content = "",
                    isPhone = false,
                    createdTimeStamp = System.currentTimeMillis()
                )
                val result = messagesRepository.deleteMessage(messageToDelete)
                withContext(Dispatchers.Main) {
                    if (result.isSuccess) {
                        Toast.makeText(context, "Message deleted", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Error deleting Message", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}