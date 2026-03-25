package com.arnabcloud.chronos.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.arnabcloud.chronos.model.TimelineItem

@Database(
    entities = [TimelineItem.Task::class, TimelineItem.Event::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class ChronosDatabase : RoomDatabase() {
    abstract fun chronosDao(): ChronosDao

    companion object {
        @Volatile
        private var INSTANCE: ChronosDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE tasks ADD COLUMN isPeriodic INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE tasks ADD COLUMN recurrence TEXT")
            }
        }

        fun getDatabase(context: Context): ChronosDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ChronosDatabase::class.java,
                    "chronos_database"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
