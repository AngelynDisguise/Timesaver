package com.example.timesaver.database

import androidx.lifecycle.LiveData
import androidx.paging.PagingSource
import java.time.LocalDate
import java.time.LocalTime

class TimesaverRepository(private val dao: TimesaverDao) {

    /** @see [TimesaverDao.insertActivity] */
    suspend fun insertActivity(activity: Activity) {
        dao.insertActivity(activity)
    }

    /** @see [TimesaverDao.insertTimelog] */
    suspend fun insertTimeLog(log: Timelog) {
        dao.insertTimelog(log)
    }

    /** @see [TimesaverDao.updateActivity] */
    suspend fun updateActivity(activity: Activity) {
        dao.updateActivity(activity)
    }

    /** @see [TimesaverDao.updateTimelog] */
    suspend fun updateTimeLog(log: Timelog) {
        dao.updateTimelog(log)
    }

    /** @see [TimesaverDao.deleteActivity] */
    suspend fun deleteActivity(activity: Activity) {
        dao.deleteActivity(activity)
    }

    /** @see [TimesaverDao.deleteTimelog] */
    suspend fun deleteTimelog(timelog: Timelog) {
        dao.deleteTimelog(timelog)
    }

    /** @see [TimesaverDao.getActivitiesLive] */
    fun getActivitiesLive(): LiveData<List<Activity>> {
        return dao.getActivitiesLive()
    }

    /** @see [TimesaverDao.getTimelogsOnDateLive] */
    fun getTimelogsOnDateLive(date: LocalDate): LiveData<List<Timelog>> {
        return dao.getTimelogsOnDateLive(date)
    }

    /** @see [TimesaverDao.getTimelogsForActivity] */
    fun getTimelogsForActivity(activityId: Long): List<Timelog> {
        return dao.getTimelogsForActivity(activityId)
    }

    /** @see [TimesaverDao.getTimelogsForActivityNewestFirst] */
    fun getTimelogsForActivityNewestFirst(activityId: Long): PagingSource<Int, Timelog> {
        return dao.getTimelogsForActivityNewestFirst(activityId)
    }

    /** @see [TimesaverDao.getTimelogsForActivityOldestFirst] */
    fun getTimelogsForActivityOldestFirst(activityId: Long): PagingSource<Int, Timelog> {
        return dao.getTimelogsForActivityOldestFirst(activityId)
    }

    /** @see [TimesaverDao.getActivityTimelogLive] */
    fun getActivityTimelogLive(activityId: Long): LiveData<ActivityTimelog> {
        return dao.getActivityTimelogLive(activityId)
    }

    /** @see [TimesaverDao.getOverlappingTimelogs] */
    suspend fun getOverlappingTimelogs(
        activityId: Long,
        excludeId: Long,
        date: LocalDate,
        startTime: LocalTime,
        endTime: LocalTime,
    ): List<Timelog> {
        return dao.getOverlappingTimelogs(activityId, excludeId, date, startTime, endTime)
    }

}