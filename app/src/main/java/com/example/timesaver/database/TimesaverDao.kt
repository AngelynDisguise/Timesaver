package com.example.timesaver.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
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

    @Query("SELECT * FROM time_log WHERE date = :date")
    fun getLogsFromDate(date: LocalDate): LiveData<List<TimeLog>>
}