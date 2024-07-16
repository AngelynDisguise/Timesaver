package com.example.timesaver.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.RecyclerView
import com.example.timesaver.ActivityTimeLogListAdapter
import com.example.timesaver.CircularButtonView
import com.example.timesaver.MainActivity
import com.example.timesaver.MainViewModel
import com.example.timesaver.MainViewModelFactory
import com.example.timesaver.R
import com.example.timesaver.database.Activity
import com.example.timesaver.database.ActivityTimeLog
import com.example.timesaver.database.TimeLog
import java.time.Duration
import java.time.LocalDate

// Dummies
var dummyNumActivities: Int = 5
var dummyActivityLabels: List<String> = listOf("Work", "LitAI", "Break", "Reddit", "Cooking") // Size MUST match numActivities
val dummyLogs: List<ActivityTimeLog> = listOf(ActivityTimeLog(Activity(0,"Break", Duration.ZERO), TimeLog(0,0, LocalDate.now(), Duration.ZERO)))

class MainFragment : Fragment() {

    private lateinit var activityTimeLogListAdapter: ActivityTimeLogListAdapter

    private lateinit var circularButton: CircularButtonView // outer: activity wheel, inner: play/pause button
    private lateinit var refreshButton: ImageView

    private lateinit var timerText: TextView

    private var numActivities: Int = dummyNumActivities
    private var activityLabels: List<String> = dummyActivityLabels
    private var activityTimeLogs: List<ActivityTimeLog> = dummyLogs

    private lateinit var currentActivityTimeLog: ActivityTimeLog
    private var currentActivityIndex: Int = 0

    // Set up shared view model
    private val viewModel: MainViewModel by activityViewModels {
        MainViewModelFactory((requireActivity() as MainActivity).repository)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_main, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Draw Circular Button (the "timesaver" :P)
        circularButton = view.findViewById(R.id.circular_button_view)

        circularButton.outerCircleSections = numActivities // temp value: remove later
        circularButton.sectionLabels = activityLabels // temp value: remove later

        // Set icon back to pause if stopwatch is running
        if (viewModel.stopwatchIsRunning()) {
            circularButton.changeButton()
        }

        // Setup timer text and refresh
        timerText = view.findViewById(R.id.timer_text_view)
        refreshButton = view.findViewById(R.id.refresh_button_image_view)

        refreshButton.setOnClickListener {
            if (viewModel.timeHasElapsed()) {
                if(viewModel.stopwatchIsRunning()) {
                    circularButton.changeButton()
                }
                viewModel.resetStopwatch()
                Toast.makeText(requireContext(), "Stopwatch Reset", Toast.LENGTH_SHORT).show()
            }
        }

        // Setup RecyclerView and List Adapters
        activityTimeLogListAdapter = ActivityTimeLogListAdapter(activityTimeLogs)
        val activityTimeLogRecyclerView: RecyclerView = view.findViewById(R.id.activity_timelog_recycler_view)
        activityTimeLogRecyclerView.adapter = activityTimeLogListAdapter

        // Click Play/Pause
        circularButton.setOnInnerCircleClickListener {
            if (circularButton.isPlaying()) {
                Toast.makeText(requireContext(), "Stopwatch Paused", Toast.LENGTH_SHORT).show()
                viewModel.pauseStopwatch()
            } else {
                val playState: String = if (viewModel.timeHasElapsed()) "Resumed" else "Started"
                Toast.makeText(requireContext(), "Stopwatch $playState", Toast.LENGTH_SHORT).show()
                viewModel.startStopwatch()
            }
        }

        // Click Activity Wheel
        circularButton.setOnOuterCircleClickListener { section ->
            Toast.makeText(requireContext(), "Activity Button \"${activityLabels[section]}\" clicked", Toast.LENGTH_SHORT).show()
            //Toast.makeText(requireContext(), "Outer circle section $section clicked", Toast.LENGTH_SHORT).show()
            if (viewModel.stopwatchIsRunning()) {
                // TODO(): Save previous activity time elapsed
            }
        }

        // Update stopwatch UI
        viewModel.elapsedTime.observe(viewLifecycleOwner) { duration ->
            val hours = duration.toHours()
            val minutes = duration.toMinutes() % 60
            val seconds = duration.seconds % 60
            val text = "%02d:%02d:%02d".format(hours, minutes, seconds)
            timerText.text = text
        }

        // Update Activity Buttons UI
        viewModel.activities.observe(viewLifecycleOwner) { activities ->
            // TODO(): Update wheel with activity names
            if (activities.isNotEmpty()) {
                val activityNames: MutableList<String> = mutableListOf()
                for (a in activities) {
                    activityNames.add(a.activityName)
                }
                activityLabels = activityNames
                numActivities = activities.size
                circularButton.invalidate() // draw new buttons
            } else {
                Log.d(
                    "MainFragment",
                    "ERROR: Received empty list of activities"
                )
            }
        }

        // Update Activity time logs list UI
        viewModel.todaysLogs.observe(viewLifecycleOwner) { logs ->
            // TODO(): Update listview with activity names and time elapsed
            if (logs.isNotEmpty()) {
                activityTimeLogs = logs
                activityTimeLogListAdapter.notifyDataSetChanged()
            } else {
                Log.d(
                    "MainFragment",
                    "WARNING: Received empty list of time logs"
                )
            }
        }
    }

}