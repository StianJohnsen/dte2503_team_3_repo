package com.example.dashcarr.presentation.tabs.settings.social_settings

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dashcarr.domain.entity.FriendsEntity
import com.example.dashcarr.domain.repository.IFriendsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * ViewModel for managing friends' data in the social settings of the application. It is marked
 * with the [HiltViewModel] annotation for dependency injection and uses [IFriendsRepository]
 * for data operations related to friends.
 *
 * @property friendsRepository The repository for friends.
 */
@HiltViewModel
class AddFriendViewModel @Inject constructor(
    private val friendsRepository: IFriendsRepository
) : ViewModel() {

    private val saveResult = MutableStateFlow<Boolean?>(null)

    fun getFriendById(id: Int): LiveData<FriendsEntity> {
        return friendsRepository.getFriendById(id)
    }

    fun saveNewFriend(friend: FriendsEntity) {
        viewModelScope.launch {
            val result = friendsRepository.saveNewFriend(friend)
            saveResult.value = result
        }
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
                val result = friendsRepository.updateFriend(updatedFriend)
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

    fun deleteFriend(context: Context, id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val friendToDelete = FriendsEntity(id = id,
                    name = "",
                    phone = "",
                    email = "",
                    createdTimeStamp = System.currentTimeMillis())
                val result = friendsRepository.deleteFriend(friendToDelete)
                withContext(Dispatchers.Main) {
                    if (result.isSuccess) {
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