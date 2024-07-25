package com.example.timesaver.database

import androidx.room.TypeConverter
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class Converters {
    @TypeConverter
    fun fromDuration(value: Duration?): Long? {
        return value?.toMillis()
    }

    @TypeConverter
    fun toDuration(value: Long?): Duration? {
        return value?.let { Duration.ofMillis(it) }
    }

    companion object {
        private val formatter = DateTimeFormatter.ISO_LOCAL_TIME
    }

    @TypeConverter
    fun fromLocalTime(value: LocalTime?): String? {
        return value?.format(formatter)
    }

    @TypeConverter
    fun toLocalTime(value: String?): LocalTime? {
        return value?.let { formatter.parse(it, LocalTime::from) }
    }

    @TypeConverter
    fun fromLocalDate(value: LocalDate?): Long? {
        return value?.toEpochDay()
    }

    @TypeConverter
    fun toLocalDate(value: Long?): LocalDate? {
        return value?.let { LocalDate.ofEpochDay(it) }
    }

    @TypeConverter
    fun fromLocalDateTime(value: String?): LocalDateTime? {
        return value?.let { LocalDateTime.parse(it) }
    }

    @TypeConverter
    fun toLocalDateTime(date: LocalDateTime?): String? {
        return date?.toString()
    }
}