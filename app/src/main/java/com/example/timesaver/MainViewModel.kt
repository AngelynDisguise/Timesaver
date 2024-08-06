package com.example.timesaver

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.timesaver.database.Activity
import com.example.timesaver.database.Timelog
import com.example.timesaver.database.TimesaverRepository
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime

class MainViewModel(private val repository: TimesaverRepository) : ViewModel() {
    var activities: LiveData<List<Activity>> = MutableLiveData()
    private var todaysLogs: LiveData<List<Timelog>> = MutableLiveData()

    private val _combinedData = MediatorLiveData<Pair<List<Activity>, List<Timelog>>>()
    val combinedData: LiveData<Pair<List<Activity>, List<Timelog>>> = _combinedData

    private val stopwatch = Stopwatch()
    private val _elapsedTime = MutableLiveData<Duration>() // only ViewModel modifies this
    val elapsedTime: LiveData<Duration> = _elapsedTime // read-only

    var currentActivityIndex = -1

    // Settings
    var warnBeforeSwitch: Boolean = false
    var pauseBeforeStart: Boolean = false
    
    init {
        getActivities()
        getTodaysLogs()

        _combinedData.addSource(todaysLogs) { logs ->
            activities.value?.let { acts ->
                _combinedData.value = Pair(acts, logs)
            }
        }
        _combinedData.addSource(activities) { acts ->
            todaysLogs.value?.let { logs ->
                _combinedData.value = Pair(acts, logs)
            }
        }
    }

    private fun getActivities() {
        viewModelScope.launch {
            activities = repository.getActivities()
        }
    }

    private fun getTodaysLogs() {
        viewModelScope.launch {
            todaysLogs = repository.getTimelogsOnDate(LocalDate.now())
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
        return stopwatch.getElapsedTime() >= Duration.ofSeconds(1)
    }

    fun getStartTime(): LocalTime {
        val startTime = stopwatch.getStartTime()
        return checkNotNull(startTime) {
            "Requested for start time when stopwatch has not started yet."
        }
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

    fun addActivity(activity: Activity) {
        viewModelScope.launch {
            repository.insertActivity(activity)
        }
    }

    fun updateActivity(activity: Activity) {
        viewModelScope.launch {
            repository.updateActivity(activity)
        }
    }

    fun deleteActivity(activity: Activity) {
        viewModelScope.launch {
            repository.deleteActivity(activity)
        }
    }

    fun saveNewTimelog(timeLog: Timelog) {
        viewModelScope.launch {
            repository.insertTimeLog(timeLog)
        }
    }

    fun updateTimelog(timeLog: Timelog) {
        viewModelScope.launch {
            repository.updateTimeLog(timeLog)
        }
    }

    fun buttonIsSelected(): Boolean {
        return currentActivityIndex > -1
    }

    override fun onCleared() {
        super.onCleared()
        Log.d(
            "MainViewModel",
            "Clearing MainViewModel..."
        )
    }
}