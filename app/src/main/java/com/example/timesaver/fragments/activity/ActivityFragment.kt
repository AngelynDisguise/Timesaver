package com.example.timesaver.fragments.activity

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.example.timesaver.MainActivity
import com.example.timesaver.R
import com.example.timesaver.ViewModelFactory
import com.example.timesaver.database.Activity
import com.example.timesaver.database.TimesaverDatabase
import com.example.timesaver.database.TimesaverRepository
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class ActivityFragment : Fragment() {

    private val dao by lazy { TimesaverDatabase.getDatabase(requireContext()).timesaverDao() }
    val viewModel: ActivityViewModel by viewModels {
        ViewModelFactory(TimesaverRepository(dao))
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_activity, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(
            "ActivityFragment",
            "ActivityFragment VIEW Created"
        )

        // Get Activity from bundle sent by ActivityMenu
        @Suppress("DEPRECATION")
        val activity: Activity? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable("activity", Activity::class.java)
        } else {
            arguments?.getParcelable("activity")
        }

        activity?.let {
            Log.i(
                "ActivityFragment",
                "Received activity: $activity"
            )

            // Set current activity
            viewModel.activity = it

            // Setup tabs and horizontal paging for Logs and Insights fragments
            val pagerAdapter = ActivityPagerAdapter(this) // Send activity data to fragments
            val viewPager: ViewPager2 = view.findViewById(R.id.activity_view_pager)
            val tabLayout: TabLayout = view.findViewById(R.id.activity_tab_layout)

            viewPager.adapter = pagerAdapter
            TabLayoutMediator(tabLayout, viewPager) { tab, position ->
                tab.text = when (position) {
                    0 -> "Logs"
                    1 -> "Insights"
                    else -> null
                }
            }.attach()

        } ?: let {
            Log.e(
                "ActivityFragment",
                "Expected an Activity from ActivityMenuFragment bundle but got nothing :("
            )
        }

        // Go back to ActivityMenu when pressing up
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().navigateUp()
            }
        })
    }

    /* Debug stuff */

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(
            "ActivityFragment",
            "ActivityFragment Created"
        )
    }

    override fun onPause() {
        super.onPause()
        Log.d(
            "ActivityFragment",
            "ActivityFragment Paused"
        )
    }

    override fun onResume() {
        super.onResume()
        Log.d(
            "ActivityFragment",
            "ActivityFragment Resumed"
        )
        // Change toolbar title to the selected activity name
        viewModel.activity?.let {
            (requireActivity() as MainActivity).setActionBarTitle(it.activityName)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(
            "ActivityFragment",
            "ActivityFragment Destroyed"
        )
    }

}