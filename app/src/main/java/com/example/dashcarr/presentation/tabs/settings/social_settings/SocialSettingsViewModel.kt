package com.example.dashcarr.presentation.tabs.settings.social_settings

import androidx.lifecycle.ViewModel
import com.example.dashcarr.domain.entity.FriendsEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * ViewModel for Social Settings.
 * Provides functionalities to check if a friend entity has a phone number or email.
 * This aids in managing friend contact details in the Social Settings context.
 */
@HiltViewModel
class SocialSettingsViewModel @Inject constructor(
    ): ViewModel() {

    fun hasPhoneNumber(friend: FriendsEntity): Boolean {
        return friend.phone.isNotEmpty()
    }

    fun hasEmail(friend: FriendsEntity): Boolean {
        return friend.email.isNotEmpty()
    }
}