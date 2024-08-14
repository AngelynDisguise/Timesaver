package com.example.timesaver.fragments.activity

import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.timesaver.fragments.activity.logs.LogsFragment

class ActivityPagerAdapter(
    fragmentActivity: FragmentActivity,
    private val activityId: Long
) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        val fragment = when (position) {
            0 -> LogsFragment()
            1 -> InsightsFragment()
            else -> throw IllegalArgumentException("Invalid position $position")
        }
        fragment.arguments = bundleOf("activityId" to activityId)
        return fragment
    }
}