package com.example.timesaver

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.timesaver.database.Activity
import com.example.timesaver.database.TimesaverDao
import com.example.timesaver.database.TimesaverDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

import androidx.lifecycle.Observer
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

@RunWith(AndroidJUnit4::class)
class TimesaverDatabaseTest {
    private lateinit var timesaverDao: TimesaverDao
    private lateinit var db: TimesaverDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context, TimesaverDatabase::class.java
        ).build()
        timesaverDao = db.timesaverDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun insertAndGetActivity() {
        val activity = Activity(activityName = "Reading")
        CoroutineScope(Dispatchers.IO).launch {
            timesaverDao.insertActivity(activity)
        }
        val activities = timesaverDao.getActivities().getOrAwaitValue()
        assert(activities.isNotEmpty())
        assert(activities[0].activityName == "Reading")
    }

    @Test
    @Throws(Exception::class)
    fun getActivities() {
        val activities = timesaverDao.getActivities().getOrAwaitValue()
        assert(activities.isNotEmpty())
        Log.d("DatabaseTest", "All activities: $activities")
    }

    @Test
    @Throws(Exception::class)
    fun getActivityTimelogs() {
        val activities = timesaverDao.getActivityTimelogs().getOrAwaitValue()
        assert(activities.isNotEmpty())
        Log.d("DatabaseTest", "All activities: $activities")
    }
}

fun <T> LiveData<T>.getOrAwaitValue(
    time: Long = 2,
    timeUnit: TimeUnit = TimeUnit.SECONDS
): T {
    var data: T? = null
    val latch = CountDownLatch(1)
    val observer = object : Observer<T> {
        override fun onChanged(value: T) {
            data = value
            latch.countDown()
            this@getOrAwaitValue.removeObserver(this)
        }
    }

    this.observeForever(observer)

    // Don't wait indefinitely if the LiveData is not set.
    if (!latch.await(time, timeUnit)) {
        throw TimeoutException("LiveData value was never set.")
    }

    @Suppress("UNCHECKED_CAST")
    return data as T
}