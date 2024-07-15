package com.example.timesaver.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import java.time.LocalDate

@Dao
interface TimesaverDao {
    @Insert
    suspend fun insertActivity(activity: Activity)

    @Insert
    suspend fun insertTimeLog(timeLog: TimeLog)

    @Update
    suspend fun updateActivity(activity: Activity)

    @Update
    suspend fun updateTimeLog(timeLog: TimeLog)

    @Delete
    suspend fun deleteActivity(activity: Activity)

    @Delete
    suspend fun deleteTimeLog(timeLog: TimeLog)

    @Transaction
    @Query("""
        SELECT t.*, a.activityName, a.timeLimit
        FROM time_log t
        LEFT JOIN activity a ON t.activityId = a.activityId
        WHERE t.date = :date
    """)
    fun getActivityTimeLogsFromDate(date: LocalDate): LiveData<List<ActivityTimeLogs>>

    @Transaction
    @Query("SELECT * FROM activity")
    fun getAllActivities(): LiveData<List<Activity>>
}