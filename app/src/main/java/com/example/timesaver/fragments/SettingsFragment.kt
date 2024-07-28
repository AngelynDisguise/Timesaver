package com.example.timesaver.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.timesaver.MainActivity
import com.example.timesaver.MainViewModel
import com.example.timesaver.MainViewModelFactory
import com.example.timesaver.R

class SettingsFragment : Fragment() {

    // Shared view model with MainActivity
    private val viewModel: MainViewModel by activityViewModels {
        MainViewModelFactory((requireActivity() as MainActivity).repository)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d(
            "SettingsFragment",
            "SettingsFragment Created"
        )

    }

    override fun onPause() {
        super.onPause()
        Log.d(
            "SettingsFragment",
            "SettingsFragment Paused"
        )
    }

    override fun onResume() {
        super.onResume()
        Log.d(
            "SettingsFragment",
            "SettingsFragment Resumed"
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(
            "SettingsFragment",
            "SettingsFragment Destroyed"
        )
    }



}