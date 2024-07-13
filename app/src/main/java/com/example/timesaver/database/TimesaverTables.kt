package com.example.timesaver.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import java.time.LocalDate
import java.time.Duration

@Entity(tableName = "activity")
@TypeConverters(Converters::class)
data class Activity(
    @PrimaryKey val activityId: Long = 0,
    val activityName: String,
    val timeLimit: Duration
)

@Entity(tableName = "time_log",
    foreignKeys = [
        ForeignKey(
            entity = Activity::class,
            parentColumns = ["activityId"],
            childColumns = ["activityId"],
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE
        )
    ]
)
@TypeConverters(Converters::class)
data class TimeLog(
    @PrimaryKey(autoGenerate = true) val timeLogId: Long = 0,
    @ColumnInfo(index = true) val activityId: Long,
    val date: LocalDate,
    val timeElapsed: Duration
)

