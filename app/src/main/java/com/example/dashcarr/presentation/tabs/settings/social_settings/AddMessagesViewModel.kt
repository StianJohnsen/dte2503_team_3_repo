package com.example.dashcarr.presentation.tabs.settings.social_settings

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dashcarr.R
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
                        Toast.makeText(context, R.string.added_message, Toast.LENGTH_SHORT).show()
                    } else
                        Toast.makeText(context, R.string.error, Toast.LENGTH_SHORT).show()


                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, context.getString(R.string.error_input, e.message), Toast.LENGTH_SHORT)
                        .show()
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
                        Toast.makeText(context, context.getString(R.string.updated), Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, context.getString(R.string.error_updating), Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, context.getString(R.string.error_input, e.message), Toast.LENGTH_SHORT)
                        .show()
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
                        Toast.makeText(
                            context,
                            context.getString(R.string.message_deleted_successfully),
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(context, context.getString(R.string.error_delete_message), Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, context.getString(R.string.error_input, e.message), Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }
}