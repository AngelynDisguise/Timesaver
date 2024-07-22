package com.example.timesaver

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.timesaver.database.Activity
import com.example.timesaver.database.ActivityTimeLog
import com.example.timesaver.database.TimeLog
import com.example.timesaver.database.TimesaverRepository
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDate

class MainViewModel(private val repository: TimesaverRepository) : ViewModel() {
    private var activities: LiveData<List<Activity>> = MutableLiveData()
    private var todaysLogs: LiveData<List<ActivityTimeLog>> = MutableLiveData()

    private val _combinedData = MediatorLiveData<Pair<List<Activity>, List<ActivityTimeLog>>>()
    val combinedData: LiveData<Pair<List<Activity>, List<ActivityTimeLog>>> = _combinedData

    private val stopwatch = Stopwatch()
    private val _elapsedTime = MutableLiveData<Duration>() // only ViewModel modifies this
    val elapsedTime: LiveData<Duration> = _elapsedTime // read-only

    init {
        getAllActivities()
        getTodaysLogs()

        _combinedData.addSource(activities) { acts ->
            todaysLogs.value?.let { logs ->
                _combinedData.value = Pair(acts, logs)
            }
        }
        _combinedData.addSource(todaysLogs) { logs ->
            activities.value?.let { acts ->
                _combinedData.value = Pair(acts, logs)
            }
        }

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

    // Returns time at which stopwatch stopped
    fun stopStopwatch(): Duration {
        stopwatch.pause()
        return stopwatch.getElapsedTime() // stopwatch's elapsed time is the true time
    }

    private fun updateElapsedTime() {
        viewModelScope.launch {
            while (true) {
                _elapsedTime.value = stopwatch.getElapsedTime()
                kotlinx.coroutines.delay(1000)
            }
        }
    }

    fun saveNewTimeLog(timeLog: TimeLog) {
        viewModelScope.launch {
            repository.insertTimeLog(timeLog)
        }
    }

    fun updateTimeLog(timeLog: TimeLog) {
        viewModelScope.launch {
            repository.updateTimeLog(timeLog)
        }
    }
}