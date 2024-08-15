package com.example.timesaver

import android.content.Context
import android.os.Bundle
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
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
import kotlinx.coroutines.async
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull

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
            val deferred = async { timesaverDao.insertActivity(activity) }
            deferred.await()
            val activities = timesaverDao.getActivitiesLive().value
            assertNotNull(activities)
            if (activities != null) {
                assert(activities.isNotEmpty())
                assertEquals(activities[0].activityName, "Reading")
            }
        }
    }

    @Test
    @Throws(Exception::class)
    fun getActivities() {
        CoroutineScope(Dispatchers.IO).launch {
            val activities = async { timesaverDao.getActivitiesLive().value }
            assertNotNull(activities.await())
            if (activities.await() != null) {
                assert(activities.await()!!.isNotEmpty())
                InstrumentationRegistry.getInstrumentation().sendStatus(
                    1, Bundle().apply {
                        putString("result", "getActivities test results: $activities")
                    }
                )
            } else {
                InstrumentationRegistry.getInstrumentation().sendStatus(
                    1, Bundle().apply {
                        putString("result", "getActivities got null Activities")
                    }
                )

            }
        }
        //println("test2 ended")
    }
}
