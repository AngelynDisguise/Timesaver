package com.example.timesaver.database

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.Relation
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

// 1:1 activity-timelog; An instance of an activity with its time log on one date
data class ActivityTimeLog(
    @Embedded val activity: Activity,
    @Relation(
        parentColumn = "activityId",
        entityColumn = "activityId"
    )
    val timeLog: TimeLog
)

// 1:N activity-timelog; An activity with all its time logs on all dates
data class ActivityWithAllTimeLogs(
    @Embedded val activity: Activity,
    @Relation(
        parentColumn = "activityId",
        entityColumn = "activityId"
    )
    val exercises: List<TimeLog>
)

