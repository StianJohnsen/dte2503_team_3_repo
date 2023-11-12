package com.example.dashcarr.presentation.tabs.settings.social_settings

import androidx.lifecycle.ViewModel
import com.example.dashcarr.data.database.dao.FriendsDao
import com.example.dashcarr.domain.entity.FriendsEntity

class SocialSettingsViewModel(
    private val dao: FriendsDao
) : ViewModel() {

    fun hasPhoneNumber(friend: FriendsEntity): Boolean {
        return friend.phone.isNotEmpty()
    }

    fun hasEmail(friend: FriendsEntity): Boolean {
        return friend.email.isNotEmpty()
    }
}