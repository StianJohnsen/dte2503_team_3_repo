package com.example.dashcarr.presentation.tabs.social.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.dashcarr.R
import com.example.dashcarr.domain.entity.SentMessageFinalEntity
import com.example.dashcarr.domain.entity.SentMessagesEntity
import com.example.dashcarr.extensions.getFormattedDate

/**
 * The `SentMessagesAdapter` is responsible for displaying sent messages in a RecyclerView.
 * It uses a ListAdapter with DiffUtil for efficient updates.
 * The adapter handles click events on message items through the `onMessageListener` callback.
 */
class SentMessagesAdapter (private val onMessageListener: IOnSentMessageClickListener)
    : ListAdapter<SentMessageFinalEntity, SentMessagesAdapter.SentMessagesViewHolder>(DIFF_UTIL)
{
    inner class SentMessagesViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val name: TextView = itemView.findViewById(R.id.tv_to)
        val message: TextView = itemView.findViewById(R.id.tv_message_content)
        val date: TextView = itemView.findViewById(R.id.tv_message_time)
        val messageType: ImageView = itemView.findViewById(R.id.icon_sms)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SentMessagesViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.raw_message, parent, false)
        return SentMessagesViewHolder(view)
    }

    override fun onBindViewHolder(holder: SentMessagesViewHolder, position: Int) {
        val message = getItem(position)
        holder.itemView.setOnClickListener {
            onMessageListener.onClick(message.sentMessage)
        }
        holder.name.text = message.friend?.name ?: holder.itemView.context.getText(R.string.unknown_user)
        holder.message.text = message.message.content
        holder.date.text = getFormattedDate(message.sentMessage.createdTimeStamp)
        holder.messageType.setImageResource(
            if (message.sentMessage.isSms) R.drawable.ic_message
            else R.drawable.ic_email)
    }

    companion object {
        val DIFF_UTIL = object : DiffUtil.ItemCallback<SentMessageFinalEntity>() {
            override fun areItemsTheSame(
                oldItem: SentMessageFinalEntity,
                newItem: SentMessageFinalEntity,
                ): Boolean =
                oldItem.sentMessage.id == newItem.sentMessage.id


            override fun areContentsTheSame(
                oldItem: SentMessageFinalEntity,
                newItem: SentMessageFinalEntity,
                ): Boolean =
                oldItem == newItem

        }
    }

    interface IOnSentMessageClickListener {
        fun onClick(message: SentMessagesEntity)
    }
}