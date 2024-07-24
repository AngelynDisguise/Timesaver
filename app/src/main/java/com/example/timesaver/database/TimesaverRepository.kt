package com.example.timesaver.database

import androidx.lifecycle.LiveData
import java.time.LocalDate

class TimesaverRepository(private val dao: TimesaverDao) {
//    fun getActivityTimeLogsFromDate(date: LocalDate): LiveData<List<ActivityTimelog>> {
//        return dao.getAllActivityTimeLogsOnDate(date)
//    }
//
//    suspend fun insertTimeLog(log: TimeLog) {
//        dao.insertTimeLog(log)
//    }
//
//    suspend fun updateTimeLog(log: TimeLog) {
//        dao.updateTimeLog(log)
//    }

    suspend fun insertActivity(activity: Activity) {
        dao.insertActivity(activity)
    }

    suspend fun updateActivity(activity: Activity) {
        dao.updateActivity(activity)
    }

//    fun getAllActivities(): LiveData<List<Activity>> {
//        return dao.getAllActivities()
//    }
}