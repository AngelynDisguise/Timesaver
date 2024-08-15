package com.example.timesaver.database

import androidx.lifecycle.LiveData
import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import java.time.LocalDate
import java.time.LocalTime

@Dao
interface TimesaverDao {

    /* INSERT, UPDATE, DELETE **/

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

    /* END OF INSERT, UPDATE, DELETE **/



    /* QUERIES FOR ACTIVITIES **/

    /** Get all activities.
     *
     * Used in: `MainFragment`, `ActivityMenuFragment`
     * @return an observable `LiveData` object containing the resulting list of activities
     * */
    @Query("SELECT * FROM activities")
    fun getActivitiesLive(): LiveData<List<Activity>>

    /* END OF QUERIES FOR ACTIVITIES */



    /* QUERIES FOR TIMELOGS */

    /** Get all timelogs.
     *
     * @return an observable `LiveData` object containing the resulting list of activities
     * */
    @Query("SELECT * FROM timelogs")
    fun getTimelogsLive(): LiveData<List<Timelog>>


    /** Get all timelogs on a specific date, ordered by activityId.
     *
     * Used in: `MainFragment`
     * @param date the date for a timelog
     * @return an observable `LiveData` object containing the resulting list of timelogs
     * */
    @Transaction
    @Query("""
        SELECT *
        FROM timelogs
        WHERE date = :date
        ORDER BY activityId
        """)
    fun getTimelogsOnDateLive(date: LocalDate): LiveData<List<Timelog>>


    /** Get all timelogs for a specific activity.
     *
     * @param activityId the activity
     * @return an observable `LiveData` object containing the resulting list of timelogs
     * */
    @Query("""
        SELECT * 
        FROM timelogs 
        WHERE activityId = :activityId
        """)
    fun getTimelogsForActivityLive(activityId: Long): LiveData<List<Timelog>>


    /** Get all timelogs for a specific activity.
     *
     * Used in: `ActivityMenuFragment`
     * @param activityId the activity
     * @return an observable `LiveData` object containing the resulting list of timelogs
     * */
    @Query("""
        SELECT * 
        FROM timelogs 
        WHERE activityId = :activityId
        """)
    fun getTimelogsForActivity(activityId: Long): List<Timelog>


    /** Get all timelogs for a specific activity, sorted by newest-oldest dates and times.
     *
     * Used in: `LogsFragment`
     * @param activityId the activity
     * @return a `PagingSource` object that streams the resulting timelogs as `PagingData` objects
     * */
    @Query("""
        SELECT * 
        FROM timelogs 
        WHERE activityId = :activityId
        ORDER BY date DESC, startTime DESC
        """)
    fun getTimelogsForActivityNewestFirst(activityId: Long): PagingSource<Int, Timelog>


    /** Get all timelogs for a specific activity, sorted by newest-oldest dates and times
     *
     * Used in: `LogsFragment`
     * @param activityId the activity
     * @return a `PagingSource` object that streams the resulting timelogs as `PagingData` objects
     * */
    @Query("""
        SELECT * 
        FROM timelogs 
        WHERE activityId = :activityId
        ORDER BY date ASC, startTime ASC
        """)
    fun getTimelogsForActivityOldestFirst(activityId: Long): PagingSource<Int, Timelog>

    /** Get all timelogs that overlap with a given timelog's info.
     *
     * Used in: `LogsFragment`
     * @param activityId the activity that owns the timelog
     * @param excludeId the given timelog to compare against
     * @param date the date where timelogs should not overlap
     * @param startTime the startTime of the given timelog
     * @param endTime the endTime of the given timelog
     * @return the resulting list of overlapping timelogs
     */
    @Query("""
        SELECT * FROM timelogs 
        WHERE activityId = :activityId
        AND timelogId != :excludeId
        AND date = :date
        AND ((startTime < :endTime AND endTime > :startTime) 
             OR (startTime >= :startTime AND startTime < :endTime)
             OR (endTime > :startTime AND endTime <= :endTime))
    """)
    suspend fun getOverlappingTimelogs(
        activityId: Long,
        excludeId: Long,
        date: LocalDate,
        startTime: LocalTime,
        endTime: LocalTime,
    ): List<Timelog>

    /* END OF QUERIES FOR TIMELOGS */



    /* QUERIES FOR ACTIVITY-TIMELOG */

    /** Get a specific activity with their list of timelogs
     *
     * (Deprecated) Used in: `LogsFragment`
     * @param activityId the activity
     * @return an observable `LiveData` object containing the ActivityTimelog
     * */
    @Transaction
    @Query("""
        SELECT * 
        FROM activities 
        WHERE activityId = :activityId
        """)
    fun getActivityTimelogLive(activityId: Long): LiveData<ActivityTimelog>


    /** Get all activities with their list of timelogs
     *
     * @return an observable `LiveData` object containing the ActivityTimelog
     * */
    @Transaction
    @Query("SELECT * FROM activities")
    fun getAllActivityTimelogsLive(): LiveData<List<ActivityTimelog>>

    /* END OF QUERIES FOR ACTIVITY-TIMELOG */
}