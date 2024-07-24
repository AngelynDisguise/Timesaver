package com.example.timesaver.fragments

import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.RecyclerView
import com.example.timesaver.ActivityTimeLogListAdapter
import com.example.timesaver.CircularButtonView
import com.example.timesaver.MainActivity
import com.example.timesaver.MainViewModel
import com.example.timesaver.MainViewModelFactory
import com.example.timesaver.R
import com.example.timesaver.database.Activity
import com.example.timesaver.database.ActivityTimelog
import com.example.timesaver.database.Timelog
//import com.example.timesaver.database.ActivityTimeLog
//import com.example.timesaver.database.TimeLog
import java.time.Duration
import java.time.LocalDate
import kotlin.time.toKotlinDuration

data class UILog (
    val activityName: String,
    val totalTime: Duration,
    val color: Int
)

class MainFragment : Fragment() {

    var warningSuppressed: Boolean = false

    // Shared view model with MainActivity
    private val viewModel: MainViewModel by activityViewModels {
        MainViewModelFactory((requireActivity() as MainActivity).repository)
    }

    // Timelog list UI
    private lateinit var activityTimeLogListAdapter: ActivityTimeLogListAdapter

    // UI things
    private lateinit var circularButton: CircularButtonView // outer: activity wheel, inner: play/pause button
    private lateinit var doneButton: Button
    private lateinit var refreshButton: ImageView
    private lateinit var currentActivityText: TextView
    private lateinit var timerText: TextView
    private lateinit var timeDiffText: TextView

    // Note: There will always be at least 1 activity and shall always be 1:1 with timelogs
    private lateinit var activityTimelogs: List<ActivityTimelog>
    private val UILogs: MutableMap<Long, UILog> = mutableMapOf()

    private var currentActivityIndex: Int = -1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_main, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Init views
        refreshButton = view.findViewById(R.id.refresh_button_image_view)
        doneButton = view.findViewById(R.id.done_button_view)
        currentActivityText = view.findViewById(R.id.current_activity_text_view)
        timerText = view.findViewById(R.id.timer_text_view)
        timeDiffText = view.findViewById(R.id.time_diff_text_view)

        // Draw and setup Circular Button (the "timesaver" :P)
        circularButton = view.findViewById(R.id.circular_button_view)
        circularButton.padding = 30f // any smaller will push glow off the canvas

        // Setup RecyclerView and List Adapters
        activityTimeLogListAdapter = ActivityTimeLogListAdapter(UILogs.values.toList())
        val activityTimeLogRecyclerView: RecyclerView = view.findViewById(R.id.activity_timelog_recycler_view)
        activityTimeLogRecyclerView.adapter = activityTimeLogListAdapter


        // Config fix: Set icon back to pause if stopwatch is running
        if (viewModel.stopwatchIsRunning()) {
            circularButton.changeToPauseButton()
        }

        // Reveal arrow if timelog list overflows
        if (UILogs.size > 5) {
            val arrow = view.findViewById<ImageView>(R.id.arrow_down_image_view)
            arrow.visibility = VISIBLE
        }

//        // Listeners
//        refreshButton.setOnClickListener { rollbackTime() }
//        doneButton.setOnClickListener { finishActivity(currentActivityIndex) }
//        circularButton.setOnInnerCircleClickListener(onClickPlayPause)
//        circularButton.setOnOuterCircleClickListener(onClickActivity)
//
//        // Update stopwatch
//        viewModel.elapsedTime.observe(viewLifecycleOwner) { duration: Duration ->
//            if (currentActivityIndex > -1) {
//                timerText.text = formatStopwatchDuration(duration + timelogs[currentActivityIndex].timeLog.timeElapsed)
//                val text = "(Diff: " + formatStopwatchDuration(duration) + ")"
//                timeDiffText.text = text
//            }
//        }

        // Update activity wheel UI and list UI
        viewModel.activityTimelogs.observe(viewLifecycleOwner) { ats: List<ActivityTimelog> ->
            if (ats.isNotEmpty()) {
                // Update list UI
                activityTimelogs = ats
                circularButton.outerCircleSections = ats.size
                circularButton.setLabels(ats.map { it.activity.activityName })

                Log.d(
                    "MainFragment",
                    "Received activities from LiveData: $ats"
                )

                // Populate map for list UI
                for (i in ats.indices) {
                    val a: Activity = ats[i].activity
                    val t: List<Timelog> = ats[i].timelogs

                    val logTime: List<Duration> = t.map { Duration.between(it.startTime, it.endTime) }
                    val totalTime: Duration = logTime.fold(Duration.ZERO) { acc, time -> acc.plus(time) } // sumOf doesn't work :/

                    UILogs[a.activityId] = UILog(
                        activityName = a.activityName,
                        totalTime = totalTime,
                        color = circularButton.getSectionColor(i)
                    )
                }

                Log.d(
                    "MainFragment",
                    "Updated UI time logs: $UILogs"
                )

                // Update list UI
                //activityTimeLogListAdapter.submitList(UITimelogs.values.toList())
            } else {
                Log.d(
                    "MainFragment",
                    "ERROR: Received empty list of activities."
                )
            }
        }
    }

