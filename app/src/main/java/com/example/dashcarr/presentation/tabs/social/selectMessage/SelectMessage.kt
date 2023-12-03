package com.example.dashcarr.presentation.tabs.social.selectMessage

/**
 * Data class representing a selectable message.
 *
 * This data class holds information about a message that can be selected for communication.
 *
 * @param id The unique identifier of the message.
 * @param content The content of the message.
 * @param isPhone A flag indicating whether the message is a phone message (true) or an email message (false).
 */
data class SelectMessage(
    val id: Long,
    val content: String,
    val isPhone: Boolean
)
