package com.example.timesaver

import androidx.recyclerview.widget.DiffUtil
import com.example.timesaver.fragments.UILog

class UILogDiffCallback : DiffUtil.ItemCallback<UILog>() {
    override fun areItemsTheSame(oldItem: UILog, newItem: UILog): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: UILog, newItem: UILog): Boolean {
        return oldItem == newItem
    }
}