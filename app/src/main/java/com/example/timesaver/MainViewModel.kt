package com.example.timesaver

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.timesaver.database.Activity
import com.example.timesaver.database.ActivityTimelog
import com.example.timesaver.database.Timelog
//import com.example.timesaver.database.ActivityTimeLog
//import com.example.timesaver.database.TimeLog
import com.example.timesaver.database.TimesaverRepository
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime

class MainViewModel(private val repository: TimesaverRepository) : ViewModel() {
    private var activities: LiveData<List<Activity>> = MutableLiveData()
    private var todaysLogs: LiveData<List<Timelog>> = MutableLiveData()

    private val _combinedData = MediatorLiveData<Pair<List<Activity>, List<Timelog>>>()
    val combinedData: LiveData<Pair<List<Activity>, List<Timelog>>> = _combinedData

    private val stopwatch = Stopwatch()
    private val _elapsedTime = MutableLiveData<Duration>() // only ViewModel modifies this
    val elapsedTime: LiveData<Duration> = _elapsedTime // read-only
    
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
}