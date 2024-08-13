package com.example.timesaver.fragments.activity

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.timesaver.MainActivity
import com.example.timesaver.MainViewModel
import com.example.timesaver.R
import com.example.timesaver.database.Timelog
import com.google.android.material.snackbar.Snackbar
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class LogsFragment : Fragment() {

    private lateinit var viewModel: MainViewModel
    private lateinit var adapter: TimelogListAdapter
    private val calendar = Calendar.getInstance()

    // Sort
    private var sortByNewest = true

    // Filter
    // TODO(): By today, daily, weekly, monthly?

    // Formats
    private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MM-dd-yyyy")
    private val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("hh:mm a")

    override fun onAttach(context: Context) {
        super.onAttach(context)
        viewModel = (requireActivity() as MainActivity).viewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_logs, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get activity id from bundle sent by ActivityFragment
        val activityId: Long? = arguments?.getLong("activityId")

        activityId?.let {
            Log.i(
                "LogsFragment",
                "Received Activity: $activity"
            )

            // Set up adapter
            adapter = TimelogListAdapter()
            val recyclerView: RecyclerView = view.findViewById(R.id.logs_recycler_view)
            recyclerView.adapter = adapter

            // Get ActivityTimelog (activity with all timelogs in history) from database
            viewModel.getActivityTimelog(activityId)

            viewModel.currentActivityTimelog.observe(viewLifecycleOwner) { activityTimelog ->
                activityTimelog?.let {
                    Log.i(
                        "LogsFragment",
                        "Received ActivityTimelog: $activityTimelog"
                    )

                    // Sort timelogs by date and start time, newest to oldest
                    val sortedTimelogs = activityTimelog.timelogs
                        .sortedWith(compareByDescending<Timelog> { it.date }
                            .thenByDescending { it.startTime })
                        .toList()

                    // Update adapter
                    adapter.submitList(sortedTimelogs)
                } ?: let {
                    Log.e(
                        "LogsFragment",
                        "Expected the ActivityTimelog from database but got nothing :("
                    )
                }
            }
        } ?: let {
            Log.d(
                "LogsFragment",
                "Expected an activity id from ActivityFragment bundle but got nothing :("
            )
        }

        adapter.setOnClickTimelogListener { viewHolder ->
            adapter.toggleChildRow(viewHolder) // input UI is saved while fragment is alive
        }

        adapter.setOnClickDeleteListener { deleteView, timelog ->
            deleteTimelog(deleteView, timelog)
        }

        adapter.setOnClickConfirmListener { viewHolder, timelog ->
            if (validateEditInput(viewHolder, timelog)) {
                adapter.toggleChildRow(viewHolder)
            }
        }

        adapter.setOnEditDateListener {
            openDatePicker(it)
        }

        adapter.setOnEditStartTimeListener { start, end, se, ee ->
            openTimePicker(start, end, se, ee,true)
        }

        adapter.setOnEditEndTimeListener { start, end, se, ee ->
            openTimePicker(start, end, se, ee, false)
        }
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
        startTime: LocalTime,
        endTime: LocalTime,
        startEditText: EditText,
        endEditText: EditText,
        isStart: Boolean
    ) {
        var currStartTime: LocalTime = startTime
        var currEndTime: LocalTime = endTime

        // Open Time picker
        TimePickerDialog(
            requireContext(),
            { _, hourOfDay, minute ->
                // Take new time and put it on the edit text
                val time: LocalTime = LocalTime.of(hourOfDay, minute)
                val currEditText: EditText = if (isStart) startEditText else endEditText
                currEditText.setText(time.format(timeFormatter))

                // Update start and time values for comparison
                if (isStart) {
                    currStartTime = time // start was changed
                    if (endEditText.text.isNotEmpty()) { // both start and end are changed
                        currEndTime = timeFormatter.parse(endEditText.text, LocalTime::from)
                    }
                } else {
                    currEndTime = time // end was changed
                    if (startEditText.text.isNotEmpty()) { // both start and end are changed
                        currStartTime = timeFormatter.parse(startEditText.text, LocalTime::from)
                    }
                }

                // Validate time range and reject if invalid
                if (isStart && currStartTime.isAfter(currEndTime)) {
                    Toast.makeText(requireContext(), "Start time cannot be later than end time. Try again.", Toast.LENGTH_LONG).show()
                    currEditText.text.clear()
                } else if (!isStart && currEndTime.isBefore(currStartTime)) {
                    Toast.makeText(requireContext(), "End time cannot be earlier than start time. Try again.", Toast.LENGTH_LONG).show()
                    currEditText.text.clear()
                }
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            false
        ).show()
    }

    private fun validateEditInput(viewHolder: TimelogListAdapter.ViewHolder, timelog: Timelog): Boolean {
        val dateText = viewHolder.dateEditText.text
        val startText = viewHolder.startTimeEditText.text
        val endText = viewHolder.endTimeEditText.text

        val currDate = if (dateText.isNotEmpty()) dateFormatter.parse(dateText, LocalDate::from) else timelog.date
        val currStartTime = if (startText.isNotEmpty()) timeFormatter.parse(startText, LocalTime::from) else timelog.startTime
        val currEndTime = if (endText.isNotEmpty()) timeFormatter.parse(endText, LocalTime::from) else timelog.endTime

        if (dateText.isNotEmpty() || startText.isNotEmpty() || endText.isNotEmpty()) {
            val newTimelog = Timelog(
                timelogId = timelog.timelogId,
                activityId = timelog.activityId,
                date = currDate,
                startTime = currStartTime,
                endTime = currEndTime
            )
            return insertNewTimelog(adapter.currentList, newTimelog)
        }
        return false
    }

    private fun insertNewTimelog(timelogs: List<Timelog>, newTimelog: Timelog): Boolean {
        val sortedTimelogs: List<Timelog> = sortTimelogs(timelogs, sortByNewest)
        val overlappingTimelog = sortedTimelogs
            .firstOrNull { it.timelogId != newTimelog.timelogId && it.date == newTimelog.date && it.overlaps(newTimelog) }

        Log.d(
            "LogsFragment",
            "Overlapping timelog = $overlappingTimelog"
        )

        return overlappingTimelog?.let {
            Toast.makeText(requireContext(), "Time range overlaps with another log on that date. " +
                    "Please try again.", Toast.LENGTH_LONG).show()
            Log.d(
                "LogsFragment",
                "Time range (${newTimelog.startTime.format(timeFormatter)} - ${newTimelog.endTime.format(timeFormatter)}) overlaps with another time log on that date (${newTimelog.date})."
            )
            false
        } ?: let {
            val timelogExists: Boolean = timelogs.any { it.timelogId == newTimelog.timelogId }
            if (timelogExists) {
                viewModel.updateTimelog(newTimelog)
            } else {
                viewModel.addTimelog(newTimelog)
            }
            true
        }
    }

    private fun sortTimelogs(timelogs: List<Timelog>, byNewest: Boolean): List<Timelog> {
        return when {
            timelogs.isEmpty() -> emptyList()
            byNewest -> timelogs.sortedWith(compareByDescending<Timelog> { it.date }
                .thenBy { it.startTime })
            else -> timelogs.sortedWith(compareBy<Timelog> { it.date } // ascending
                .thenBy { it.startTime })
        }
    }

    private fun deleteTimelog(view: View, timelog: Timelog) {
        // Confirm and provide Undo option
        val oldList = adapter.currentList
        val newList = adapter.currentList.toMutableList()

        val removed: Boolean = newList.remove(timelog)
        require(removed) { "Tried to remove a timelog that doesn't exist." }

        viewModel.deleteTimelog(timelog)

        Log.i(
            "LogsFragment",
            "Deleted Timelog: $timelog"
        )

        Snackbar.make(view, "Timelog for ${timelog.date.format(dateFormatter)} deleted", Snackbar.LENGTH_LONG)
            .setAction("UNDO") {
                viewModel.addTimelog(timelog)
                Log.i(
                    "LogsFragment",
                    "Delete undone"
                )
            }
            .show()
    }

    private fun updateTimelog(timelog: Timelog) {
        val newList = adapter.currentList.toMutableList()
        val i = newList.indexOfFirst { timelog.timelogId == it.timelogId }
        newList[i] = timelog

        adapter.submitList(newList)
        viewModel.updateTimelog(timelog)

        Log.i(
            "LogsFragment",
            "Updated Timelog: $timelog"
        )
    }

}