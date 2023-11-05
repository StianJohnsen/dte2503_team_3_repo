package com.example.dashcarr.presentation.tabs.settings.social_settings

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dashcarr.data.database.AppDatabase
import com.example.dashcarr.data.database.dao.FriendsDao
import com.example.dashcarr.domain.entity.FriendsEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AddFriendViewModel(private val dao: FriendsDao) : ViewModel() {
    fun addToDatabase(context: Context, name: String, email: String, phone: String) {
        val newFriend =
            FriendsEntity(
                name = name,
                phone = phone,
                email = email,
                createdTimeStamp = System.currentTimeMillis()
            )

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val db = AppDatabase.getInstance(context)
                val newFriendRowId = db.FriendsDao().insert(newFriend)
                withContext(Dispatchers.Main) {
                    if (newFriendRowId > 0) {
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

    fun getFriendById(id: Int): LiveData<FriendsEntity> {
        return dao.getFriendById(id)
    }

    fun updateFriend(context: Context, id: Int, name: String, email: String, phone: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val updatedFriend = FriendsEntity(
                    id = id,
                    name = name,
                    phone = phone,
                    email = email,
                    createdTimeStamp = System.currentTimeMillis()
                )
                val updateCount = dao.update(updatedFriend)
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

    fun deleteFriend(context: Context, id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val numberOfRowsDeleted = dao.deleteById(id)
                withContext(Dispatchers.Main) {
                    if (numberOfRowsDeleted > 0) {
                        Toast.makeText(context, "Friend deleted", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Error deleting friend", Toast.LENGTH_SHORT).show()
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