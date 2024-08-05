package com.example.timesaver.fragments.activitymenufragment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.timesaver.R
import com.example.timesaver.database.Activity


class ActivityListAdapter: ListAdapter<Activity, ActivityListAdapter.ViewHolder>(ActivityDiffCallback()) {

    private var onClickListener: ((View, Int, Activity) -> Unit)? = null

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val activityTextView: TextView = view.findViewById(R.id.activity_menu_activity_text_view)
        val optionsIcon: ImageView = view.findViewById(R.id.options_icon)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.activity_item, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val activity: Activity = getItem(position)
        viewHolder.activityTextView.text = activity.activityName
        viewHolder.optionsIcon.setOnClickListener {
            onClickListener?.invoke(it, position, activity)
        }
    }

    fun setOnClickListener(listener: (View, Int, Activity) -> Unit) {
        this.onClickListener = listener
    }

}