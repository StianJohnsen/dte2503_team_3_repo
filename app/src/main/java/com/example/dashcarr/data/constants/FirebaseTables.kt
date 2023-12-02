package com.example.dashcarr.data.constants

/**
 * A utility class providing constants for Firebase table names and keys.
 * This class helps in maintaining consistent naming conventions across the Firebase database structure.
 */
abstract class FirebaseTables {
    companion object {

        //Friend

        const val FRIENDS_DOCUMENT = "FriendsDocument"
        const val FRIENDS_COLLECTION = "FriendsCollection"
        const val FRIEND_NAME_KEY = "name"
        const val FRIEND_PHONE_KEY = "phone"
        const val FRIEND_EMAIL_KEY = "email"
        const val FRIEND_CREATED_TIMESTAMP_KEY = "createdTimeStamp"

        //Message

        const val MESSAGES_DOCUMENT = "MessagesDocument"
        const val MESSAGES_COLLECTION = "MessagesCollection"
        const val MESSAGE_CONTENT_KEY = "content"
        const val MESSAGE_IPHONE_KEY = "iphone"
        const val MESSAGE_CREATED_TIMESTAMP_KEY = "createdTimeStamp"

        //Last changes timestamps
        const val LAST_CHANGES_DOCUMENT = "LastChangesDocument"
        const val LAST_FRIENDS_CHANGES_TIMESTAMP_KEY = "lastFriendsChangesTimestamp"
        const val LAST_MESSAGES_CHANGES_TIMESTAMP_KEY = "lastMessagesChangesTimestamp"
    }
}