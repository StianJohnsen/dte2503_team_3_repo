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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class AddMessagesViewModel @Inject constructor(
    private val messagesRepository: MessagesRepository
) : ViewModel() {

    private val saveResult = MutableStateFlow<Result<Unit>?>(null)

    fun saveNewMessage(message: MessagesEntity) {
        viewModelScope.launch {
            val result = messagesRepository.saveNewMessage(message)
            saveResult.value = result
        }
    }

    fun addToDatabase(context: Context, messageContent: String) {
        val newMessage =
            MessagesEntity(
                content = messageContent,
                isPhone = true
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
        return messagesDao.getMesssageById(id)
    }

    fun updateMessage(context: Context, id: Int, content: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val updatedFriend = MessagesEntity(
                    id = id.toLong(),
                    content = content,
                    isPhone = false
                )
                val updateCount = messagesDao.update(updatedFriend)
                withContext(Dispatchers.Main) {
                    if (updateCount > 0) {
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
                val numberOfRowsDeleted = messagesDao.deleteById(id)
                withContext(Dispatchers.Main) {
                    if (numberOfRowsDeleted > 0) {
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