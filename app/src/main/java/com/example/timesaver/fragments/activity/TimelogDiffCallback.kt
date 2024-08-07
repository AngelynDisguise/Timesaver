package com.example.timesaver.fragments.activity

import androidx.recyclerview.widget.DiffUtil
import com.example.timesaver.database.Timelog

class TimelogDiffCallback : DiffUtil.ItemCallback<Timelog>() {
    override fun areItemsTheSame(oldItem: Timelog, newItem: Timelog): Boolean {
        return oldItem.timelogId == newItem.timelogId
    }

    override fun areContentsTheSame(oldItem: Timelog, newItem: Timelog): Boolean {
        return oldItem == newItem
    }
}