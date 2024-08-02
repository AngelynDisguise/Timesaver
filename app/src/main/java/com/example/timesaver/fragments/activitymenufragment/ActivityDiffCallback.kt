package com.example.timesaver.fragments.activitymenufragment

import androidx.recyclerview.widget.DiffUtil
import com.example.timesaver.database.Activity

class ActivityDiffCallback : DiffUtil.ItemCallback<Activity>() {
    override fun areItemsTheSame(oldItem: Activity, newItem: Activity): Boolean {
        return oldItem.activityId == newItem.activityId
    }

    override fun areContentsTheSame(oldItem: Activity, newItem: Activity): Boolean {
        return oldItem == newItem
    }
}