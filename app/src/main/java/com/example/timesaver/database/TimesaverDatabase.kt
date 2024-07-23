package com.example.timesaver.database

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// Room Database class
@Database(
    version = 19,
    entities = [
    Activity::class,
    Timelog::class]
)
@TypeConverters(Converters::class)
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
                )
                .addCallback( object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        Log.d("TimesaverDatabase", "Callback onCreate() called")
                        INSTANCE?.let { database ->
                            CoroutineScope(Dispatchers.IO).launch {
                                populateManyDatabase(database.timesaverDao())
                            }
                        }
                    }
                    override fun onDestructiveMigration(db: SupportSQLiteDatabase) {
                        Log.d("TimesaverDatabase", "Callback onDestructiveMigration() called")
                        super.onDestructiveMigration(db)
                        INSTANCE?.let { database ->
                            CoroutineScope(Dispatchers.IO).launch {
                                populateManyDatabase(database.timesaverDao())
                            }
                        }
                    }
                })
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun populateDatabase(activityDao: TimesaverDao) {
            Log.d("TimesaverDatabase", "Populating database...")
            activityDao.insertActivity(dummyActivity)
            Log.d("TimesaverDatabase", "Database populated")
        }

        private suspend fun populateManyDatabase(activityDao: TimesaverDao) {
            Log.d("TimesaverDatabase", "Populating database...")
            for (a in dummyActivities) {
                activityDao.insertActivity(a)
            }
            for (t in dummyTimelogs) {
                activityDao.insertTimelog(t)
            }
            Log.d("TimesaverDatabase", "Database populated")
        }
    }
}