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

/**
 * A RecyclerView adapter for managing a list of PointOfInterestEntity items in a user interface.
 *
 * @param onPointListener An interface to handle click events on the list items.
 */
class PointOfInterestRecyclerAdapter (private val onPointListener: OnPointClickListener)
: ListAdapter<PointOfInterestEntity, PointOfInterestRecyclerAdapter.PointsOfInterestViewHolder>(DIFF_UTIL) {

    /**
     * ViewHolder for individual items in the RecyclerView.
     *
     * @property pointName A TextView to display the name of the point of interest.
     * @property deleteButton A Button to trigger the deletion of the point of interest.
     * @property renameButton A Button to trigger the renaming of the point of interest.
     */
    inner class PointsOfInterestViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val pointName: TextView = itemView.findViewById(R.id.point_name)
        val deleteButton: Button = itemView.findViewById(R.id.btnDelete)
        val renameButton: Button = itemView.findViewById(R.id.btnRename)
    }

    /**
     * Inflates the layout for an item view and creates a new ViewHolder.
     *
     * @param parent The parent view group.
     * @param viewType The type of view.
     * @return A new PointsOfInterestViewHolder instance.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PointsOfInterestViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.raw_point, parent, false)
        return PointsOfInterestViewHolder(view)
    }

    /**
     * Binds the data to the item at the specified position in the RecyclerView.
     *
     * @param holder The ViewHolder to bind the data to.
     * @param position The position of the item in the list.
     */
    override fun onBindViewHolder(holder: PointsOfInterestViewHolder, position: Int) {
        val point = getItem(position)
        holder.pointName.text = point.name
        holder.deleteButton.setOnClickListener { onPointListener.onDelete(point) }
        holder.renameButton.setOnClickListener { onPointListener.onUpdate(point) }
    }

    /**
     * An interface for handling click events on list items.
     */
    interface OnPointClickListener {
        /**
         * Called when the delete button is clicked for a point of interest.
         *
         * @param point The PointOfInterestEntity to be deleted.
         */
        fun onDelete(point: PointOfInterestEntity)
        /**
         * Called when the rename button is clicked for a point of interest.
         *
         * @param point The PointOfInterestEntity to be updated.
         */
        fun onUpdate(point: PointOfInterestEntity)
    }

    companion object {
        /**
         * A DiffUtil callback for calculating the difference between two lists of PointOfInterestEntity items.
         */
        val DIFF_UTIL = object : DiffUtil.ItemCallback<PointOfInterestEntity>() {

            /**
             * Checks if two items are the same based on their unique identifiers (ids).
             *
             * @param oldItem The old PointOfInterestEntity.
             * @param newItem The new PointOfInterestEntity.
             * @return `true` if the items have the same id, `false` otherwise.
             */
            override fun areItemsTheSame(
                oldItem: PointOfInterestEntity,
                newItem: PointOfInterestEntity,
            ): Boolean =
                oldItem.id  == newItem.id

            /**
             * Checks if the content of two items is the same.
             *
             * @param oldItem The old PointOfInterestEntity.
             * @param newItem The new PointOfInterestEntity.
             * @return `true` if the items have the same content, `false` otherwise.
             */
            override fun areContentsTheSame(
                oldItem: PointOfInterestEntity,
                newItem: PointOfInterestEntity,
            ): Boolean =
                oldItem == newItem
        }
    }
}