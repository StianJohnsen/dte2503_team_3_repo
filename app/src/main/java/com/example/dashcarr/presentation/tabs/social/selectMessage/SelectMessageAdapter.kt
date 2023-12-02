package com.example.dashcarr.presentation.tabs.social.selectMessage

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.dashcarr.databinding.SelectMessageItemBinding

class SelectMessageAdapter(
    private var phoneNumber: String,
    private var emailAddress: String,
    private var onMessageButtonClicked: (SelectMessage) -> Unit,
    private var onEmailButtonClicked: (SelectMessage) -> Unit
) : ListAdapter<
        SelectMessage, SelectMessageAdapter.SelectMessageViewHolder>(DiffCallback) {

    companion object DiffCallback : DiffUtil.ItemCallback<SelectMessage>() {
        override fun areItemsTheSame(oldItem: SelectMessage, newItem: SelectMessage): Boolean {
            return oldItem.content == newItem.content
        }

        override fun areContentsTheSame(oldItem: SelectMessage, newItem: SelectMessage): Boolean {
            return oldItem.id == newItem.id
        }
    }

    class SelectMessageViewHolder(private var binding: SelectMessageItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(
            phoneNumber: String,
            emailAddress: String,
            selectMessage: SelectMessage,
            onMessageButtonClicked: (SelectMessage) -> Unit,
            onEmailButtonClicked: (SelectMessage) -> Unit,
        ) {
            if (emailAddress.isNotEmpty()) {
                binding.cardViewEmailButton.visibility = View.VISIBLE
            }
            if (phoneNumber.isNotEmpty()) {
                binding.cardViewMessageButton.visibility = View.VISIBLE
            }

            binding.selectMessage = selectMessage
            binding.executePendingBindings()
            binding.cardViewMessageButton.setOnClickListener {
                onMessageButtonClicked(selectMessage)
            }
            binding.cardViewEmailButton.setOnClickListener {
                onEmailButtonClicked(selectMessage)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectMessageViewHolder {
        return SelectMessageViewHolder(SelectMessageItemBinding.inflate(LayoutInflater.from(parent.context)))
    }

    override fun onBindViewHolder(holder: SelectMessageViewHolder, position: Int) {
        val selectMessage = getItem(position)
        holder.bind(phoneNumber, emailAddress, selectMessage, onMessageButtonClicked, onEmailButtonClicked)
    }
}