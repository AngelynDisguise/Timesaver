package com.example.timesaver.database

import android.content.Context
import android.util.Log
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDate

// Room Database class
@Database(
    version = 18,
//    autoMigrations = [
//        AutoMigration (from = 14, to = 18)
//    ],
    entities = [
    Activity::class,
    TimeLog::class]
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
            val dummyActivity = Activity(activityId = 0, activityName = "Break", timeLimit = Duration.ZERO)
            activityDao.insertActivity(dummyActivity)
            Log.d("TimesaverDatabase", "Database populated")
        }

        private suspend fun populateManyDatabase(activityDao: TimesaverDao) {
            Log.d("TimesaverDatabase", "Populating database...")
            val dummyActivities: List<Activity> = listOf(
                Activity(0,"Work", Duration.ZERO),
                Activity(0,"LitAI", Duration.ZERO),
                Activity(0,"Break", Duration.ZERO),
                Activity(0,"Reddit", Duration.ZERO),
                Activity(0,"Cooking", Duration.ZERO)
            )
            for (a in dummyActivities) {
                activityDao.insertActivity(a)
            }
            Log.d("TimesaverDatabase", "Database populated")
        }
    }
}