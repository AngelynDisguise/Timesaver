package com.example.timesaver.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.fragment.app.Fragment
import com.example.timesaver.MainActivity
import com.example.timesaver.MainViewModel
import com.example.timesaver.R

class SettingsFragment : Fragment() {

    private lateinit var viewModel: MainViewModel

    override fun onAttach(context: Context) {
        super.onAttach(context)
        viewModel = (requireActivity() as MainActivity).viewModel
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(
            "SettingsFragment",
            "SettingsFragment Created"
        )
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
            "SettingsFragment VIEW Created"
        )

        checkButtons(view)
        setSwitchWarningSettingListeners(view)
        setPauseWhenSwitchSettingsListeners(view)

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

    private fun checkButtons(view: View) {
        if (viewModel.warnBeforeSwitch) {
            view.findViewById<RadioButton>(R.id.settings_warn_before_switch_yes_button).isChecked = true
        } else {
            view.findViewById<RadioButton>(R.id.settings_warn_before_switch_no_button).isChecked = true
        }

        if (viewModel.pauseBeforeStart) {
            view.findViewById<RadioButton>(R.id.settings_pause_before_start_yes_button).isChecked = true
        } else {
            view.findViewById<RadioButton>(R.id.settings_pause_before_start_no_button).isChecked = true
        }

    }

    private fun setSwitchWarningSettingListeners(view: View) {
        view.findViewById<RadioButton>(R.id.settings_warn_before_switch_yes_button).setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                viewModel.warnBeforeSwitch = true
                Log.d(
                    "SettingsFragment",
                    "WarningBeforeSwitch setting set to false"
                )
            }
        }
        view.findViewById<RadioButton>(R.id.settings_warn_before_switch_no_button).setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                viewModel.warnBeforeSwitch = false
                Log.d(
                    "SettingsFragment",
                    "WarnBeforeSwitch setting set to true"
                )
            }
        }
    }

    private fun setPauseWhenSwitchSettingsListeners(view: View) {
        view.findViewById<RadioButton>(R.id.settings_pause_before_start_yes_button).setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                viewModel.pauseBeforeStart = true
                Log.d(
                    "SettingsFragment",
                    "PauseBeforeStart setting set to false"
                )
            }
        }
        view.findViewById<RadioButton>(R.id.settings_pause_before_start_no_button).setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                viewModel.pauseBeforeStart = false
                Log.d(
                    "SettingsFragment",
                    "PauseBeforeStart setting set to false"
                )
            }
        }
    }

}