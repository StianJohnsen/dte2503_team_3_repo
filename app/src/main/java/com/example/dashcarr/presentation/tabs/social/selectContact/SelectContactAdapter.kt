package com.example.dashcarr.presentation.tabs.social.selectContact

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.dashcarr.databinding.SelectContactItemBinding

class SelectContactAdapter(private var onItemClicked: (SelectContact) -> Unit) : ListAdapter<
        SelectContact, SelectContactAdapter.SelectContactViewHolder>(DiffCallback) {


    companion object DiffCallback : DiffUtil.ItemCallback<SelectContact>() {
        override fun areItemsTheSame(oldItem: SelectContact, newItem: SelectContact): Boolean {
            return oldItem.name == newItem.name
        }

        override fun areContentsTheSame(oldItem: SelectContact, newItem: SelectContact): Boolean {
            return oldItem.id == newItem.id
        }
    }

    class SelectContactViewHolder(private var binding: SelectContactItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(selectContact: SelectContact) {
            binding.selectContact = selectContact
            binding.executePendingBindings()
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectContactViewHolder {
        return SelectContactViewHolder(SelectContactItemBinding.inflate(LayoutInflater.from(parent.context)))
    }

    override fun onBindViewHolder(holder: SelectContactViewHolder, position: Int) {
        val selectContact = getItem(position)
        holder.itemView.setOnClickListener {
            onItemClicked(selectContact)
        }
        holder.bind(selectContact)
    }


}

