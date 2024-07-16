package com.example.timesaver

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
import com.example.timesaver.database.ActivityTimeLogs

var numActivities = 5
var activityLabels = listOf("Work", "LitAI", "Break", "Reddit", "Cooking") // Size MUST match numActivities

class MainFragment : Fragment() {

    private lateinit var circularButton: CircularButtonView // outer: activity wheel, inner: play/pause button
    private lateinit var refreshButton: ImageView

    private lateinit var timerText: TextView

    private lateinit var activityTimeLogs: List<ActivityTimeLogs>
    private lateinit var currentActivityTimeLog: ActivityTimeLogs
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
        circularButton = view.findViewById(R.id.circularButton)

        circularButton.outerCircleSections = numActivities // temp value: remove later
        circularButton.sectionLabels = activityLabels // temp value: remove later

        // Set icon back to pause if stopwatch is running
        if (viewModel.stopwatchIsRunning()) {
            circularButton.changeButton()
        }

        // Setup timer text and refresh
        timerText = view.findViewById(R.id.timerText)
        refreshButton = view.findViewById(R.id.refreshButton)

        refreshButton.setOnClickListener {
            if (viewModel.timeHasElapsed()) {
                if(viewModel.stopwatchIsRunning()) {
                    circularButton.changeButton()
                }
                viewModel.resetStopwatch()
                Toast.makeText(requireContext(), "Stopwatch Reset", Toast.LENGTH_SHORT).show()
            }
        }

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
        viewModel.activities.observe(viewLifecycleOwner) {
            // TODO(): Update wheel with activity names
            if (it.isNotEmpty()) {
                val activities: MutableList<String> = mutableListOf()
                for (activity in it) {
                    activities.add(activity.activityName)
                }
                activityLabels = activities
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
        viewModel.todaysLogs.observe(viewLifecycleOwner) {
            // TODO(): Update listview with activity names and time elapsed
            if (it.isNotEmpty()) {
                val logs: MutableList<ActivityTimeLogs> = mutableListOf()
                for (activityTimeLog in it) {
                    logs.add(activityTimeLog)
                }
                activityTimeLogs = logs
                // notifyDataSetChanged()
            } else {
                Log.d(
                    "MainFragment",
                    "WARNING: Received empty list of time logs"
                )
            }
        }
    }

//    override fun onResume() {
//        super.onResume()
//        if (viewModel.stopWatchIsRunning()) {
//            circularButton.icon = circularButton.pauseIcon
//            circularButton.invalidate()
//        }
//    }

}