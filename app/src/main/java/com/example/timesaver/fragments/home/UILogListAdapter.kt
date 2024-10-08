package com.example.timesaver.fragments.home

import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.graphics.drawable.RippleDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RectShape
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.timesaver.util.TimelogBarView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.timesaver.R
import java.time.Duration


class UILogListAdapter: ListAdapter<UILog, UILogListAdapter.ViewHolder>(UILogDiffCallback()) {

    var maxDuration: Duration = Duration.ofMinutes(30)

    private var onClickListener: ((View, Long) -> Unit)? = null

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val itemLayout: LinearLayout = view.findViewById(R.id.ui_log_item_layout)
        val activityTextView: TextView = view.findViewById(R.id.ui_log_activity_text_view)
        val timeLogTextView: TextView = view.findViewById(R.id.ui_log_timelog_text_view)
        val timelogBarView: TimelogBarView = view.findViewById(R.id.ui_log_timelog_bar)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.ui_log_item, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val log: UILog = getItem(position)
        val timeElapsed: Duration = log.totalTime

        if (timeElapsed >= Duration.ofSeconds(1)) { // should never be 0 but just a check
            viewHolder.activityTextView.text = log.activityName
            viewHolder.timeLogTextView.text = formatDuration(timeElapsed)

            // Configure timelog progress bar
            viewHolder.timelogBarView.setRatio(calculateBarWidthRatio(timeElapsed))
            viewHolder.timelogBarView.setBarColor(log.color)

            // Add ripple effect when touched
            val rippleDrawable = createRippleDrawable(log.color)
            viewHolder.itemView.background = rippleDrawable

            viewHolder.itemLayout.setOnClickListener {
                onClickListener?.invoke(it, log.id)
            }
        }
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

        return parts.joinToString(", ")
    }

    private fun calculateBarWidthRatio(duration: Duration): Float {
        return (duration.seconds.toFloat() / maxDuration.seconds.toFloat()).coerceIn(0f, 1f)
    }

    private fun createRippleDrawable(color: Int): Drawable {
        val mask = ShapeDrawable(RectShape())
        mask.paint.color = android.graphics.Color.WHITE

        // Adjust rgb to make color a bit more transparent?
        val rippleColor = android.graphics.Color.argb(
            (android.graphics.Color.alpha(color) * 0.3).toInt(), // 30% of alpha
            android.graphics.Color.red(color),
            android.graphics.Color.green(color),
            android.graphics.Color.blue(color)
        )

        return RippleDrawable(
            ColorStateList.valueOf(rippleColor),
            null,
            mask
        )
    }

    fun setOnClickListener(listener: (View, Long) -> Unit) {
        onClickListener = listener
    }
}