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
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.timesaver.MainActivity
import com.example.timesaver.MainViewModel
import com.example.timesaver.R
import com.example.timesaver.database.Activity

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

        builder.setPositiveButton("OK") { _, _ ->
            val newActivity = input.text.toString()
            if (newActivity.isNotEmpty()) {
                addActivityToList(newActivity)
            }
        }
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }

        builder.show()
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
        viewModel.addNewActivity(newActivity)

        Log.i(
            "ActivityMenuFragment",
            "Added Activity: ${newActivity.activityName}"
        )
    }

}