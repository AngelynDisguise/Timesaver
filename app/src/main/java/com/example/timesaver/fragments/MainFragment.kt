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
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.RecyclerView
import com.example.timesaver.CircularButtonView
import com.example.timesaver.MainActivity
import com.example.timesaver.MainViewModel
import com.example.timesaver.MainViewModelFactory
import com.example.timesaver.R
import com.example.timesaver.UILogListAdapter
import com.example.timesaver.database.Activity
import com.example.timesaver.database.ActivityTimelog
import com.example.timesaver.database.Timelog
//import com.example.timesaver.database.ActivityTimeLog
//import com.example.timesaver.database.TimeLog
import java.time.Duration
import java.time.LocalDate
import kotlin.time.toKotlinDuration

data class UILog (
    val id: Long,
    val activityName: String,
    val totalTime: Duration,
    val color: Int
)

class MainFragment : Fragment() {

    // Shared view model with MainActivity
    private val viewModel: MainViewModel by activityViewModels {
        MainViewModelFactory((requireActivity() as MainActivity).repository)
    }

    // Timelog list UI
    private lateinit var adapter: UILogListAdapter

    // UI things
    private lateinit var circularButton: CircularButtonView // outer: activity wheel, inner: play/pause button
    private lateinit var doneButton: Button
    private lateinit var refreshButton: ImageView
    private lateinit var currentActivityText: TextView
    private lateinit var timerText: TextView

    // Note: There will always be at least 1 activity
    private lateinit var activities: List<Activity>
    private val uiLogs: MutableMap<Long, UILog> = mutableMapOf()

    private var currentActivityIndex: Int = -1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_main, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d(
            "MainFragment",
            "MainFragment Created"
        )

        initViews(view)
        initCircularButton(view) // Draw the "timesaver" :P
        initListAdapter(view)
        initUI(view)

        // Listeners
        //refreshButton.setOnClickListener { rollbackTime() }
        circularButton.setOnInnerCircleClickListener(onClickPlayPause)
        circularButton.setOnOuterCircleClickListener(onClickActivity)

        // Update stopwatch
        viewModel.elapsedTime.observe(viewLifecycleOwner) { duration: Duration ->
            if (currentActivityIndex > -1) {
                timerText.text = formatStopwatchDuration(duration)
            }
        }

