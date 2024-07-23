package com.example.timesaver.database

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.Relation
import androidx.room.TypeConverters
import java.time.LocalDateTime

@Entity(tableName = "activities")
@TypeConverters(Converters::class)
data class Activity(
    @PrimaryKey(autoGenerate = true) val activityId: Long = 0,
    val activityName: String
)

@Entity(tableName = "timelogs",
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
data class Timelog(
    @PrimaryKey(autoGenerate = true) val timelogId: Long = 0,
    @ColumnInfo(index = true) val activityId: Long,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime
)

// 1:N activity-timelog; An activity with all timelogs in history
data class ActivityTimelog(
    @Embedded val activity: Activity,
    @Relation(
        parentColumn = "activityId",
        entityColumn = "activityId"
    )
    val timelogs: List<Timelog>
)

