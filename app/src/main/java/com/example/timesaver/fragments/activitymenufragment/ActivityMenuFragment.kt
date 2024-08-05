package com.example.timesaver.fragments.activitymenufragment

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
import androidx.recyclerview.widget.RecyclerView
import com.example.timesaver.MainActivity
import com.example.timesaver.MainViewModel
import com.example.timesaver.R
import com.example.timesaver.database.Activity
import com.google.android.material.snackbar.Snackbar

class ActivityMenuFragment : Fragment() {

    private lateinit var adapter: ActivityListAdapter
    private lateinit var viewModel: MainViewModel

    override fun onAttach(context: Context) {
        super.onAttach(context)
        viewModel = (requireActivity() as MainActivity).viewModel
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(
            "ActivityMenuFragment",
            "ActivityMenuFragment Created"
        )
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

        // Delete activity
        adapter.setOnClickListener { v, p, a ->
            showPopupMenu(
                view = v,
                position = p,
                activity = a
            )
        }
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

    private fun showAddActivityDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Enter new Activity name:")

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
                val newActivity = input.text.toString()
                val notDuplicate: Boolean = adapter.currentList.none { it.activityName == newActivity }
                if (newActivity.isNotEmpty() && notDuplicate) {
                    addActivityToList(newActivity)
                    dialog.dismiss()
                } else {
                    Toast.makeText(requireContext(), "\'$newActivity\' already exists. Please try again.", Toast.LENGTH_SHORT).show()
                    //input.text.clear()
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

    private fun addActivityToList(activityName: String) {
        val newActivity = Activity(
            activityId = 0,
            activityName = activityName
        )

        Log.i(
            "ActivityMenuFragment",
            "Adding new Activity..."
        )

        adapter.submitList(adapter.currentList + newActivity)
        viewModel.addActivity(newActivity)

        Log.i(
            "ActivityMenuFragment",
            "Added Activity: ${newActivity.activityName}"
        )
    }

    private fun showPopupMenu(view: View, position: Int, activity: Activity) {
        val popupMenu = PopupMenu(view.context, view)
        popupMenu.inflate(R.menu.activity_options_menu)

        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.rename -> {
                    true
                }
                R.id.delete -> {
                    if (adapter.currentList.size > 1) { // at least 1 activity must exist
                        val oldList = adapter.currentList.toList() // copy
                        val newList = oldList.filter { it != activity }
                        adapter.submitList(newList)
                        viewModel.deleteActivity(activity)

                        // Confirm and provide Undo option
                        Snackbar.make(view, "${activity.activityName} deleted", Snackbar.LENGTH_LONG)
                            .setAction("UNDO") {
                                adapter.submitList(oldList)
                                viewModel.addActivity(activity)
                            }
                            .show()
                    } else {
                        Toast.makeText(requireContext(), "Must have at least one activity.", Toast.LENGTH_SHORT).show()
                    }
                    true
                }
                R.id.open -> {
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
    }

}