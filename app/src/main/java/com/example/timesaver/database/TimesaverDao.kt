package com.example.timesaver.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import java.time.LocalDate

@Dao
interface TimesaverDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivity(activity: Activity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimelog(timelog: Timelog)

    @Update
    suspend fun updateActivity(activity: Activity)

    @Update
    suspend fun updateTimelog(timelog: Timelog)

    @Delete
    suspend fun deleteActivity(activity: Activity)

    @Delete
    suspend fun deleteTimelog(timelog: Timelog)

    /** Get all activities */
    @Query("SELECT * FROM activities")
    fun getActivities(): LiveData<List<Activity>>

    /** Get all timelogs */
    @Query("SELECT * FROM timelogs")
    fun getTimelogs(): LiveData<List<Timelog>>

    /** Get all timelogs on a specific date, sorted by the activityId
     */
    @Transaction
    @Query("""
        SELECT *
        FROM timelogs
        WHERE date = :date
        ORDER BY activityId, startTime ASC
        """)
    fun getTimelogsOnDate(date: LocalDate): LiveData<List<Timelog>>

    /** Get an activity with their list of timelogs */
    @Transaction
    @Query("SELECT * FROM activities WHERE activityId = :activityId")
    fun getActivityTimelog(activityId: Long): LiveData<ActivityTimelog>

    /** Get all activities with their list of timelogs */
    @Transaction
    @Query("SELECT * FROM activities")
    fun getAllActivityTimelogs(): LiveData<List<ActivityTimelog>>

    /** Get total time elapsed for an activity in all history */
    @Transaction
    @Query("""SELECT SUM(strftime('%s', endTime) - strftime('%s', startTime))
            FROM timelogs
            WHERE activityId = :activityId
            """)
    fun getTotalActivityTime(activityId: Long): Long

    /** Get total time elapsed for an activity on a specific date */
    @Transaction
    @Query("""SELECT SUM(strftime('%s', endTime) - strftime('%s', startTime))
            FROM timelogs
            WHERE activityId = :activityId AND date(startTime) = :date 
            """)
    fun getTotalActivityTimeOnDate(activityId: Long, date: LocalDate): Long

}