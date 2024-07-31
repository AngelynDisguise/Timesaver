package com.example.timesaver.fragments.mainfragment

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
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.timesaver.CircularButtonView
import com.example.timesaver.MainActivity
import com.example.timesaver.MainViewModel
import com.example.timesaver.R
import com.example.timesaver.database.Activity
import com.example.timesaver.database.Timelog
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime

data class UILog (
    val id: Long,
    val activityName: String,
    val totalTime: Duration,
    val color: Int
)

class MainFragment : Fragment() {

    private lateinit var viewModel: MainViewModel

    // Timelog list UI
    private lateinit var adapter: UILogListAdapter

    // UI things
    private lateinit var circularButton: CircularButtonView // outer: activity wheel, inner: play/pause button
    private lateinit var refreshButton: ImageView
    private lateinit var arrowImageView: ImageView
    private lateinit var currentActivityText: TextView
    private lateinit var timerText: TextView

    // Note: There will always be at least 1 activity
    private lateinit var activities: List<Activity>
    private val uiLogs: MutableMap<Long, UILog> = mutableMapOf()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        viewModel = (requireActivity() as MainActivity).viewModel
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(
            "MainFragment",
            "MainFragment Created"
        )
    }

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
            "MainFragment VIEW Created"
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
            if (viewModel.currentActivityIndex > -1) {
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

                Log.i(
                    "MainFragment",
                    "Received ${acts.size} activities from LiveData: $acts\nReceived ${logs.size} timelogs from LiveData: $logs\n..."
                )

                // Populate map for list UI
                for (i in acts.indices) {
                    val a: Activity = acts[i]
                    val t: List<Timelog> = logs.filter { it.activityId == a.activityId }

                    // If activity has timelogs for today, add to map
                    if (t.isNotEmpty()) {
//                        Log.d(
//                            "MainFragment",
//                            "Processing activity $i: ${a.activityName} with ${t.size} time logs: $t\n..."
//                        )

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
                    Log.i(
                        "MainFragment",
                        "Updated UI time logs of size ${uiLogs.size}: $uiLogs\n..."
                    )
                } else {
                    Log.w(
                        "MainFragment",
                        "uiLogs is empty - no activities have timelogs for today\n..."
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

    override fun onStop() {
        super.onStop()
        Log.d(
            "MainFragment",
            "MainFragment Stopped"
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
        arrowImageView = view.findViewById(R.id.arrow_down_image_view)
        currentActivityText = view.findViewById(R.id.current_activity_text_view)
        timerText = view.findViewById(R.id.timer_text_view)
    }

    private fun initCircularButton(view: View) {
        circularButton = view.findViewById(R.id.circular_button_view)
        circularButton.padding = 30f // any smaller will push glow off the canvas
    }

    private fun initListAdapter(view: View) {
        adapter = UILogListAdapter()
        val activityTimeLogRecyclerView: RecyclerView = view.findViewById(R.id.ui_log_recycler_view)
        activityTimeLogRecyclerView.adapter = adapter
    }

    private fun initUI(view: View) {
        // Config fix: Set icon back to pause if stopwatch is running
        if (viewModel.stopwatchIsRunning() && viewModel.buttonIsSelected()) {
            circularButton.changeToPauseButton()
            circularButton.rollbackTouch(viewModel.currentActivityIndex)
        }
        checkListOverflow()
    }

    private fun checkListOverflow() {
        // Reveal arrow if timelog list overflows (seems like max is 4 for now)
        if (uiLogs.size > 4) {
            arrowImageView.visibility = VISIBLE
        }
    }

    private fun clearUI() {
        circularButton.changeToPlayButton()
        viewModel.resetStopwatch()

        timerText.text = formatStopwatchDuration(Duration.ZERO)

        val text = "Select an Activity"
        currentActivityText.text = text
        currentActivityText.setTextColor(requireContext().getColorResCompat(android.R.attr.textColorPrimary))
        currentActivityText.setTypeface(Typeface.create(currentActivityText.typeface, Typeface.NORMAL))
        circularButton.turnOffGlowing()
        viewModel.currentActivityIndex = -1
    }

    private val onClickPlayPause = {
        Log.d("MainFragment", "(onClickPlayPause) currentActivityIndex: $viewModel.currentActivityIndex")

        // Start/Resume/Pause the time for the current activity
        if(viewModel.buttonIsSelected()) {
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

    private val onClickActivity = { selectedIndex: Int ->
        val selectedDifferentButton = viewModel.buttonIsSelected()
//        Log.d(
//            "MainFragment",
//            "Previous selection: $viewModel.currentActivityIndex (${if (selectedDifferentButton) activities[viewModel.currentActivityIndex].activityName else "none"}), Current selection: $selectedIndex (${activities[selectedIndex].activityName})"
//        )

        // A different button was selected
        if (selectedIndex != viewModel.currentActivityIndex) {
            // Switching when stopwatch has time
            if (selectedDifferentButton && viewModel.timeHasElapsed()) {
                // Switching while stopwatch is running
                if (viewModel.stopwatchIsRunning()) {
                    switchingWhileRunning(selectedIndex)
                } else {
                    // Switching while stopwatch is paused and has time
                    startActivity(selectedIndex)
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
                if (selectedDifferentButton) {
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
        } else { // Same button was selected - unselecting button
            Toast.makeText(
                requireContext(),
                "Activity \"${activities[selectedIndex].activityName}\" unselected",
                Toast.LENGTH_SHORT
            ).show()

            // Unselected and time has elapsed
            if (viewModel.timeHasElapsed()) {
                // This is the only other case that saves without switching
                saveActivityTimelog(selectedIndex)
            }
            clearUI()
        }
    }

    /**
     * Helper function for onClickActivity().
     * @param i the index of the activity to switch to
     */
    private fun switchingWhileRunning(i: Int) {
        Log.d(
            "MainFragment",
            "User chose a different activity while current is running. WarnWhenSwitch = ${viewModel.warnBeforeSwitch}"
        )
        if (viewModel.warnBeforeSwitch) {
            viewModel.stopStopwatch()

            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("Activity in Progress")
                .setMessage("Finish current activity and switch to another? Progress will be saved.")

            builder.setPositiveButton("Yes") { _, _ ->
                startActivity(i)
            }
            builder.setNegativeButton("No") { _, _ ->
                viewModel.startStopwatch()
                circularButton.rollbackTouch(viewModel.currentActivityIndex)
            }
            val dialog: AlertDialog = builder.create()
            dialog.show()
        } else {
            startActivity(i)
        }
    }

    /**
     * @param i the index of the activity to start
     */
    private fun startActivity(i: Int) {
        // Update current activity
        viewModel.currentActivityIndex = i
        timerText.text = formatStopwatchDuration(Duration.ZERO)
        if (viewModel.timeHasElapsed()) {
            saveActivityTimelog(i)
            viewModel.resetStopwatch()
        }

        Log.d(
            "MainFragment",
            "Starting new activity \"${activities[i]}\". PauseBeforeStart = ${viewModel.pauseBeforeStart}"
        )

        if (viewModel.pauseBeforeStart){
            circularButton.changeToPlayButton()
        } else{
            viewModel.startStopwatch()
            circularButton.changeToPauseButton()
        }

        currentActivityText.text = activities[i].activityName
        currentActivityText.setTextColor(circularButton.getSectionColor(i))
        currentActivityText.setTypeface(Typeface.create(currentActivityText.typeface, Typeface.BOLD))
    }

    /**
     * Saves
     * @param i the index of the activity to save
     * Note: the reason the param isn't an Activity itself is
     * because I also need the color, whose index matches to the activity.
     */
    private fun saveActivityTimelog(i: Int) {
        if (viewModel.timeHasElapsed()) { // Double-check
            Log.d(
                "MainFragment",
                "...\nSaving new Timelog for Activity \"${activities[i].activityName}\"..."
            )

            val id = activities[i].activityId
            val endTime: LocalTime = LocalTime.now()


            val newTimelog = Timelog(
                timelogId = 0, // auto-generated
                activityId = id,
                date = LocalDate.now(),
                startTime = viewModel.getStartTime(),
                endTime = endTime
            )

            // Save to database
            if (id in uiLogs) {
                Log.d(
                    "MainFragment",
                    "Updating existing time log..."
                )
                viewModel.updateTimelog(newTimelog)
            } else {
                Log.d(
                    "MainFragment",
                    "Inserting new time log..."
                )
                viewModel.saveNewTimelog(newTimelog)
            }

            val timeElapsed: Duration = viewModel.stopStopwatch() + (uiLogs[id]?.totalTime ?: Duration.ZERO)

            // Add or update timelog UI in map
            uiLogs[id] = UILog(
                id = id,
                activityName = activities[i].activityName,
                color = circularButton.getSectionColor(i),
                totalTime = timeElapsed
            )

            // Update list UI
            adapter.submitList(uiLogs.values.toList())
            checkListOverflow()

            Log.i(
                "MainFragment",
                "Successfully saved Timelog for Activity \"${activities[i].activityName}\" to database! \nUpdated UI Logs (size: ${uiLogs.size}: $uiLogs\n..."
            )
        } else {
            Log.e(
                "MainFragment",
                "ERROR: Tried to save activity but no time elapsed.\n"
            )
        }
    }

//    private fun rollbackTime() {
//        if (viewModel.timeHasElapsed()) {
//            private fun finishActivity(activityIndex: Int) {
//                Toast.makeText(requireContext(), "Finished ${activities[activityIndex].activityName}", Toast.LENGTH_SHORT).show()
//                saveActivityTimeLog(activityIndex)
//                clearUI()
//            }

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