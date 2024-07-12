package com.example.timesaver

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.timesaver.database.TimeLog
import com.example.timesaver.database.TimesaverDao
import com.example.timesaver.database.TimesaverDatabase
import kotlinx.coroutines.launch
import java.time.LocalDate

class MainViewModel(private val dao: TimesaverDao) : ViewModel() {
    private val today: LocalDate = LocalDate.now()
    lateinit var todaysLogs: LiveData<List<TimeLog>>

    init {
       getTodaysLogs()
    }

    private fun getTodaysLogs() {
        viewModelScope.launch {
            todaysLogs = dao.getLogsFromDate(today)
        }
    }
}