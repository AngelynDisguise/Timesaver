package com.example.timesaver

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.timesaver.database.ActivityTimeLog
import java.time.Duration


class ActivityTimeLogListAdapter(private val logs: List<ActivityTimeLog>) :
    RecyclerView.Adapter<ActivityTimeLogListAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val activityTextView: TextView = view.findViewById(R.id.activity_text_view)
        val timeLogTextView: TextView = view.findViewById(R.id.timelog_text_view)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.activity_timelog_item, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.activityTextView.text = logs[position].activity.activityName
        viewHolder.timeLogTextView.text = formatDuration(logs[position].timeLog.timeElapsed)
    }

    override fun getItemCount(): Int {
        return logs.size
    }

    fun formatDuration(duration: Duration): String {
        val hours = duration.toHours()
        val minutes = duration.toMinutes() % 60
        val seconds = duration.seconds % 60
        return String.format("%d hours, %d min, %d sec", hours, minutes, seconds)
    }
}