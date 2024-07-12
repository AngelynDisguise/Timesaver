package com.example.timesaver.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// Room Database class
@Database(entities = [],
    version = 14)
abstract class TimesaverDatabase : RoomDatabase() {
    abstract fun timesaverDao(): TimesaverDao

    companion object {
        @Volatile
        private var INSTANCE: TimesaverDatabase? = null
        fun getDatabase(context: Context): TimesaverDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TimesaverDatabase::class.java,
                    "timesaver_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }

    }
}