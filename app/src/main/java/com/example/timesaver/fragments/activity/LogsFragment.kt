package com.example.timesaver.fragments.activity

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.timesaver.MainActivity
import com.example.timesaver.MainViewModel
import com.example.timesaver.R
import com.example.timesaver.database.Timelog

class LogsFragment : Fragment() {

    private lateinit var viewModel: MainViewModel
    private lateinit var adapter: TimelogListAdapter

    override fun onAttach(context: Context) {
        super.onAttach(context)
        viewModel = (requireActivity() as MainActivity).viewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_logs, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get activity id from bundle sent by ActivityFragment
        val activityId: Long? = arguments?.getLong("activityId")

        activityId?.let {
            Log.i(
                "LogsFragment",
                "Received Activity: $activity"
            )

            // Set up adapter
            adapter = TimelogListAdapter()
            val recyclerView: RecyclerView = view.findViewById(R.id.logs_recycler_view)
            recyclerView.adapter = adapter

            // Get ActivityTimelog (activity with all timelogs in history) from database
            viewModel.getActivityTimelog(activityId)

            viewModel.currentActivityTimelog.observe(viewLifecycleOwner) { activityTimelog ->
                activityTimelog?.let {
                    Log.i(
                        "LogsFragment",
                        "Received ActivityTimelog: $activityTimelog"
                    )

                    // Sort timelogs by date and start time, newest to oldest
                    val sortedTimelogs = activityTimelog.timelogs
                        .sortedWith(compareByDescending<Timelog> { it.date }
                            .thenByDescending { it.startTime })
                        .toList()

                    // Update adapter
                    adapter.submitList(sortedTimelogs)
                } ?: let {
                    Log.e(
                        "LogsFragment",
                        "Expected the ActivityTimelog from database but got nothing :("
                    )
                }
            }
        } ?: let {
            Log.d(
                "LogsFragment",
                "Expected an activity id from ActivityFragment bundle but got nothing :("
            )
        }

        adapter.setOnClickTimelogListener { vh ->
            // TODO(): Save input UI?
            adapter.toggleChildRow(vh)
        }

        adapter.setOnClickDeleteListener { v, t ->
            // TODO() Delete timelog
        }

        adapter.setOnClickConfirmListener { vh, t ->
            // TODO() Update timelog
            adapter.toggleChildRow(vh)
        }
    }


}