package com.example.dashcarr.presentation.tabs.settings.social_settings

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dashcarr.data.database.AppDatabase
import com.example.dashcarr.domain.entity.FriendsEntity
import com.example.dashcarr.domain.entity.MessagesEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SocialSettingsViewModel : ViewModel() {
    fun addToDatabase(context: Context) {
        val newFriend =
            FriendsEntity(
                name = "bob",
                phone = "00000000",
                email = "test@gmail.com",
                createdTimeStamp = System.currentTimeMillis()
            )

        val newMessage =
            MessagesEntity(
                content = "This is a message"
            )

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val db = AppDatabase.getInstance(context)
                val newMessageRowId = db.MessagesDao().insert(newMessage)
                val newFriendRowId = db.FriendsDao().insert(newFriend)
                withContext(Dispatchers.Main) {
                    if (newFriendRowId > 0) {
                        Toast.makeText(context, "Added", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show()
                    }
                    if (newMessageRowId > 0) {
                        Toast.makeText(context, "Added", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show()
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