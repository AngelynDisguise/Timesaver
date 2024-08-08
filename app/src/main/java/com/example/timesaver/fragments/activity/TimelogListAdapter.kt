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
import java.time.format.DateTimeFormatter

class TimelogListAdapter: ListAdapter<Timelog, TimelogListAdapter.ViewHolder>(TimelogDiffCallback()) {

    private var dateFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("MM-dd-yyyy") // default
    private var timeFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("hh:mm a") // default

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val dateTextView: TextView = view.findViewById(R.id.timelog_date_text_view)
        val totalTimeTextView: TextView = view.findViewById(R.id.timelog_total_time_text_view)
        val startTimeTextView: TextView = view.findViewById(R.id.timelog_start_time_text_view)
        val endTimeTextView: TextView = view.findViewById(R.id.timelog_end_time_text_view)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.timelog_item, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val timelog: Timelog = getItem(position)
        viewHolder.dateTextView.text = timelog.date.format(dateFormat)
        viewHolder.startTimeTextView.text = timelog.startTime.format(timeFormat)
        viewHolder.endTimeTextView.text = timelog.endTime.format(timeFormat)
        viewHolder.totalTimeTextView.text = formatDuration(Duration.between(timelog.startTime, timelog.endTime))
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

    fun setDateFormat(pattern: String){
        dateFormat = DateTimeFormatter.ofPattern(pattern)
    }

    fun setTimeFormat(pattern: String){
        timeFormat = DateTimeFormatter.ofPattern(pattern)
    }

}