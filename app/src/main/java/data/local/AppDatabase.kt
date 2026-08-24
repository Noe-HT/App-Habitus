package com.app.habitus.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.app.habitus.data.models.Habit
import com.app.habitus.data.models.HabitLog

@Database(
    entities = [Habit::class, HabitLog::class],
    version = 2,
    exportSchema = true
)

abstract class AppDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao

    companion object {
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                ALTER TABLE habits 
                ADD COLUMN icon TEXT NOT NULL DEFAULT '📝'
            """
                )
                database.execSQL(
                    """
                ALTER TABLE habits 
                ADD COLUMN durationHours INTEGER NOT NULL DEFAULT 0
            """
                )
                database.execSQL(
                    """
                ALTER TABLE habits 
                ADD COLUMN durationMinutes INTEGER NOT NULL DEFAULT 30
            """
                )
            }
        }

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "habitflow_database"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}