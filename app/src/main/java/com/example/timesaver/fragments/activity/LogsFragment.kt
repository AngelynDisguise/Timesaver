package com.example.timesaver.fragments.activity

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.timesaver.MainActivity
import com.example.timesaver.MainViewModel
import com.example.timesaver.R

class LogsFragment : Fragment() {

    private lateinit var viewModel: MainViewModel

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

        val textView: TextView = view.findViewById(R.id.logs_text_view)
        val i: Long? = arguments?.getLong("activityId")
        i?.let {
            Log.d(
                "LogsFragment",
                "got id = $i"
            )
            viewModel.getActivityTimelog(i)
            viewModel.currentActivityTimelog.observe(viewLifecycleOwner) {
                Log.d(
                    "LogsFragment",
                    "got activity = $it"
                )
                val text = "Timelog for Activity: ${it.activity.activityName}"
                textView.text = text
            }
        } ?: let {
            Log.d(
                "LogsFragment",
                "got nothing :("
            )
        }
    }

}