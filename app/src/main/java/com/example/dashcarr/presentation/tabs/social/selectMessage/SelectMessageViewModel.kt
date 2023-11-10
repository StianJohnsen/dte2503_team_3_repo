package com.example.dashcarr.presentation.tabs.social.selectMessage

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dashcarr.data.database.AppDatabase
import com.example.dashcarr.data.database.dao.MessagesDao
import com.example.dashcarr.domain.entity.FriendsEntity
import com.example.dashcarr.domain.entity.MessagesEntity
import com.example.dashcarr.domain.entity.SentMessagesEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SelectMessageViewModel(messagesDao: MessagesDao) : ViewModel() {

    private var _messagesList = MutableLiveData<List<MessagesEntity>>()
    val messagesList: LiveData<List<MessagesEntity>> = _messagesList

    private var _contactsList = MutableLiveData<FriendsEntity>()
    val contactsList: LiveData<FriendsEntity> = _contactsList

    fun getAllMessages(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getInstance(context)
            _messagesList.postValue(db.MessagesDao().getAllMessages())
        }
    }

    fun getContact(id: Int, context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getInstance(context)
            _contactsList.postValue(db.FriendsDao().getContactById(id))

        }
    }

    fun insertIntoSentMessages(context: Context, sentMessagesEntity: SentMessagesEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getInstance(context)
            db.SentMessageDao().insert(sentMessagesEntity)
        }

    }
}