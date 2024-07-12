package com.example.timesaver.database

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation
import androidx.room.TypeConverters
import java.time.DayOfWeek
import java.time.LocalDate
import kotlin.time.Duration

@Entity(tableName = "activities")
@TypeConverters(Converters::class)
data class Activity(
    @PrimaryKey val activitiesId: Long = 0,
    val name: String,
    val timeLimit: Duration
)

@Entity(tableName = "time_log")
@TypeConverters(Converters::class)
data class TimeLog(
    @PrimaryKey(autoGenerate = true) val timeLogId: Long = 0,
    @ColumnInfo(index = true) val activitiesId: Long,
    val date: LocalDate,
    val timeElapsed: Duration
)

//
//data class ActivityTimeLog(
//    @Embedded val workoutExercise: Activities,
//    @Relation(
//        parentColumn = "activitiesId",
//        entityColumn = "activitiesId"
//    )
//    val exercises: List<TimeLog>
//)

