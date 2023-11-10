package com.example.dashcarr.presentation.tabs.settings.social_settings

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dashcarr.data.database.AppDatabase
import com.example.dashcarr.data.database.dao.MessagesDao
import com.example.dashcarr.domain.entity.MessagesEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AddMessagesViewModel(private val messagesDao: MessagesDao) : ViewModel() {

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
            } catch (E: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error: ${E.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

    }
}