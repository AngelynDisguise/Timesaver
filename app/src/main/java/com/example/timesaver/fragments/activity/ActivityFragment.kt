package com.example.timesaver.fragments.activity

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.example.timesaver.MainActivity
import com.example.timesaver.MainViewModel
import com.example.timesaver.R
import com.example.timesaver.database.Activity
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class ActivityFragment : Fragment() {

    private lateinit var navController: NavController
    private lateinit var viewModel: MainViewModel
    private lateinit var currentActivity: Activity

    override fun onAttach(context: Context) {
        super.onAttach(context)
        viewModel = (requireActivity() as MainActivity).viewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_activity, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val i = arguments?.getInt("activityIndex") ?: 0

        // Recieve Activity list from database
        viewModel.activities.observe(viewLifecycleOwner) {
            currentActivity = it[i]
            (requireActivity() as MainActivity).setActionBarTitle(currentActivity.activityName)
            Log.i(
                "ActivityFragment",
                "Recieved activity: $it"
            )

            // Setup horizontal paging for Logs and Insights fragments
            val pagerAdapter = ActivityPagerAdapter(requireActivity(), it[i].activityId)
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
        }

        // Go back to ActivityMenu when pressing up
        navController = findNavController()
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                navController.navigateUp()
            }
        })

    }

}