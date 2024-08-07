package com.example.timesaver.database

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.Relation
import androidx.room.TypeConverters
import kotlinx.parcelize.Parcelize
import java.time.LocalDate
import java.time.LocalTime

@Parcelize
@Entity(tableName = "activities")
@TypeConverters(Converters::class)
data class Activity(
    @PrimaryKey(autoGenerate = true) val activityId: Long = 0,
    val activityName: String
): Parcelable

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
    val date: LocalDate,
    val startTime: LocalTime,
    val endTime: LocalTime
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

