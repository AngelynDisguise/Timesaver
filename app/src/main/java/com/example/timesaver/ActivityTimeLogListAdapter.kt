package com.example.timesaver

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.timesaver.database.ActivityTimeLog
import java.time.Duration


class ActivityTimeLogListAdapter(private val logs: List<ActivityTimeLog>) :
    RecyclerView.Adapter<ActivityTimeLogListAdapter.ViewHolder>() {

    private var maxDuration = Duration.ofHours(1)

    init {
        updateMaxDuration()
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val activityTextView: TextView = view.findViewById(R.id.activity_text_view)
        val timeLogTextView: TextView = view.findViewById(R.id.timelog_text_view)
        val timeLogBar: View = view.findViewById(R.id.timelog_bar)
        val barContainer: FrameLayout = view.findViewById(R.id.bar_container)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.activity_timelog_item, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val log = logs[position]
        viewHolder.activityTextView.text = log.activity.activityName
        viewHolder.timeLogTextView.text = formatDuration(log.timeLog.timeElapsed)

        viewHolder.barContainer.post {
            val barWidthRatio = calculateBarWidthRatio(log.timeLog.timeElapsed)
            val barWidth = (viewHolder.barContainer.width * barWidthRatio).toInt()
            val layoutParams = viewHolder.timeLogBar.layoutParams
            layoutParams.width = barWidth
            viewHolder.timeLogBar.layoutParams = layoutParams
        }
    }

    override fun getItemCount(): Int {
        return logs.size
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
        if (duration.seconds.toInt() == 0) parts.add("---")

        return parts.joinToString(", ")
    }


    private fun updateMaxDuration() {
        maxDuration = logs.maxOfOrNull { it.timeLog.timeElapsed } ?: Duration.ofHours(1)
        maxDuration = maxDuration.coerceAtLeast(Duration.ofHours(1))
    }

    private fun calculateBarWidthRatio(duration: Duration): Float {
        return (duration.seconds.toFloat() / maxDuration.seconds.toFloat()).coerceIn(0f, 1f)
    }
}