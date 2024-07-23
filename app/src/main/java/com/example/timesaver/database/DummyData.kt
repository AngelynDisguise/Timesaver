package com.example.timesaver.database

import java.time.LocalDateTime
import java.time.Month
val dummyActivity = Activity(activityId = 0, activityName = "Break")

val dummyActivities: List<Activity> = listOf(
    Activity(1,"Work"),
    Activity(2,"LitAI"),
    Activity(3,"Break"),
    Activity(4,"Reddit"),
    Activity(5,"Cooking")
)

val dummyLocalDateTimes: List<Pair<LocalDateTime, LocalDateTime>> = listOf(
    Pair(
        LocalDateTime.of(2020, Month.DECEMBER, 29, 19, 30, 40),
        LocalDateTime.of(2020, Month.DECEMBER, 29, 20, 0, 0)
    ),
    Pair(
        LocalDateTime.of(2020, Month.OCTOBER, 30, 10, 30, 0),
        LocalDateTime.of(2015, Month.JULY, 29, 11, 21, 20)
    ),
    Pair(
        LocalDateTime.of(2022, Month.AUGUST, 29, 18, 30, 40),
        LocalDateTime.of(2015, Month.AUGUST, 29, 18, 40, 59)
    ),
    Pair(
        LocalDateTime.of(2024, Month.JULY, 19, 9, 0, 40),
        LocalDateTime.of(2024, Month.JULY, 19, 9, 30, 10)
    ),
    Pair(
        LocalDateTime.of(2024, Month.JULY, 20, 11, 30, 0),
        LocalDateTime.of(2024, Month.JULY, 20, 11, 30, 1)
    ),
    Pair(
        LocalDateTime.of(2024, Month.JULY, 21, 18, 2, 0),
        LocalDateTime.of(2024, Month.JULY, 21, 19, 6, 2)
    ),
    Pair(
        LocalDateTime.of(2024, Month.JULY, 22, 10, 0, 0),
        LocalDateTime.of(2024, Month.JULY, 22, 11, 0, 0)
    ),
    Pair(
        LocalDateTime.of(2024, Month.JULY, 22, 15, 0, 0),
        LocalDateTime.of(2024, Month.JULY, 22, 15, 30, 0)
    ),
    Pair(
        LocalDateTime.of(2024, Month.JULY, 22, 18, 0, 0),
        LocalDateTime.of(2024, Month.JULY, 22, 18, 5, 0)
    ),
    Pair(
        LocalDateTime.now().minusHours(10),
        LocalDateTime.now().minusHours(8),
    ),
    Pair(
        LocalDateTime.now().minusHours(5).minusMinutes(3).minusSeconds(10),
        LocalDateTime.now().minusHours(4).minusMinutes(0).minusSeconds(30)
    ),
    Pair(
        LocalDateTime.now().minusMinutes(30).minusSeconds(30),
        LocalDateTime.now().minusMinutes(25),
    )
)

val dummyTimelogs: List<Timelog> = dummyActivities.mapIndexed { i, a: Activity ->
    Timelog(
        timelogId = 0,
        activityId = a.activityId,
        startTime = dummyLocalDateTimes[i].first,
        endTime = dummyLocalDateTimes[i].second
    )
}