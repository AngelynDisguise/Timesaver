package com.example.timesaver.fragments.activity

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.timesaver.R
import com.example.timesaver.database.Timelog
import java.time.Duration

class TimelogListAdapter: ListAdapter<Timelog, TimelogListAdapter.ViewHolder>(TimelogDiffCallback()) {


    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val dateTextView: TextView = view.findViewById(R.id.timelog_date_text_view)
        val timeTextView: TextView = view.findViewById(R.id.timelog_time_text_view)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.timelog_item, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val timelog: Timelog = getItem(position)
        viewHolder.dateTextView.text = timelog.date.toString()
        viewHolder.timeTextView.text = formatDuration(Duration.between(timelog.startTime, timelog.endTime))
    }

    private fun formatDuration(duration: Duration): String {
        val hours = duration.toHours()
        val minutes = duration.toMinutes() % 60
        val seconds = duration.seconds % 60

        val parts = mutableListOf<String>()
        if (hours > 1) parts.add("$hours hours")
        if (hours.toInt() == 1) parts.add("$hours hour")
        if (minutes > 0) parts.add("$minutes min")
        if (seconds > 0) parts.add("$seconds sec")
        //if (duration == Duration.ZERO) parts.add("---") // should not save if no time elapsed

        return parts.joinToString(", ")
    }

}