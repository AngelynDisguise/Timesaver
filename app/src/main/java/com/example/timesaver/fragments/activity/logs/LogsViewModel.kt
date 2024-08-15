package com.example.timesaver.fragments.activity.logs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.timesaver.database.Timelog
import com.example.timesaver.database.TimesaverRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.format.DateTimeFormatter

class LogsViewModel(private val repository: TimesaverRepository) : ViewModel() {
    private val _sortOrder = MutableStateFlow(SortOrder.NEWEST_FIRST)
    private val sortOrder: StateFlow<SortOrder> = _sortOrder.asStateFlow()

    val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern(DateFormat.US.pattern) // default
    val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern(TimeFormat.STANDARD_TIME.pattern) // default


    private var activityId: Long? = null

    /** Sets the ID of the activity for which timelogs should be retrieved.
     * Once set, this allows the `Flow` of `PagingData` to emit timelog data
     * for the specified activity when collected.
     *
     * @param id the activity
     */
    fun setActivityId(id: Long) {
        activityId = id
    }

    /**
     * The Flow of Paging Data
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val timelogs: Flow<PagingData<Timelog>> = sortOrder.flatMapLatest { order ->
        Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false,
                maxSize = 100
            )
        ) {
            activityId?.let { id ->
                when (order) {
                    SortOrder.NEWEST_FIRST -> repository.getTimelogsForActivityNewestFirst(id)
                    SortOrder.OLDEST_FIRST -> repository.getTimelogsForActivityOldestFirst(id)
                }
            } ?: throw IllegalStateException("activityId not set")
        }.flow
    }.cachedIn(viewModelScope)

    suspend fun checkForOverlap(newTimelog: Timelog): Boolean {
        return withContext(Dispatchers.IO) {
            repository.getOverlappingTimelogs(
                newTimelog.activityId,
                newTimelog.timelogId,
                newTimelog.date,
                newTimelog.startTime,
                newTimelog.endTime
            ).isNotEmpty()
        }
    }

    fun addTimelog(timelog: Timelog) {
        viewModelScope.launch {
            repository.insertTimeLog(timelog)
        }
    }

    fun updateTimelog(timelog: Timelog) {
        viewModelScope.launch {
            repository.updateTimeLog(timelog)
        }
    }

    fun deleteTimelog(timelog: Timelog) {
        viewModelScope.launch {
            repository.deleteTimelog(timelog)
        }
    }

    fun toggleSortOrder() {
        _sortOrder.value = when (_sortOrder.value) {
            SortOrder.NEWEST_FIRST -> SortOrder.OLDEST_FIRST
            SortOrder.OLDEST_FIRST -> SortOrder.NEWEST_FIRST
        }
    }

}

enum class SortOrder {
    NEWEST_FIRST, OLDEST_FIRST
}

enum class DateFormat(val pattern: String) {
    US("MM-dd-yyyy"),
    ISO("yyyy-MM-dd")
}

enum class TimeFormat(val pattern: String) {
    STANDARD_TIME("hh:mm a"),
    MILITARY_TIME("hh:mm")
}