//    private fun rollbackTime() {
//        if (viewModel.timeHasElapsed()) {
//            private fun finishActivity(activityIndex: Int) {
//                Toast.makeText(requireContext(), "Finished ${activities[activityIndex].activityName}", Toast.LENGTH_SHORT).show()
//                saveActivityTimeLog(activityIndex)
//                clearUI()
//            }
//
//            // Click an Activity Button
//            private val onClickActivity = { selectedIndex: Int ->
//                Log.d(
//                    "MainFragment", "Previous selection: $currentActivityIndex (${if (currentActivityIndex > -1) activities[currentActivityIndex].activityName else "none"}), Current selection: $selectedIndex (${activities[selectedIndex].activityName})"
//                )
//
//                // Different button pressed (from none or a different activity)
//                if (selectedIndex != currentActivityIndex) {
//
//                    // Switching while progress made
//                    if (currentActivityIndex > -1 && viewModel.timeHasElapsed()) {
//
//                        // Switching while running (progress made)
//                        if (viewModel.stopwatchIsRunning()) {
//                            Log.d(
//                                "MainFragment",
//                                "User chose a different activity while current is running. Sending Alert Dialogue."
//                            )
//                            if (!warningSuppressed) {
//                                warnUser(selectedIndex)
//                            } else { // Warning supressed
//                                finishActivity(currentActivityIndex)
//                                switchActivity(selectedIndex)
//                            }
//                        } else { // Switching while not running, but progress made
//                            finishActivity(currentActivityIndex)
//                            switchActivity(selectedIndex)
//                        }
//                    } else { // 1) None selected and no time passed, 2) None selected and time passed (impossible), 3) Selected and no time passed
//                        if (currentActivityIndex > -1) {
//                            Toast.makeText(
//                                requireContext(),
//                                "Switched to Activity \"${activities[selectedIndex].activityName}\"",
//                                Toast.LENGTH_SHORT
//                            ).show()
//                        } else {
//                            Toast.makeText(
//                                requireContext(),
//                                "Selected Activity \"${activities[selectedIndex].activityName}\"",
//                                Toast.LENGTH_SHORT
//                            ).show()
//                        }
//                        switchActivity(selectedIndex)
//                    }
//                } else { // Same button pressed
//                    if (circularButton.isGlowing()) {
//                        Toast.makeText(
//                            requireContext(),
//                            "Selected Activity \"${activities[selectedIndex].activityName}\"",
//                            Toast.LENGTH_SHORT
//                        ).show()
//                        switchActivity(selectedIndex)
//                    } else {
//                        Toast.makeText(
//                            requireContext(),
//                            "Activity \"${activities[selectedIndex].activityName}\" unselected",
//                            Toast.LENGTH_SHORT
//                        ).show()
//                        if (viewModel.timeHasElapsed()) {
//                            finishActivity(currentActivityIndex)
//                        } else {
//                            clearUI()
//                        }
//                    }
//
//                }
//            }
//
//            private fun warnUser(selectedIndex: Int) {
//                val builder = AlertDialog.Builder(requireContext())
//                builder.setTitle("Activity in Progress")
//                    .setMessage("Finish current activity and switch to another? Progress will be saved.")
//
//                builder.setPositiveButton("Yes") { _, _ ->
//                    finishActivity(currentActivityIndex)
//                    switchActivity(selectedIndex)
//                }
//                builder.setNegativeButton("No") { _, _ ->
//                    circularButton.rollbackTouch(currentActivityIndex)
//                }
//                val dialog: AlertDialog = builder.create()
//                dialog.show()
//            }
//
//            private val onClickPlayPause = {
//                Log.d("MainFragment", "(onClickPlayPause) currentActivityIndex: $currentActivityIndex")
//
//                // Start/Resume/Pause the time for the current activity
//                if(currentActivityIndex > -1) {
//                    if (circularButton.isPlaying()) {
//                        Toast.makeText(requireContext(), "Stopwatch Paused", Toast.LENGTH_SHORT).show()
//                        viewModel.pauseStopwatch()
//                        circularButton.changeToPlayButton()
//                    } else {
//                        val playState: String = if (viewModel.timeHasElapsed()) "Resumed" else "Started"
//                        Toast.makeText(requireContext(), "Stopwatch $playState", Toast.LENGTH_SHORT).show()
//                        viewModel.startStopwatch()
//                        circularButton.changeToPauseButton()
//                    }
//                } else {
//                    Toast.makeText(requireContext(), "Select an Activity", Toast.LENGTH_SHORT).show()
//                }
//            }
//
//            private fun switchActivity(newActivityIndex: Int) {
//                // Update current activity
//                currentActivityIndex = newActivityIndex
//
//                val timeElapsed: Duration = timelogs[newActivityIndex].timeLog.timeElapsed
//                timerText.text = formatStopwatchDuration(timeElapsed)
//                var text = "(Diff: " + formatStopwatchDuration(Duration.ZERO) + ")"
//                timeDiffText.text = text
//
//                currentActivityText.text = activities[newActivityIndex].activityName
//                currentActivityText.setTextColor(circularButton.getSectionColor(newActivityIndex))
//                currentActivityText.setTypeface(Typeface.create(currentActivityText.typeface, Typeface.BOLD))
//
//                doneButton.visibility = VISIBLE
//            }
//
//            private fun saveActivityTimeLog(activityIndex: Int) {
//                if (viewModel.timeHasElapsed()) {
//                    // Stop stopwatch, record time elapsed
//                    val oldElapsedTime: Duration = timelogs[activityIndex].timeLog.timeElapsed // 0 if new log
//                    val timeElapsed: Duration = viewModel.stopStopwatch()
//
//                    val newTimeLog = TimeLog(
//                        timeLogId = timelogs[activityIndex].timeLog.timeLogId,
//                        activityId = activities[activityIndex].activityId,
//                        date = LocalDate.now(),
//                        timeElapsed = timeElapsed + oldElapsedTime
//                    )
//
//                    val newActivityTimeLog = ActivityTimeLog(activities[activityIndex], newTimeLog)
//                    val newColor = circularButton.getSectionColor(activityIndex)
//
//                    Log.d(
//                        "MainFragment",
//                        "Attempting to save: $newTimeLog with color $newColor"
//                    )
//
//                    // Save or update new time
//                    if (oldElapsedTime == Duration.ZERO) {
//                        Log.d(
//                            "MainFragment",
//                            "1\nlogs = $adapterLogs\ncolors = $colors"
//                        )
//                        colors.add(newColor)
//                        Log.d(
//                            "MainFragment",
//                            "2:\nlogs = $adapterLogs\ncolors = $colors"
//                        )
//                        viewModel.saveNewTimeLog(newTimeLog)
//
//                        adapterLogs.add(newActivityTimeLog)
//                        activityTimeLogListAdapter.notifyItemInserted(adapterLogs.size-1)
//
//                        Log.d(
//                            "MainFragment",
//                            "Insert successful!"
//                        )
//                    } else {
//                        viewModel.updateTimeLog(newTimeLog)
//
//                        // Update timelogs list
//                        val i = adapterLogs.indexOfFirst { it.activity.activityId == newTimeLog.activityId }
//                        if (i != -1) adapterLogs[i] = newActivityTimeLog // color won't change, so no need to update colors
//                        activityTimeLogListAdapter.notifyItemChanged(i)
//
//                        Log.d(
//                            "MainFragment",
//                            "Update successful!"
//                        )
//                    }
//
//                    Log.d(
//                        "MainFragment",
//                        "Adapter after save:\nlogs = $adapterLogs\ncolors = $colors"
//                    )
//                } else {
//                    Log.d(
//                        "MainFragment",
//                        "WARNING: Tried to save but no time elapsed."
//                 //
//    private fun rollbackTime() {
//        if (viewModel.timeHasElapsed()) {
//            private fun finishActivity(activityIndex: Int) {
//                Toast.makeText(requireContext(), "Finished ${activities[activityIndex].activityName}", Toast.LENGTH_SHORT).show()
//                saveActivityTimeLog(activityIndex)
//                clearUI()
//            }
//
//            // Click an Activity Button
//            private val onClickActivity = { selectedIndex: Int ->
//                Log.d(
//                    "MainFragment", "Previous selection: $currentActivityIndex (${if (currentActivityIndex > -1) activities[currentActivityIndex].activityName else "none"}), Current selection: $selectedIndex (${activities[selectedIndex].activityName})"
//                )
//
//                // Different button pressed (from none or a different activity)
//                if (selectedIndex != currentActivityIndex) {
//
//                    // Switching while progress made
//                    if (currentActivityIndex > -1 && viewModel.timeHasElapsed()) {
//
//                        // Switching while running (progress made)
//                        if (viewModel.stopwatchIsRunning()) {
//                            Log.d(
//                                "MainFragment",
//                                "User chose a different activity while current is running. Sending Alert Dialogue."
//                            )
//                            if (!warningSuppressed) {
//                                warnUser(selectedIndex)
//                            } else { // Warning supressed
//                                finishActivity(currentActivityIndex)
//                                switchActivity(selectedIndex)
//                            }
//                        } else { // Switching while not running, but progress made
//                            finishActivity(currentActivityIndex)
//                            switchActivity(selectedIndex)
//                        }
//                    } else { // 1) None selected and no time passed, 2) None selected and time passed (impossible), 3) Selected and no time passed
//                        if (currentActivityIndex > -1) {
//                            Toast.makeText(
//                                requireContext(),
//                                "Switched to Activity \"${activities[selectedIndex].activityName}\"",
//                                Toast.LENGTH_SHORT
//                            ).show()
//                        } else {
//                            Toast.makeText(
//                                requireContext(),
//                                "Selected Activity \"${activities[selectedIndex].activityName}\"",
//                                Toast.LENGTH_SHORT
//                            ).show()
//                        }
//                        switchActivity(selectedIndex)
//                    }
//                } else { // Same button pressed
//                    if (circularButton.isGlowing()) {
//                        Toast.makeText(
//                            requireContext(),
//                            "Selected Activity \"${activities[selectedIndex].activityName}\"",
//                            Toast.LENGTH_SHORT
//                        ).show()
//                        switchActivity(selectedIndex)
//                    } else {
//                        Toast.makeText(
//                            requireContext(),
//                            "Activity \"${activities[selectedIndex].activityName}\" unselected",
//                            Toast.LENGTH_SHORT
//                        ).show()
//                        if (viewModel.timeHasElapsed()) {
//                            finishActivity(currentActivityIndex)
//                        } else {
//                            clearUI()
//                        }
//                    }
//
//                }
//            }
//
//            private fun warnUser(selectedIndex: Int) {
//                val builder = AlertDialog.Builder(requireContext())
//                builder.setTitle("Activity in Progress")
//                    .setMessage("Finish current activity and switch to another? Progress will be saved.")
//
//                builder.setPositiveButton("Yes") { _, _ ->
//                    finishActivity(currentActivityIndex)
//                    switchActivity(selectedIndex)
//                }
//                builder.setNegativeButton("No") { _, _ ->
//                    circularButton.rollbackTouch(currentActivityIndex)
//                }
//                val dialog: AlertDialog = builder.create()
//                dialog.show()
//            }
//
//            private val onClickPlayPause = {
//                Log.d("MainFragment", "(onClickPlayPause) currentActivityIndex: $currentActivityIndex")
//
//                // Start/Resume/Pause the time for the current activity
//                if(currentActivityIndex > -1) {
//                    if (circularButton.isPlaying()) {
//                        Toast.makeText(requireContext(), "Stopwatch Paused", Toast.LENGTH_SHORT).show()
//                        viewModel.pauseStopwatch()
//                        circularButton.changeToPlayButton()
//                    } else {
//                        val playState: String = if (viewModel.timeHasElapsed()) "Resumed" else "Started"
//                        Toast.makeText(requireContext(), "Stopwatch $playState", Toast.LENGTH_SHORT).show()
//                        viewModel.startStopwatch()
//                        circularButton.changeToPauseButton()
//                    }
//                } else {
//                    Toast.makeText(requireContext(), "Select an Activity", Toast.LENGTH_SHORT).show()
//                }
//            }
//
//            private fun switchActivity(newActivityIndex: Int) {
//                // Update current activity
//                currentActivityIndex = newActivityIndex
//
//                val timeElapsed: Duration = timelogs[newActivityIndex].timeLog.timeElapsed
//                timerText.text = formatStopwatchDuration(timeElapsed)
//                var text = "(Diff: " + formatStopwatchDuration(Duration.ZERO) + ")"
//                timeDiffText.text = text
//
//                currentActivityText.text = activities[newActivityIndex].activityName
//                currentActivityText.setTextColor(circularButton.getSectionColor(newActivityIndex))
//                currentActivityText.setTypeface(Typeface.create(currentActivityText.typeface, Typeface.BOLD))
//
//                doneButton.visibility = VISIBLE
//            }
//
//            private fun saveActivityTimeLog(activityIndex: Int) {
//                if (viewModel.timeHasElapsed()) {
//                    // Stop stopwatch, record time elapsed
//                    val oldElapsedTime: Duration = timelogs[activityIndex].timeLog.timeElapsed // 0 if new log
//                    val timeElapsed: Duration = viewModel.stopStopwatch()
//
//                    val newTimeLog = TimeLog(
//                        timeLogId = timelogs[activityIndex].timeLog.timeLogId,
//                        activityId = activities[activityIndex].activityId,
//                        date = LocalDate.now(),
//                        timeElapsed = timeElapsed + oldElapsedTime
//                    )
//
//                    val newActivityTimeLog = ActivityTimeLog(activities[activityIndex], newTimeLog)
//                    val newColor = circularButton.getSectionColor(activityIndex)
//
//                    Log.d(
//                        "MainFragment",
//                        "Attempting to save: $newTimeLog with color $newColor"
//                    )
//
//                    // Save or update new time
//                    if (oldElapsedTime == Duration.ZERO) {
//                        Log.d(
//                            "MainFragment",
//                            "1\nlogs = $adapterLogs\ncolors = $colors"
//                        )
//                        colors.add(newColor)
//                        Log.d(
//                            "MainFragment",
//                            "2:\nlogs = $adapterLogs\ncolors = $colors"
//                        )
//                        viewModel.saveNewTimeLog(newTimeLog)
//
//                        adapterLogs.add(newActivityTimeLog)
//                        activityTimeLogListAdapter.notifyItemInserted(adapterLogs.size-1)
//
//                        Log.d(
//                            "MainFragment",
//                            "Insert successful!"
//                        )
//                    } else {
//                        viewModel.updateTimeLog(newTimeLog)
//
//                        // Update timelogs list
//                        val i = adapterLogs.indexOfFirst { it.activity.activityId == newTimeLog.activityId }
//                        if (i != -1) adapterLogs[i] = newActivityTimeLog // color won't change, so no need to update colors
//                        activityTimeLogListAdapter.notifyItemChanged(i)
//
//                        Log.d(
//                            "MainFragment",
//                            "Update successful!"
//                        )
//                    )
//                }
//            }
//
//            private fun formatStopwatchDuration(duration: Duration): String {
//                val hours = duration.toHours()
//                val minutes = duration.toMinutes() % 60
//                val seconds = duration.seconds % 60
//                return "%02d:%02d:%02d".format(hours, minutes, seconds)
//            }
//
//            @ColorInt
//            private fun Context.getColorResCompat(@AttrRes id: Int): Int {
//                val resolvedAttr = TypedValue()
//                this.theme.resolveAttribute(id, resolvedAttr, true)
//                val colorRes = resolvedAttr.run { if (resourceId != 0) resourceId else data }
//                return ContextCompat.getColor(this, colorRes)
//            }
//            circularButton.changeToPlayButton()
//            viewModel.resetStopwatch()
//
//            val prevTimeText: String = formatStopwatchDuration(timelogs[currentActivityIndex].timeLog.timeElapsed)
//            timerText.text = prevTimeText
//            val zeroDiffText: String = "(Diff: " + formatStopwatchDuration(Duration.ZERO) + ")"
//            timeDiffText.text = zeroDiffText
//
//            Toast.makeText(requireContext(), "Stopwatch Reset", Toast.LENGTH_SHORT).show()
//        }
//    }
//
//
//    private fun clearUI() {
//        circularButton.changeToPlayButton()
//        viewModel.resetStopwatch()
//
//        var text = formatStopwatchDuration(Duration.ZERO)
//        timerText.text = text
//        text = "(Diff: " + formatStopwatchDuration(Duration.ZERO) + ")"
//        timeDiffText.text = text
//
//        doneButton.visibility = GONE
//        text = "Select an Activity"
//        currentActivityText.text = text
//        currentActivityText.setTextColor(requireContext().getColorResCompat(android.R.attr.textColorPrimary))
//        currentActivityText.setTypeface(Typeface.create(currentActivityText.typeface, Typeface.NORMAL))
//        circularButton.turnOffGlowing()
//        currentActivityIndex = -1
//    }
}