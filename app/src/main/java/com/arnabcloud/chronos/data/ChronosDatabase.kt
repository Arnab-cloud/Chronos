package com.arnabcloud.chronos.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.arnabcloud.chronos.model.TimelineItem

@Database(entities = [TimelineItem.Task::class, TimelineItem.Event::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class ChronosDatabase : RoomDatabase() {
    abstract fun chronosDao(): ChronosDao

    companion object {
        @Volatile
        private var INSTANCE: ChronosDatabase? = null

        fun getDatabase(context: Context): ChronosDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ChronosDatabase::class.java,
                    "chronos_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
