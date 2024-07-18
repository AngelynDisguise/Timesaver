package com.example.timesaver.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
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
import org.checkerframework.checker.index.qual.GTENegativeOne
import java.time.Duration
import java.time.LocalDate

// Dummies
var dummyNumActivities: Int = 5
var dummyActivityLabels: List<String> = listOf("Work", "LitAI", "Break", "Reddit", "Cooking") // Size MUST match numActivities
val dummyLogs: List<ActivityTimeLog> = listOf(
    ActivityTimeLog(Activity(0,"Work", Duration.ZERO), TimeLog(0,0, LocalDate.now(), Duration.ofMinutes(12))),
    ActivityTimeLog(Activity(0,"LitAI", Duration.ZERO), TimeLog(0,0, LocalDate.now(), Duration.ofHours(1))),
    ActivityTimeLog(Activity(0,"Break", Duration.ZERO), TimeLog(0,0, LocalDate.now(), Duration.ZERO)),
    ActivityTimeLog(Activity(0,"Reddit", Duration.ZERO), TimeLog(0,0, LocalDate.now(), Duration.ofMinutes(30))),
    ActivityTimeLog(Activity(0,"Cooking", Duration.ZERO), TimeLog(0,0, LocalDate.now(), Duration.ofSeconds(5023))), // repeated
    ActivityTimeLog(Activity(0,"Work", Duration.ZERO), TimeLog(0,0, LocalDate.now(), Duration.ofMinutes(12))),
    ActivityTimeLog(Activity(0,"LitAI", Duration.ZERO), TimeLog(0,0, LocalDate.now(), Duration.ofHours(1))),
    ActivityTimeLog(Activity(0,"Break", Duration.ZERO), TimeLog(0,0, LocalDate.now(), Duration.ZERO)),
    ActivityTimeLog(Activity(0,"Reddit", Duration.ZERO), TimeLog(0,0, LocalDate.now(), Duration.ofMinutes(30))),
    ActivityTimeLog(Activity(0,"Cooking", Duration.ZERO), TimeLog(0,0, LocalDate.now(), Duration.ofSeconds(5023)))
)

class MainFragment : Fragment() {

    private lateinit var activityTimeLogListAdapter: ActivityTimeLogListAdapter

    private lateinit var circularButton: CircularButtonView // outer: activity wheel, inner: play/pause button
    private lateinit var refreshButton: ImageView
    private lateinit var doneButton: Button
    private lateinit var currentActivityText: TextView
    private lateinit var timerText: TextView

    private var numActivities: Int = dummyNumActivities
    private var activityLabels: List<String> = dummyActivityLabels
    private var activityTimeLogs: List<ActivityTimeLog> = dummyLogs

    private var currentActivityTimeLog: ActivityTimeLog? = null
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

        // Draw and setup Circular Button (the "timesaver" :P)
        circularButton = view.findViewById(R.id.circular_button_view)
        circularButton.outerCircleSections = numActivities
        circularButton.sectionLabels = activityLabels
        circularButton.padding = 28f

        // Set icon back to pause if stopwatch is running
        if (viewModel.stopwatchIsRunning()) {
            circularButton.changeButton()
        }

        // Setup views
        timerText = view.findViewById(R.id.timer_text_view)
        refreshButton = view.findViewById(R.id.refresh_button_image_view)
        doneButton = view.findViewById(R.id.done_button_view)
        currentActivityText = view.findViewById(R.id.current_activity_text_view)

        // Setup RecyclerView and List Adapters
        activityTimeLogListAdapter = ActivityTimeLogListAdapter(activityTimeLogs)
        val activityTimeLogRecyclerView: RecyclerView = view.findViewById(R.id.activity_timelog_recycler_view)
        activityTimeLogRecyclerView.adapter = activityTimeLogListAdapter

        // Reveal arrow if list overflows
        if (activityTimeLogs.size > 5) {
            val arrow = view.findViewById<ImageView>(R.id.arrow_down_image_view)
            arrow.visibility = View.VISIBLE
        }

        // Click Refresh
        refreshButton.setOnClickListener {
            if (viewModel.timeHasElapsed()) {
                if(viewModel.stopwatchIsRunning()) {
                    circularButton.changeButton()
                }
                viewModel.resetStopwatch()
                Toast.makeText(requireContext(), "Stopwatch Reset", Toast.LENGTH_SHORT).show()
            }
        }

        // Click Done
        doneButton.setOnClickListener {
            if(viewModel.stopwatchIsRunning()) {
                circularButton.changeButton()
            }

            // TODO(): Save timelog
            viewModel.resetStopwatch()

            doneButton.visibility = View.GONE
            val text = "Select an Activity"
            currentActivityText.text = text

            Toast.makeText(requireContext(), "Activity finished", Toast.LENGTH_SHORT).show()
        }

        // Click Play/Pause
        circularButton.setOnInnerCircleClickListener {
            if (currentActivityTimeLog != null) {
                if (circularButton.isPlaying()) {
                    Toast.makeText(requireContext(), "Stopwatch Paused", Toast.LENGTH_SHORT).show()
                    viewModel.pauseStopwatch()
                } else {
                    val playState: String = if (viewModel.timeHasElapsed()) "Resumed" else "Started"
                    Toast.makeText(requireContext(), "Stopwatch $playState", Toast.LENGTH_SHORT).show()
                    viewModel.startStopwatch()
                }
                circularButton.changeButton()
            } else {
                Toast.makeText(requireContext(), "Select an Activity", Toast.LENGTH_SHORT).show()
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