package com.example.timesaver

import android.widget.TextView
import androidx.lifecycle.LifecycleCoroutineScope
import kotlinx.coroutines.*
import java.time.Duration

class Stopwatch(private val textView: TextView, private val scope: LifecycleCoroutineScope) {
    private var job: Job? = null
    private var elapsedTime: Duration = Duration.ZERO
    private var lastStartTime: Long = 0

    fun start() {
        if (job == null) {
            lastStartTime = System.currentTimeMillis()
            job = scope.launch {
                while (isActive) {
                    val currentTime = System.currentTimeMillis()
                    elapsedTime = elapsedTime.plusMillis(currentTime - lastStartTime)
                    lastStartTime = currentTime
                    updateDisplay()
                    delay(1000)
                }
            }
        }
    }

    fun pause() {
        job?.cancel()
        job = null
    }

    fun reset() {
        pause()
        elapsedTime = Duration.ZERO
        updateDisplay()
    }

    private fun updateDisplay() {
        val hours = elapsedTime.toHours()
        val minutes = elapsedTime.toMinutes() % 60
        val seconds = elapsedTime.seconds % 60

        val text = "%02d:%02d:%02d".format(hours, minutes, seconds)
        textView.text = text
    }
}