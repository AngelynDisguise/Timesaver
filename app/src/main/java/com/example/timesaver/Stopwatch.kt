package com.example.timesaver

import android.util.Log
import kotlinx.coroutines.*
import java.time.Duration
import java.time.LocalTime

class Stopwatch {
    private var job: Job? = null
    private var elapsedTime: Duration = Duration.ZERO
    private var lastStartTime: Long = 0
    private var startTime: LocalTime? = null

    fun start(scope: CoroutineScope) {
        if (job == null) {
//            Log.d(
//                "Stopwatch",
//                "Stopwatch Running"
//            )
            if (elapsedTime == Duration.ZERO) {
                startTime = LocalTime.now() // first start time
            }
            lastStartTime = System.currentTimeMillis()
            job = scope.launch {
                while (isActive) {
                    val currentTime = System.currentTimeMillis()
                    elapsedTime = elapsedTime.plusMillis(currentTime - lastStartTime)
                    lastStartTime = currentTime
                    delay(1000) // delay by 1 second
//                    Log.d(
//                        "Stopwatch",
//                        "."
//                    )
                }
            }
        }
    }

    fun pause() {
        job?.cancel()
        job = null

//        Log.d(
//            "Stopwatch",
//            "Stopwatch Paused"
//        )
    }

    fun isRunning(): Boolean {
        return job?.isActive ?: false
    }

    fun reset() {
        pause()
        elapsedTime = Duration.ZERO
        startTime = null

//        Log.d(
//            "Stopwatch",
//            "Stopwatch Reset"
//        )
    }

    fun getElapsedTime(): Duration {
        return elapsedTime
    }

    fun getStartTime(): LocalTime? {
        return startTime
    }
}