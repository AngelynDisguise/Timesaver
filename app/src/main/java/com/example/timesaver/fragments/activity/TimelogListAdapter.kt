package com.example.timesaver.fragments.activity

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
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.timesaver.R
import com.example.timesaver.database.Timelog
import java.time.Duration
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class TimelogListAdapter: ListAdapter<Timelog, TimelogListAdapter.ViewHolder>(TimelogDiffCallback()) {

    // Parent row clickables
    private var onClickTimelogListener: ((ViewHolder) -> Unit)? = null
    private var onClickDeleteListener: ((View, Timelog) -> Unit)? = null

    // Child row clickables
    private var onEditDateListener: ((EditText) -> Unit)? = null
    private var onEditStartTimeListener: ((LocalTime, LocalTime, EditText, EditText) -> Unit)? = null
    private var onEditEndTimeListener: ((LocalTime, LocalTime, EditText, EditText) -> Unit)? = null
    private var onClickConfirmListener: ((ViewHolder, Timelog) -> Unit)? = null

    private var dateFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("MM-dd-yyyy") // default
    private var timeFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("hh:mm a") // default

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
        val timelog: Timelog = getItem(position)

        // Set up parent row
        viewHolder.dateTextView.text = timelog.date.format(dateFormat)
        viewHolder.startTimeTextView.text = timelog.startTime.format(timeFormat)
        viewHolder.endTimeTextView.text = timelog.endTime.format(timeFormat)
        viewHolder.totalTimeTextView.text = formatDuration(Duration.between(timelog.startTime, timelog.endTime))

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
            onEditStartTimeListener?.invoke(timelog.startTime, timelog.endTime, viewHolder.startTimeEditText, viewHolder.endTimeEditText)
        }
        viewHolder.endTimeEditText.setOnClickListener {
            onEditEndTimeListener?.invoke(timelog.startTime, timelog.endTime, viewHolder.startTimeEditText, viewHolder.endTimeEditText)
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
        //if (duration == Duration.ZERO) parts.add("---") // should not save if no time elapsed

        return parts.joinToString(", ")
    }

    fun toggleChildRow(viewHolder: ViewHolder) {
        if (viewHolder.childRow.visibility == View.GONE) {
            expandChildRow(viewHolder.childRow)
        } else {
            collapseChildRow(viewHolder.childRow)
        }
    }

    private fun expandChildRow(view: View) {
        view.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        val targetHeight = view.measuredHeight

        view.layoutParams.height = 0
        view.visibility = View.VISIBLE
        animateHeight(view, 0, targetHeight)
    }

    private fun collapseChildRow(view: View) {
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

    fun setDateFormat(pattern: String){
        dateFormat = DateTimeFormatter.ofPattern(pattern)
    }

    fun setTimeFormat(pattern: String){
        timeFormat = DateTimeFormatter.ofPattern(pattern)
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

    fun setOnEditStartTimeListener(listener: (LocalTime, LocalTime, EditText, EditText) -> Unit) {
        onEditStartTimeListener = listener
    }

    fun setOnEditEndTimeListener(listener: (LocalTime, LocalTime, EditText, EditText) -> Unit) {
        onEditEndTimeListener = listener
    }


    fun setOnClickConfirmListener(listener: (ViewHolder, Timelog) -> Unit) {
        onClickConfirmListener = listener
    }

}