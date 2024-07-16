package com.example.timesaver

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.timesaver.database.Activity
import com.example.timesaver.database.ActivityTimeLog
import com.example.timesaver.database.TimeLog
import com.example.timesaver.database.TimesaverRepository
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.Duration

class MainViewModel(private val repository: TimesaverRepository) : ViewModel() {
    lateinit var activities: LiveData<List<Activity>>
    lateinit var todaysLogs: LiveData<List<ActivityTimeLog>>

    private val stopwatch = Stopwatch()
    private val _elapsedTime = MutableLiveData<Duration>() // only ViewModel modifies this
    val elapsedTime: LiveData<Duration> = _elapsedTime // read-only

    init {
        getTodaysLogs()
        getAllActivities()
    }

    private fun getTodaysLogs() {
        val today: LocalDate = LocalDate.now()
        viewModelScope.launch {
            todaysLogs = repository.getActivityTimeLogsFromDate(today)
        }
    }

    private fun getAllActivities() {
        viewModelScope.launch {
            activities = repository.getAllActivities()
        }
    }

    fun startStopwatch() {
        stopwatch.start(viewModelScope)
        updateElapsedTime()
    }

    fun pauseStopwatch() {
        stopwatch.pause()
    }

    fun resetStopwatch() {
        stopwatch.reset()
        _elapsedTime.value = Duration.ZERO
    }

    fun stopwatchIsRunning(): Boolean {
        return stopwatch.isRunning()
    }

    fun timeHasElapsed(): Boolean {
        return stopwatch.getElapsedTime() != Duration.ZERO
    }

    private fun updateElapsedTime() {
        viewModelScope.launch {
            while (true) {
                _elapsedTime.value = stopwatch.getElapsedTime()
                kotlinx.coroutines.delay(1000)
            }
        }
    }

    private fun saveFinishedTimeLog(timeLog: TimeLog) {
        viewModelScope.launch {
            repository.insertTimeLog(timeLog)
            // TODO(): update instead if timelog already exists
        }
    }
}