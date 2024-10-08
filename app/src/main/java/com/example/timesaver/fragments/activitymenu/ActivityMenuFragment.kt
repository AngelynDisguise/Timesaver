package com.example.timesaver.fragments.activitymenu

import android.content.Context
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.timesaver.MainActivity
import com.example.timesaver.MainViewModel
import com.example.timesaver.R
import com.example.timesaver.database.Activity
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class ActivityMenuFragment : Fragment() {

    private lateinit var adapter: ActivityListAdapter
    private lateinit var viewModel: MainViewModel

    override fun onAttach(context: Context) {
        super.onAttach(context)
        viewModel = (requireActivity() as MainActivity).viewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_activity_menu, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d(
            "ActivityMenuFragment",
            "ActivityMenuFragment VIEW Created"
        )

        adapter = ActivityListAdapter()
        val activityRecyclerView: RecyclerView = view.findViewById(R.id.activity_recycler_view)
        activityRecyclerView.adapter = adapter

        // Recieve Activity list from database
        viewModel.activities.observe(viewLifecycleOwner) {
            adapter.submitList(it)
            Log.i(
                "ActivityMenuFragment",
                "Recieved ${it.size} activities: $it"
            )
        }

        // Add activity
        val addButton: Button = view.findViewById(R.id.add_activity_button)
        addButton.setOnClickListener {
            if (adapter.itemCount < 8) {
                showAddActivityDialog()
            } else {
                Toast.makeText(requireContext(), "Max. 8 Activities", Toast.LENGTH_SHORT).show()
            }
        }

        // Click on Activity goes to ActivityFragment
        // Navigate to Activity screen if long clicked
        adapter.setOnClickActivityListener { _, id ->
            val bundle = Bundle().apply {
                putParcelable("activity", adapter.currentList.find { it.activityId == id })
            }

            Log.d(
                "MainFragment",
                "Sending bundle to ActivityFragment: $bundle"
            )

            findNavController().navigate(R.id.action_activity_menu_fragment_to_activity_fragment, bundle)
        }

        // Click More Options opens menu
        adapter.setOnClickOptionsListener { v, a ->
            showPopupMenu(
                view = v,
                activity = a
            )
        }
    }

    private fun showPopupMenu(view: View, activity: Activity) {
        val popupMenu = PopupMenu(view.context, view)
        popupMenu.inflate(R.menu.activity_options_menu)

        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.rename -> {
                    showRenameActivityDialog(activity)
                    true
                }
                R.id.delete -> {
                    if (adapter.itemCount > 1) { // at least 1 activity must exist
                        deleteActivity(view, activity)
                    } else {
                        Toast.makeText(requireContext(), "Must have at least one activity.", Toast.LENGTH_SHORT).show()
                    }
                    true
                }
                R.id.open -> {
                    val bundle = Bundle().apply {
                        putParcelable("activity", activity)
                    }

                    Log.d(
                        "ActivityMenuFragment",
                        "Sending bundle to ActivityFragment: $bundle"
                    )

                    findNavController().navigate(R.id.action_activity_menu_fragment_to_activity_fragment, bundle)
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
    }

    private fun showAddActivityDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Enter a new activity name:")

        val views = setDialogViews()
        val input = views.first
        val container = views.second

        container.addView(input)
        builder.setView(container)

        builder.setPositiveButton("OK", null)
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }

        val dialog = builder.create()

        dialog.setOnShowListener { dialogInterface ->
            val positiveButton = (dialogInterface as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.setOnClickListener {
                val newActivityName = input.text.toString()
                val notDuplicate: Boolean = adapter.currentList.none { it.activityName == newActivityName }
                if (newActivityName.isNotEmpty() && notDuplicate) {
                    addActivity(newActivityName)
                    dialog.dismiss()
                } else if (!notDuplicate) {
                    Toast.makeText(requireContext(), "\'$newActivityName\' already exists. Please try again.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Please enter a new activity name.", Toast.LENGTH_SHORT).show()
                }
            }
        }

        dialog.show()
    }

    private fun showRenameActivityDialog(oldActivity: Activity) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Enter a new activity name:")

        val views = setDialogViews()
        val input = views.first
        val container = views.second

        input.hint = oldActivity.activityName

        container.addView(input)
        builder.setView(container)

        builder.setPositiveButton("OK", null)
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }

        val dialog = builder.create()

        dialog.setOnShowListener { dialogInterface ->
            val positiveButton = (dialogInterface as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.setOnClickListener {
                val newActivityName = input.text.toString()
                val notDuplicate: Boolean = adapter.currentList.none { it.activityName == newActivityName }
                if (newActivityName.isNotEmpty() && notDuplicate) {
                    updateActivity(newActivityName, oldActivity)
                    dialog.dismiss()
                } else if (!notDuplicate) {
                    Toast.makeText(requireContext(), "\'$newActivityName\' already exists. Please try again.", Toast.LENGTH_SHORT).show()
                    input.text.clear()
                } else {
                    Toast.makeText(requireContext(), "Please enter a new activity name.", Toast.LENGTH_SHORT).show()
                }
            }
        }

        dialog.show()
    }

    private fun setDialogViews(): Pair<EditText, FrameLayout> {
        val input = EditText(requireContext())
        input.inputType = InputType.TYPE_CLASS_TEXT

        val container = FrameLayout(requireContext())
        val params = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        val dp = 20
        val px = (dp * requireContext().resources.displayMetrics.density).toInt()
        params.leftMargin = px
        params.rightMargin = px
        input.layoutParams = params

        return Pair(input, container)
    }

    private fun addActivity(activityName: String) {
        val newActivity = Activity(
            activityId = 0,
            activityName = activityName
        )

        Log.d(
            "ActivityMenuFragment",
            "Adding new Activity..."
        )

        adapter.submitList(adapter.currentList + newActivity)
        viewModel.addActivity(newActivity)

        Log.i(
            "ActivityMenuFragment",
            "Added Activity: $newActivity"
        )
    }

    private fun updateActivity(newActivityName: String, oldActivity: Activity) {
        val updatedActivity = Activity(
            activityId = oldActivity.activityId,
            activityName = newActivityName
        )

        Log.d(
            "ActivityMenuFragment",
            "Updating Activity..."
        )

        val updatedList = adapter.currentList.toMutableList()
        val i = updatedList.indexOfFirst { it.activityId == oldActivity.activityId }
        require(i > -1 && i < updatedList.size) { "Out of bounds: Tried to update an activity that doesn't exist in the list. "} // just wanted to try this :D
        updatedList[i] = oldActivity


        adapter.submitList(updatedList)
        viewModel.updateActivity(updatedActivity)

        Log.i(
            "ActivityMenuFragment",
            "Updated Activity: $updatedActivity"
        )
    }

    private fun deleteActivity(view: View, activity: Activity) {
        CoroutineScope(Dispatchers.IO).launch {
            val data = async { viewModel.getTimelogsForActivity(activity.activityId) }
            val timelogs = data.await()

            Log.d(
                "ActivityMenuFragment",
                "Got timelogs (${timelogs.size}) for activity ${activity.activityName}: ${timelogs.map { it.timelogId }}"
            )
            viewModel.deleteActivity(activity)
            Log.i(
                "ActivityMenuFragment",
                "Deleted Activity: ${activity.activityName}"
            )
            // Confirm and provide Undo option
            Snackbar.make(view, "${activity.activityName} deleted", Snackbar.LENGTH_LONG)
                .setAction("UNDO") {
                    viewModel.addActivity(activity)
                    for (t in timelogs) { viewModel.addTimelog(t) }
                    Log.i(
                        "ActivityMenuFragment",
                        "Delete undone. Added back timelogs (${timelogs.size}) for activity ${activity.activityName}: ${timelogs.map { it.timelogId }}"
                    )
                }
                .show()
        }
    }

    /* Debug stuff */

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(
            "ActivityMenuFragment",
            "ActivityMenuFragment Created"
        )
    }

    override fun onPause() {
        super.onPause()
        Log.d(
            "ActivityMenuFragment",
            "ActivityMenuFragment Paused"
        )
    }

    override fun onResume() {
        super.onResume()
        Log.d(
            "ActivityMenuFragment",
            "ActivityMenuFragment Resumed"
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(
            "ActivityMenuFragment",
            "ActivityMenuFragment Destroyed"
        )
    }

}