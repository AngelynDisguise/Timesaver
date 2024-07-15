package com.example.timesaver

import kotlinx.coroutines.*
import java.time.Duration

class Stopwatch {
    private var job: Job? = null
    private var elapsedTime: Duration = Duration.ZERO
    private var lastStartTime: Long = 0

    fun start(scope: CoroutineScope) {
        if (job == null) {
            lastStartTime = System.currentTimeMillis()
            job = scope.launch {
                while (isActive) {
                    val currentTime = System.currentTimeMillis()
                    elapsedTime = elapsedTime.plusMillis(currentTime - lastStartTime)
                    lastStartTime = currentTime
                    delay(1000) // delay by 1 second
                }
            }
        }
    }

    fun pause() {
        job?.cancel()
        job = null
    }

    fun isRunning(): Boolean {
        return job?.isActive ?: false
    }

    fun reset() {
        pause()
        elapsedTime = Duration.ZERO
    }

    fun getElapsedTime(): Duration {
        return elapsedTime
    }
}