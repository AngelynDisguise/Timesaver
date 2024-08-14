package com.example.timesaver.database

import androidx.lifecycle.LiveData
import java.time.LocalDate

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

    /** @see [TimesaverDao.getTimelogsNewestFirst] */
    fun getTimelogsForActivityNewestFirst(activityId: Long): List<Timelog> {
        return getTimelogsForActivityNewestFirst(activityId)
    }

    /** @see [TimesaverDao.getTimelogsOldestFirst] */
    fun getTimelogsForActivityOldestFirst(activityId: Long): List<Timelog> {
        return dao.getTimelogsForActivityOldestFirst(activityId)
    }

    /** @see [TimesaverDao.getActivityTimelogLive] */
    fun getActivityTimelogLive(activityId: Long): LiveData<ActivityTimelog> {
        return dao.getActivityTimelogLive(activityId)
    }

}