package com.example.timesaver.fragments.activity.logs

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.os.Bundle
import android.text.Editable
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.example.timesaver.R
import com.example.timesaver.ViewModelFactory
import com.example.timesaver.database.Timelog
import com.example.timesaver.database.TimesaverDatabase
import com.example.timesaver.database.TimesaverRepository
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

    private val dao by lazy { TimesaverDatabase.getDatabase(requireContext()).timesaverDao() }
    private val viewModel by lazy {
         ViewModelProvider(
            this,
            ViewModelFactory(
                TimesaverRepository(dao)
            )
        ) [LogsViewModel::class.java]
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

        // Set up adapter
        adapter = TimelogListAdapter()
        val recyclerView: RecyclerView = view.findViewById(R.id.logs_recycler_view)
        recyclerView.adapter = adapter

        // Get activity id from bundle sent by ActivityFragment
        val activityId: Long? = arguments?.getLong("activityId")
        activityId?.let {
            Log.i(
                "LogsFragment",
                "Received Activity ID: $activityId"
            )

            // Trigger the flow of paged timelogs from the database
            viewModel.setActivityId(it)

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

        } ?: let {
            Log.d(
                "LogsFragment",
                "Expected an activity id from ActivityFragment bundle but got nothing :("
            )
        }

        // Change chronological order of timelogs list
        val sortButton: ImageView = view.findViewById(R.id.timelog_sort_icon)
        sortButton.setOnClickListener {
            viewModel.toggleSortOrder()
        }

        // Drop down edit layout
        adapter.setOnClickTimelogListener { viewHolder ->
            adapter.toggleChildRow(viewHolder) // input UI is saved while fragment is alive
        }

        // Delete timelog
        adapter.setOnClickDeleteListener { deleteView, timelog ->
            deleteTimelog(deleteView, timelog)
        }

        // Save edit input if valid
        adapter.setOnClickConfirmListener { viewHolder, timelog ->
            viewLifecycleOwner.lifecycleScope.launch {
                if (validateEditInput(viewHolder, timelog, true)) {
                    adapter.toggleChildRow(viewHolder)
                } // else do nothing
            }
        }

        // Edit texts
        adapter.setOnEditDateListener {
            openDatePicker(it)
        }
        adapter.setOnEditStartTimeListener { viewHolder, startTime, endTime ->
            openTimePicker(startTime, endTime, viewHolder, true)
        }
        adapter.setOnEditEndTimeListener { viewHolder, startTime, endTime ->
            openTimePicker(startTime, endTime, viewHolder, false)
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
        viewHolder: TimelogListAdapter.ViewHolder,
        isStart: Boolean
    ) {
        val startEditText: EditText = viewHolder.startTimeEditText
        val endEditText: EditText = viewHolder.endTimeEditText
        val (currEditText, otherEditText) = if (isStart) (startEditText to endEditText) else (endEditText to startEditText)

        fun parseTime(text: Editable): LocalTime = viewModel.timeFormatter.parse(text, LocalTime::from)
        fun showInvalidTimeToast(message: String) {
            Toast.makeText(requireContext(), "$message Try again.", Toast.LENGTH_LONG).show()
            startEditText.text.clear()
            endEditText.text.clear()
        }

        // Open Time picker
        TimePickerDialog(
            requireContext(),
            { _, hourOfDay, minute ->
                // Take new time and put it on the selected edit text
                val newTime: LocalTime = LocalTime.of(hourOfDay, minute)
                currEditText.setText(newTime.format(viewModel.timeFormatter))

                // Get both start and end times for validation
                val (newStartTime, newEndTime) = when {
                    isStart && otherEditText.text.isEmpty() -> newTime to endTime
                    isStart -> newTime to parseTime(otherEditText.text)
                    otherEditText.text.isEmpty() -> startTime to newTime
                    else -> parseTime(otherEditText.text) to newTime
                }

                // Validate time range - reject if invalid
                when {
                    isStart && newStartTime.isAfter(newEndTime) -> showInvalidTimeToast("Start time cannot be later than end time.")
                    !isStart && newEndTime.isBefore(newStartTime) -> showInvalidTimeToast("End time cannot be earlier than start time.")
                    else -> let {
                        viewHolder.modifiedTotalTime.text = adapter.formatDuration(Duration.between(startTime, endTime))
                        if (otherEditText.text.isEmpty()) {
                            otherEditText.setText(otherEditText.hint)
                        }
                    }
                }
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            false
        ).show()
    }

    private suspend fun validateEditInput(viewHolder: TimelogListAdapter.ViewHolder, timelog: Timelog, isNew: Boolean): Boolean {
        val dateText = viewHolder.dateEditText.text
        val startText = viewHolder.startTimeEditText.text
        val endText = viewHolder.endTimeEditText.text

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
            return insertNewTimelog(newTimelog, isNew)
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