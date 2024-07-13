package com.example.timesaver.database

import androidx.lifecycle.LiveData
import java.time.LocalDate

class TimesaverRepository(private val dao: TimesaverDao) {
    fun getLogsFromDate(today: LocalDate): LiveData<List<TimeLog>> {
        return dao.getLogsFromDate(today)
    }

    suspend fun insertLog(log: TimeLog) {
        dao.insertTimeLog(log)
    }
}