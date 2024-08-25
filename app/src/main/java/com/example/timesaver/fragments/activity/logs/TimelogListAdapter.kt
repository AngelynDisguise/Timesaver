package com.example.timesaver.fragments.activity.logs

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.timesaver.R
import com.example.timesaver.database.Timelog
import com.example.timesaver.fragments.activity.DateFormat
import com.example.timesaver.fragments.activity.TimeFormat
import java.time.Duration
import java.time.format.DateTimeFormatter

class TimelogListAdapter: PagingDataAdapter<Timelog, TimelogListAdapter.ViewHolder>(TimelogDiffCallback()) {

    private var dateFormat: DateTimeFormatter = DateTimeFormatter.ofPattern(DateFormat.US.pattern) // default
    private var timeFormat: DateTimeFormatter = DateTimeFormatter.ofPattern(TimeFormat.STANDARD_TIME.pattern) // default

    private var expandedRow: View? = null

    // Parent row clickables
    private var onClickTimelogListener: ((ViewHolder) -> Unit)? = null
    private var onClickDeleteListener: ((View, Timelog) -> Unit)? = null

    // Child row clickables
    private var onEditDateListener: ((EditText) -> Unit)? = null
    private var onEditStartTimeListener: ((ViewHolder, Timelog) -> Unit)? = null
    private var onEditEndTimeListener: ((ViewHolder, Timelog) -> Unit)? = null
    private var onClickConfirmListener: ((ViewHolder, Timelog) -> Unit)? = null

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val parentRow: LinearLayout = view.findViewById(R.id.timelog_parent_row_layout)
        val childRow: LinearLayout = view.findViewById(R.id.timelog_child_row_layout)

        // Parent stuff
        val dateTextView: TextView = view.findViewById(R.id.timelog_date_text_view)
        val totalTimeTextView: TextView = view.findViewById(R.id.timelog_total_time_text_view)
        val startTimeTextView: TextView = view.findViewById(R.id.timelog_start_time_text_view)
        val endTimeTextView: TextView = view.findViewById(R.id.timelog_end_time_text_view)

        // Child stuff
        val dateEditText: EditText = view.findViewById(R.id.timelog_date_edit_text_view)
        val startTimeEditText: EditText = view.findViewById(R.id.timelog_start_time_edit_text_view)
        val endTimeEditText: EditText = view.findViewById(R.id.timelog_end_time_edit_text_view)
        val modifiedTotalTime: TextView = view.findViewById(R.id.timelog_modified_total_time_text_view)

        // Icons
        val deleteIcon: ImageView = view.findViewById(R.id.timelog_delete_icon)
        val confirmIcon: ImageView = view.findViewById(R.id.timelog_confirm_icon)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.timelog_item, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val timelog: Timelog? = getItem(position)

        // Note: timelog is nullable if it's a placeholder, but this should never be true (see Flow in LogsViewModel).
        timelog?.let {
            val date = timelog.date.format(dateFormat)
            val startTime = timelog.startTime.format(timeFormat)
            val endTime = timelog.endTime.format(timeFormat)
            val duration = formatDuration(Duration.between(timelog.startTime, timelog.endTime))

            // Set up parent row
            viewHolder.dateTextView.text = date
            viewHolder.startTimeTextView.text = startTime
            viewHolder.endTimeTextView.text = endTime
            viewHolder.totalTimeTextView.text = duration

            // Set up child row
            viewHolder.dateEditText.hint = date
            viewHolder.startTimeEditText.hint = startTime
            viewHolder.endTimeEditText.hint = endTime
            viewHolder.modifiedTotalTime.text = "---"

            // Just in case of previous edit
            viewHolder.dateEditText.text.clear()
            viewHolder.startTimeEditText.text.clear()
            viewHolder.endTimeEditText.text.clear()

            // Set up parent clickables
            viewHolder.parentRow.setOnClickListener {
                onClickTimelogListener?.invoke(viewHolder)
            }
            viewHolder.deleteIcon.setOnClickListener {
                onClickDeleteListener?.invoke(it, timelog)
            }

            // Set up child clickables
            viewHolder.confirmIcon.setOnClickListener {
                onClickConfirmListener?.invoke(viewHolder, timelog)
            }
            viewHolder.dateEditText.setOnClickListener {
                onEditDateListener?.invoke(viewHolder.dateEditText)
            }
            viewHolder.startTimeEditText.setOnClickListener {
                onEditStartTimeListener?.invoke(viewHolder, timelog)
            }
            viewHolder.endTimeEditText.setOnClickListener {
                onEditEndTimeListener?.invoke(viewHolder, timelog)
            }
        }
    }

    fun toggleRow(row: View) {
        if (row.visibility == View.GONE) {
            expandedRow?.let {
                collapseRow(it)
            }
            expandRow(row)
            expandedRow = row
        } else {
            collapseRow(row)
            expandedRow = null
        }
    }

    private fun expandRow(view: View) {
        view.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        val targetHeight = view.measuredHeight

        view.layoutParams.height = 0
        view.visibility = View.VISIBLE
        animateHeight(view, 0, targetHeight)
    }

    private fun collapseRow(view: View) {
        val initialHeight = view.height
        animateHeight(view, initialHeight, 0) {
            view.visibility = View.GONE
        }
    }

    private fun animateHeight(view: View, start: Int, end: Int, onEnd: () -> Unit = {}) {
        ValueAnimator.ofInt(start, end).apply {
            duration = 200
            addUpdateListener { animator ->
                view.layoutParams.height = animator.animatedValue as Int
                view.requestLayout()
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    if (end != 0) view.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
                    onEnd()
                }
            })
            start()
        }
    }

    fun formatDuration(duration: Duration): String {
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

    fun setDateFormat(format: DateFormat){
        dateFormat = DateTimeFormatter.ofPattern(format.pattern)
        notifyDataSetChanged()
    }

    fun setTimeFormat(format: TimeFormat){
        timeFormat = DateTimeFormatter.ofPattern(format.pattern)
        notifyDataSetChanged()
    }

    fun setOnClickTimelogListener(listener: (ViewHolder) -> Unit) {
        onClickTimelogListener = listener
    }

    // Delete
    fun setOnClickDeleteListener(listener: (View, Timelog) -> Unit) {
        onClickDeleteListener = listener
    }

    fun setOnEditDateListener(listener: (EditText) -> Unit) {
        onEditDateListener = listener
    }

    fun setOnEditStartTimeListener(listener: (ViewHolder, Timelog) -> Unit) {
        onEditStartTimeListener = listener
    }

    fun setOnEditEndTimeListener(listener: (ViewHolder, Timelog) -> Unit) {
        onEditEndTimeListener = listener
    }


    fun setOnClickConfirmListener(listener: (ViewHolder, Timelog) -> Unit) {
        onClickConfirmListener = listener
    }

}