        // Update activity wheel UI and list UI
        viewModel.combinedData.observe(viewLifecycleOwner) { (acts, logs): Pair<List<Activity>, List<Timelog>> ->
            if (acts.isNotEmpty()) {
                // Update list UI
                activities = acts
                circularButton.outerCircleSections = acts.size
                circularButton.setLabels(acts.map { it.activityName })

                Log.d(
                    "MainFragment",
                    "Received ${acts.size} activities from LiveData: $acts\nReceived ${logs.size} timelogs from LiveData: $logs\n..."
                )

                // Populate map for list UI
                for (i in acts.indices) {
                    val a: Activity = acts[i]
                    val t: List<Timelog> = logs.filter { it.activityId == a.activityId }

                    // If activity has timelogs for today, add to map
                    if (t.isNotEmpty()) {
                        Log.d(
                            "MainFragment",
                            "Processing activity $i: ${a.activityName} with ${t.size} time logs: $t\n..."
                        )

                        val timeElapsed: List<Duration> = t.map { Duration.between(it.startTime, it.endTime) }
                        val totalTimeElapsed: Duration = timeElapsed.fold(Duration.ZERO) { acc, time -> acc.plus(time) } // sumOf doesn't work :/

                        uiLogs[a.activityId] = UILog(
                            id = a.activityId,
                            activityName = a.activityName,
                            totalTime = totalTimeElapsed,
                            color = circularButton.getSectionColor(i)
                        )
                    } else {
                        Log.d(
                            "MainFragment",
                            "No timelogs for activity $i: ${a.activityName}\n..."
                        )
                    }
                }

                if (uiLogs.isNotEmpty()) {
                    Log.d(
                        "MainFragment",
                        "SUCCESS: Updated UI time logs of size ${uiLogs.size}: $uiLogs\n..."
                    )
                } else {
                    Log.d(
                        "MainFragment",
                        "WARNING: uiLogs is empty - no activities have timelogs for today\n..."
                    )
                }

                // Update list UI
                adapter.submitList(uiLogs.values.toList())

                // Reveal arrow UI if uiLog list overflows
                if (uiLogs.size > 5) {
                    val arrow = view.findViewById<ImageView>(R.id.arrow_down_image_view)
                    arrow.visibility = VISIBLE
                }
            } else {
                Log.d(
                    "MainFragment",
                    "ERROR: Received empty list of activities."
                )
            }
        }
    }

    override fun onPause() {
        super.onPause()
        Log.d(
            "MainFragment",
            "MainFragment Paused"
        )
    }

    override fun onResume() {
        super.onResume()
        Log.d(
            "MainFragment",
            "MainFragment Resumed"
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(
            "MainFragment",
            "MainFragment Destroyed"
        )
    }

    private fun initViews(view: View) {
        refreshButton = view.findViewById(R.id.refresh_button_image_view)
        currentActivityText = view.findViewById(R.id.current_activity_text_view)
        timerText = view.findViewById(R.id.timer_text_view)
    }

    private fun initCircularButton(view: View) {
        circularButton = view.findViewById(R.id.circular_button_view)
        circularButton.padding = 30f // any smaller will push glow off the canvas
    }

    private fun initListAdapter(view: View) {
        adapter = UILogListAdapter()
        val activityTimeLogRecyclerView: RecyclerView = view.findViewById(R.id.activity_timelog_recycler_view)
        activityTimeLogRecyclerView.adapter = adapter
    }

    private fun initUI(view: View) {
        // Config fix: Set icon back to pause if stopwatch is running
        if (viewModel.stopwatchIsRunning()) {
            circularButton.changeToPauseButton()
        }

        // Reveal arrow if timelog list overflows
        if (uiLogs.size > 5) {
            val arrow = view.findViewById<ImageView>(R.id.arrow_down_image_view)
            arrow.visibility = VISIBLE
        }
    }

    private fun clearUI() {
        circularButton.changeToPlayButton()
        viewModel.resetStopwatch()

        timerText.text = formatStopwatchDuration(Duration.ZERO)

        doneButton.visibility = GONE
        val text = "Select an Activity"
        currentActivityText.text = text
        currentActivityText.setTextColor(requireContext().getColorResCompat(android.R.attr.textColorPrimary))
        currentActivityText.setTypeface(Typeface.create(currentActivityText.typeface, Typeface.NORMAL))
        circularButton.turnOffGlowing()
        currentActivityIndex = -1
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
    private val onClickPlayPause = {
        Log.d("MainFragment", "(onClickPlayPause) currentActivityIndex: $currentActivityIndex")

        // Start/Resume/Pause the time for the current activity
        if(currentActivityIndex > -1) {
            if (circularButton.isPlaying()) {
                Toast.makeText(requireContext(), "Stopwatch Paused", Toast.LENGTH_SHORT).show()
                viewModel.pauseStopwatch()
                circularButton.changeToPlayButton()
            } else {
                val playState: String = if (viewModel.timeHasElapsed()) "Resumed" else "Started"
                Toast.makeText(requireContext(), "Stopwatch $playState", Toast.LENGTH_SHORT).show()
                viewModel.startStopwatch()
                circularButton.changeToPauseButton()
            }
        } else {
            Toast.makeText(requireContext(), "Select an Activity", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * @param i the selected activity index
     */
    private fun startActivity(i: Int) {
        // Update current activity
        currentActivityIndex = i
        timerText.text = formatStopwatchDuration(Duration.ZERO)
        if (viewModel.timeHasElapsed()) {
            viewModel.resetStopwatch()
        }
        viewModel.startStopwatch()

        currentActivityText.text = activities[i].activityName
        currentActivityText.setTextColor(circularButton.getSectionColor(i))
        currentActivityText.setTypeface(Typeface.create(currentActivityText.typeface, Typeface.BOLD))
    }
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
    // Click an Activity Button
    private val onClickActivity = { selectedIndex: Int ->
        Log.d(
            "MainFragment", "Previous selection: $currentActivityIndex (${if (currentActivityIndex > -1) activities[currentActivityIndex].activityName else "none"}), Current selection: $selectedIndex (${activities[selectedIndex].activityName})"
        )

        // A different button was selected
        if (selectedIndex != currentActivityIndex) {
            // Switching when stopwatch has time
            if (currentActivityIndex > -1 && viewModel.timeHasElapsed()) {
                // Switching while stopwatch is running
                if (viewModel.stopwatchIsRunning()) {
                    switchingWhileRunning(selectedIndex)
                }
            } else {
                /*
                Switching while no time passed:
                1) None selected and no time passed
                2) None selected and time passed (impossible)
                3) Selected and no time passed
                 */
                startActivity(selectedIndex)

                // Case 3
                if (currentActivityIndex > -1) {
                    Toast.makeText(
                        requireContext(),
                        "Switched to Activity \"${activities[selectedIndex].activityName}\"",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    // Case 1 and 2
                    Toast.makeText(
                        requireContext(),
                        "Selected Activity \"${activities[selectedIndex].activityName}\"",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        } else { // Same button was selected
            Toast.makeText(
                requireContext(),
                "Activity \"${activities[selectedIndex].activityName}\" unselected",
                Toast.LENGTH_SHORT
            ).show()

            // Button no longer glowing - button unselected
            if (!circularButton.isGlowing()) {
                // Unselected and time has elapsed
                if (viewModel.timeHasElapsed()) {
                    // TODO()
                    //finishActivity(currentActivityIndex)
                }
                clearUI()
            }
            // else do nothing
        }
    }

private fun switchingWhileRunning(i: Int) {
    Log.d(
        "MainFragment",
        "User chose a different activity while current is running. Sending Alert Dialogue."
    )
    if (!viewModel.warningSuppressed) {
        viewModel.stopStopwatch()

        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Activity in Progress")
            .setMessage("Finish current activity and switch to another? Progress will be saved.")

        builder.setPositiveButton("Yes") { _, _ ->
            startActivity(i)
        }
        builder.setNegativeButton("No") { _, _ ->
            viewModel.startStopwatch()
            circularButton.rollbackTouch(currentActivityIndex)
        }
        val dialog: AlertDialog = builder.create()
        dialog.show()
    } else {
        startActivity(i)
    }
}

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
    private fun formatStopwatchDuration(duration: Duration): String {
        val hours = duration.toHours()
        val minutes = duration.toMinutes() % 60
        val seconds = duration.seconds % 60
        return "%02d:%02d:%02d".format(hours, minutes, seconds)
    }

    @ColorInt
    private fun Context.getColorResCompat(@AttrRes id: Int): Int {
            val resolvedAttr = TypedValue()
            this.theme.resolveAttribute(id, resolvedAttr, true)
            val colorRes = resolvedAttr.run { if (resourceId != 0) resourceId else data }
            return ContextCompat.getColor(this, colorRes)
    }


}