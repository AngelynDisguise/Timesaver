package com.example.timesaver.fragments.activity

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
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

        val i: Long? = arguments?.getLong("activityId")
        i?.let {
            Log.d(
                "LogsFragment",
                "got id = $i"
            )
            adapter = TimelogListAdapter()
            val recyclerView: RecyclerView = view.findViewById(R.id.logs_recycler_view)
            recyclerView.adapter = adapter

            viewModel.getActivityTimelog(i)

            viewModel.currentActivityTimelog.observe(viewLifecycleOwner) { act ->
                Log.d(
                    "LogsFragment",
                    "got activity = $act"
                )

                val sortedTimelogs = act.timelogs
                    .sortedWith(compareByDescending<Timelog> { it.date }
                    .thenByDescending { it.startTime })
                    .toList()
                adapter.submitList(sortedTimelogs)
            }
        } ?: let {
            Log.d(
                "LogsFragment",
                "got nothing :("
            )
        }
    }

}