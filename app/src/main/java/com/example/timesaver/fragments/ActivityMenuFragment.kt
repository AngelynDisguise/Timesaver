package com.example.timesaver.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.timesaver.R

class ActivityMenuFragment : Fragment() {

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