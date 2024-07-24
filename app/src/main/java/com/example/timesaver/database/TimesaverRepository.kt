package com.example.timesaver.database

import androidx.lifecycle.LiveData
import java.time.LocalDate

class TimesaverRepository(private val dao: TimesaverDao) {
    suspend fun insertActivity(activity: Activity) {
        dao.insertActivity(activity)
    }

    suspend fun insertTimeLog(log: Timelog) {
        dao.insertTimelog(log)
    }

    suspend fun updateActivity(activity: Activity) {
        dao.updateActivity(activity)
    }

    suspend fun updateTimeLog(log: Timelog) {
        dao.updateTimelog(log)
    }

    fun getActivities(): LiveData<List<Activity>> {
        return dao.getActivities()
    }

    fun getActivityTimeLogs(): LiveData<List<ActivityTimelog>> {
        return dao.getActivityTimelogs()
    }

    fun getActivityTimelogsOnDate(date: LocalDate): LiveData<List<ActivityTimelog>> {
        return dao.getActivityTimelogsOnDate(date)
    }

    fun getTotalActivityTime(activityId: Long): Long {
        return dao.getTotalActivityTime(activityId)
    }

    fun getTotalActivityTimeOnDate(activityId: Long, date: LocalDate): Long {
        return dao.getTotalActivityTimeOnDate(activityId, date)
    }
}