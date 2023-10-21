package com.example.dashcarr.presentation.tabs.settings.maps_settings.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.dashcarr.R
import com.example.dashcarr.domain.entity.PointOfInterestEntity

class PointOfInterestRecyclerAdapter (private val onPointListener: OnPointClickListener)
: ListAdapter<PointOfInterestEntity, PointOfInterestRecyclerAdapter.PointsOfInterestViewHolder>(DIFF_UTIL) {

    inner class PointsOfInterestViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val pointName: TextView = itemView.findViewById(R.id.point_name)
        val deleteButton: Button = itemView.findViewById(R.id.btnDelete)
        val renameButton: Button = itemView.findViewById(R.id.btnRename)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PointsOfInterestViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.raw_point, parent, false)
        return PointsOfInterestViewHolder(view)
    }

    override fun onBindViewHolder(holder: PointsOfInterestViewHolder, position: Int) {
        val point = getItem(position)
        holder.pointName.text = point.name
        holder.deleteButton.setOnClickListener { onPointListener.onDelete(point) }
        holder.renameButton.setOnClickListener { onPointListener.onUpdate(point) }
    }

    interface OnPointClickListener {
        fun onDelete(point: PointOfInterestEntity)
        fun onUpdate(point: PointOfInterestEntity)
    }

    companion object {
        val DIFF_UTIL = object : DiffUtil.ItemCallback<PointOfInterestEntity>() {

            override fun areItemsTheSame(
                oldItem: PointOfInterestEntity,
                newItem: PointOfInterestEntity,
            ): Boolean =
                oldItem.id  == newItem.id

            override fun areContentsTheSame(
                oldItem: PointOfInterestEntity,
                newItem: PointOfInterestEntity,
            ): Boolean =
                oldItem == newItem
        }
    }
}