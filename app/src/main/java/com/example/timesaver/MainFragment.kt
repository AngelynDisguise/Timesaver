package com.example.timesaver

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.timesaver.database.TimesaverDatabase

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [MainFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MainFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var circularButton: CircularButtonView
    private lateinit var timerText: TextView
    private lateinit var stopwatch: Stopwatch

    // Set up shared view model
    private val viewModel: MainViewModel by activityViewModels {
        MainViewModelFactory((requireActivity() as MainActivity).dao)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_main, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        circularButton = view.findViewById(R.id.circularButton)
        circularButton.outerCircleSections = 8

        timerText = view.findViewById(R.id.timerText)
        stopwatch = Stopwatch(timerText, lifecycleScope)

        circularButton.setOnInnerCircleClickListener {
            if (circularButton.isPlaying()) {
                Toast.makeText(requireContext(), "Timer Paused", Toast.LENGTH_SHORT).show()
                stopwatch.pause()
            } else {
                Toast.makeText(requireContext(), "Timer Playing", Toast.LENGTH_SHORT).show()
                stopwatch.start()
            }
        }

        circularButton.setOnOuterCircleClickListener { section ->
            Toast.makeText(requireContext(), "Outer circle section $section clicked", Toast.LENGTH_SHORT).show()
        }

        // Update Time logs
        viewModel.todaysLogs.observe(viewLifecycleOwner) { logs ->
            // TODO()
        }
    }

    private fun initStopWatch() {

    }

    private fun initActivityButtons() {

    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment MainFragment.
         */
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MainFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}