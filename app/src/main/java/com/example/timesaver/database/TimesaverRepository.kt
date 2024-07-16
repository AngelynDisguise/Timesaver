package com.example.timesaver.database

import androidx.lifecycle.LiveData
import java.time.LocalDate

class TimesaverRepository(private val dao: TimesaverDao) {
    fun getActivityTimeLogsFromDate(date: LocalDate): LiveData<List<ActivityTimeLog>> {
        return dao.getAllActivityTimeLogsOnDate(date)
    }

    suspend fun insertTimeLog(log: TimeLog) {
        dao.insertTimeLog(log)
    }

    suspend fun updateTimeLog(log: TimeLog) {
        dao.insertTimeLog(log)
    }

    fun getAllActivities(): LiveData<List<Activity>> {
        return dao.getAllActivities()
    }
}