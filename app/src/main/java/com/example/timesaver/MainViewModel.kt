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

class MainViewModel(private val repository: TimesaverRepository) : ViewModel() {
    lateinit var activityTimelogs: LiveData<List<ActivityTimelog>>

    private val stopwatch = Stopwatch()
    private val _elapsedTime = MutableLiveData<Duration>() // only ViewModel modifies this
    val elapsedTime: LiveData<Duration> = _elapsedTime // read-only
    
    init {
        getTodaysActivityTimelogs()
    }

    private fun getTodaysActivityTimelogs() {
        val today: LocalDate = LocalDate.now()
        viewModelScope.launch {
            activityTimelogs = repository.getActivityTimelogsOnDate(today)
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