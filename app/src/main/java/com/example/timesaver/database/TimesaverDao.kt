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
        SELECT * 
        FROM activity 
        INNER JOIN time_log ON activity.activityId = time_log.activityId 
        WHERE time_log.date = :date
    """)
    fun getAllActivityTimeLogsOnDate(date: LocalDate): LiveData<List<ActivityTimeLog>>

    @Transaction
    @Query("SELECT * FROM activity WHERE activityId = :activityId")
    fun getActivityWithAllTimeLogs(activityId: Long): ActivityWithAllTimeLogs

    @Transaction
    @Query("SELECT * FROM activity")
    fun getAllActivitiesWithAllTimeLogs(): List<ActivityWithAllTimeLogs>

    @Transaction
    @Query("SELECT * FROM activity")
    fun getAllActivities(): LiveData<List<Activity>>
}