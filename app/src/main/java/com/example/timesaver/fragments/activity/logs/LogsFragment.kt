package com.example.timesaver.fragments.activity.logs

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.os.Bundle
import android.text.Editable
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.example.timesaver.R
import com.example.timesaver.database.Timelog
import com.example.timesaver.fragments.activity.ActivityFragment
import com.example.timesaver.fragments.activity.ActivityViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.util.Locale

class LogsFragment : Fragment() {

    private lateinit var viewModel: ActivityViewModel

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val parentFragment = parentFragment
        require(parentFragment is ActivityFragment) {
            "LogsFragment must be a child of ActivityFragment, instead it is a child of $parentFragment."
        }
        viewModel = parentFragment.viewModel
    }

    private lateinit var adapter: TimelogListAdapter
    private val calendar = Calendar.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_logs, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupAddTimelogLayout(view)

        // Set up adapter
        adapter = TimelogListAdapter()
        val recyclerView: RecyclerView = view.findViewById(R.id.logs_recycler_view)
        recyclerView.adapter = adapter

        // Collect the paging data from the stream
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.timelogs.collectLatest { pagingData ->
                adapter.submitData(pagingData)
                Log.i(
                    "LogsFragment",
                    "Received PagedData: $pagingData"
                )
            }
        }

        // Change chronological order of timelogs list
        val sortButton: ImageView = view.findViewById(R.id.timelog_sort_icon)
        sortButton.setOnClickListener {
            viewModel.toggleSortOrder()
        }

        // Drop down edit layout
        adapter.setOnClickTimelogListener { viewHolder ->
            val addTimelogLayout: LinearLayout = view.findViewById(R.id.logs_add_timelog_item)
            if (addTimelogLayout.visibility == View.VISIBLE) {
                val addTimelogButton: Button = view.findViewById(R.id.add_timelog_button)
                closeAddTimelogLayout(addTimelogLayout, addTimelogButton)
            }
            adapter.toggleRow(viewHolder.childRow) // input UI is saved while fragment is alive
        }

        // Delete timelog
        adapter.setOnClickDeleteListener { deleteView, timelog ->
            deleteTimelog(deleteView, timelog)
        }

        // Save edit input if valid
        adapter.setOnClickConfirmListener { viewHolder, timelog ->
            viewLifecycleOwner.lifecycleScope.launch {
                if (validateUpdateInput(
                        viewHolder.dateEditText,
                        viewHolder.startTimeEditText,
                        viewHolder.endTimeEditText,
                        timelog
                )) {
                    adapter.toggleRow(viewHolder.childRow)
                } // else do nothing
            }
        }

        // Edit texts
        adapter.setOnEditDateListener {
            openDatePicker(it)
        }
        adapter.setOnEditStartTimeListener { viewHolder, timelog ->
            openTimePicker(
                Pair(timelog.startTime, timelog.endTime),
                viewHolder.startTimeEditText,
                viewHolder.endTimeEditText,
                viewHolder.modifiedTotalTime,
                true
            )
        }
        adapter.setOnEditEndTimeListener { viewHolder, timelog ->
            openTimePicker(
                Pair(timelog.startTime, timelog.endTime),
                viewHolder.startTimeEditText,
                viewHolder.endTimeEditText,
                viewHolder.modifiedTotalTime,
                false
            )
        }
    }

    private fun setupAddTimelogLayout(view: View) {
        val addTimelogButton: Button = view.findViewById(R.id.add_timelog_button)
        val layout: LinearLayout = view.findViewById(R.id.logs_add_timelog_item)
        val dateET: EditText = layout.findViewById(R.id.timelog_add_date_edit_text_view)
        val startET: EditText = layout.findViewById(R.id.timelog_add_start_time_edit_text_view)
        val endET: EditText = layout.findViewById(R.id.timelog_add_end_time_edit_text_view)
        val durationT: TextView = layout.findViewById(R.id.timelog_add_total_time_text_view)
        val deleteButton: ImageView = layout.findViewById(R.id.timelog_delete_icon)
        val confirmButton: ImageView = layout.findViewById(R.id.timelog_confirm_icon)

        // Opening the Add Timelog layout
        addTimelogButton.setOnClickListener {
            if (layout.visibility == View.GONE) {
                openAddTimelogLayout(layout, addTimelogButton)
            } else {
                closeAddTimelogLayout(layout, addTimelogButton)
            }
        }

        // Edit fields for the new Timelog
        dateET.setOnClickListener { openDatePicker(it as EditText) }
        startET.setOnClickListener { openTimePicker(null, startET, endET, durationT, true) }
        endET.setOnClickListener { openTimePicker(null, startET, endET, durationT, false) }
        durationT.text = "---"

        // Adding/Deleting the Timelog
        deleteButton.setOnClickListener { closeAddTimelogLayout(layout, addTimelogButton) }
        confirmButton.setOnClickListener{
            viewLifecycleOwner.lifecycleScope.launch {
                if (validateAddInput(dateET, startET, endET)) {
                    closeAddTimelogLayout(layout, addTimelogButton)
                } // else do nothing
            }
        }
    }

    private fun openAddTimelogLayout(layout: LinearLayout, addTimelogButton: Button) {
        addTimelogButton.text = "Cancel"
        adapter.toggleRow(layout)
    }

    private fun closeAddTimelogLayout(addTimelogLayout: LinearLayout, addTimelogButton: Button) {
        val dateET: EditText = addTimelogLayout.findViewById(R.id.timelog_add_date_edit_text_view)
        val startET: EditText = addTimelogLayout.findViewById(R.id.timelog_add_start_time_edit_text_view)
        val endET: EditText = addTimelogLayout.findViewById(R.id.timelog_add_end_time_edit_text_view)
        val durationT: TextView = addTimelogLayout.findViewById(R.id.timelog_add_total_time_text_view)
        dateET.text.clear()
        startET.text.clear()
        endET.text.clear()
        durationT.text = "---"
        addTimelogButton.text = "Add Timelog"
        adapter.toggleRow(addTimelogLayout)
    }

    private fun openDatePicker(dateEditText: EditText) {
        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                val dateFormat = SimpleDateFormat("MM-dd-yyyy", Locale.US)
                dateEditText.setText(dateFormat.format(calendar.time))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun openTimePicker(
        oldTimeRange: Pair<LocalTime, LocalTime>?, // we're updating if this is not null
        startEditText: EditText,
        endEditText: EditText,
        durationText: TextView,
        isStart: Boolean
    ) {
        val (currEditText, otherEditText) = if (isStart) (startEditText to endEditText) else (endEditText to startEditText)

        // Open Time picker
        TimePickerDialog(
            requireContext(),
            { _, hourOfDay, minute ->
                val newTime: LocalTime = LocalTime.of(hourOfDay, minute)
                currEditText.setText(newTime.format(viewModel.timeFormatter))

                // If updating, update new time range
                oldTimeRange?.let { (oldStartTime, oldEndTime) ->
                    val (newStartTime, newEndTime) = when {
                        isStart && otherEditText.text.isEmpty() -> newTime to oldEndTime
                        isStart -> newTime to parseTime(otherEditText.text)
                        otherEditText.text.isEmpty() -> oldStartTime to newTime
                        else -> parseTime(otherEditText.text) to newTime
                    }

                    if (timeRangeIsValid(newStartTime, newEndTime, currEditText, isStart)) {
                        durationText.text = adapter.formatDuration(Duration.between(newStartTime, newEndTime))
                        if (otherEditText.text.isEmpty()) {
                            otherEditText.setText(otherEditText.hint) // reveal the old time range if one side is empty
                        }
                    }
                } ?: let {
                    if (otherEditText.text.isNotEmpty()) {
                        val (newStartTime, newEndTime) = if (isStart) newTime to parseTime(otherEditText.text) else parseTime(otherEditText.text) to newTime
                        if (timeRangeIsValid(newStartTime, newEndTime, currEditText, isStart)) {
                            durationText.text = adapter.formatDuration(Duration.between(newStartTime, newEndTime))
                        }
                    }
                }
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            false
        ).show()
    }

    private fun timeRangeIsValid(startTime: LocalTime, endTime: LocalTime, currEditText: EditText, isStart: Boolean): Boolean {
        val isValid = if (isStart) startTime < endTime else endTime > startTime
        if (!isValid) {
            val message = if (isStart) "Start time cannot be later than or equal to end time." else "End time cannot be earlier than or equal to start time."
            Toast.makeText(requireContext(), "$message Try again.", Toast.LENGTH_LONG).show()
            currEditText.text.clear()
        }
        return isValid
    }

    private fun parseDate(text: Editable): LocalDate = viewModel.dateFormatter.parse(text, LocalDate::from)
    private fun parseTime(text: Editable): LocalTime = viewModel.timeFormatter.parse(text, LocalTime::from)

    private suspend fun validateAddInput(dateET: EditText, startET: EditText, endET: EditText): Boolean {
        val isInvalidMessage: String? = when {
            dateET.text.isEmpty() -> "Please enter a date."
            startET.text.isEmpty() -> "Please enter a start time."
            endET.text.isEmpty() -> "Please enter an end time."
            else -> null
        }

        isInvalidMessage?.let {
            Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            return false
        } ?: let {
            viewModel.activity?.let {
                val newTimelog = Timelog(
                    timelogId = 0,
                    activityId = it.activityId,
                    date = parseDate(dateET.text),
                    startTime = parseTime(startET.text),
                    endTime = parseTime(endET.text)
                )
                return insertNewTimelog(newTimelog, true)
            } ?: return false
        }
    }

    private suspend fun validateUpdateInput(
        dateET: EditText,
        startET: EditText,
        endET: EditText,
        timelog: Timelog
    ): Boolean {
        val dateText = dateET.text
        val startText = startET.text
        val endText = endET.text

        val currDate = if (dateText.isNotEmpty()) viewModel.dateFormatter.parse(dateText, LocalDate::from) else timelog.date
        val currStartTime = if (startText.isNotEmpty()) viewModel.timeFormatter.parse(startText, LocalTime::from) else timelog.startTime
        val currEndTime = if (endText.isNotEmpty()) viewModel.timeFormatter.parse(endText, LocalTime::from) else timelog.endTime

        if (dateText.isNotEmpty() || startText.isNotEmpty() || endText.isNotEmpty()) {
            val newTimelog = Timelog(
                timelogId = timelog.timelogId,
                activityId = timelog.activityId,
                date = currDate,
                startTime = currStartTime,
                endTime = currEndTime
            )
            return insertNewTimelog(newTimelog, false)
        }
        return false
    }

    private suspend fun insertNewTimelog(newTimelog: Timelog, isNew: Boolean): Boolean {
        val isOverlapping = viewModel.checkForOverlap(newTimelog)

        return if (isOverlapping) {
            withContext(Dispatchers.Main) {
                Toast.makeText(requireContext(), "Time range overlaps with another log on that date. Please try again.", Toast.LENGTH_LONG).show()
            }
            Log.d(
                "LogsFragment",
                "Time range (${newTimelog.startTime.format(viewModel.timeFormatter)} - ${newTimelog.endTime.format(viewModel.timeFormatter)}) overlaps with another time log on that date (${newTimelog.date})."
            )
            false
        } else {
            if (isNew) {
                viewModel.addTimelog(newTimelog)
            } else {
                viewModel.updateTimelog(newTimelog)
            }
            true
        }
    }

    private fun deleteTimelog(view: View, timelog: Timelog) {
        viewModel.deleteTimelog(timelog)
        Log.i(
            "LogsFragment",
            "Deleted Timelog: $timelog"
        )

        Snackbar.make(view, "Timelog for ${timelog.date.format(viewModel.dateFormatter)} deleted", Snackbar.LENGTH_LONG)
            .setAction("UNDO") {
                viewModel.addTimelog(timelog)
                Log.i(
                    "LogsFragment",
                    "Delete undone - added back timelog for ${timelog.date.format(viewModel.dateFormatter)}"
                )
            }
            .show()
    }

}