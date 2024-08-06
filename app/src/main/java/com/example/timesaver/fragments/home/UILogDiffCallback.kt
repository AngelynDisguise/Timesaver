package com.example.timesaver.fragments.home

import androidx.recyclerview.widget.DiffUtil

class UILogDiffCallback : DiffUtil.ItemCallback<UILog>() {
    override fun areItemsTheSame(oldItem: UILog, newItem: UILog): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: UILog, newItem: UILog): Boolean {
        return oldItem == newItem
    }
}