package com.example.timesaver.fragments.activity

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.timesaver.fragments.activity.logs.LogsFragment

class ActivityPagerAdapter(
    fragment: Fragment
) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> LogsFragment()
            1 -> InsightsFragment()
            else -> throw IllegalArgumentException("Invalid position $position")
        }
    }
}