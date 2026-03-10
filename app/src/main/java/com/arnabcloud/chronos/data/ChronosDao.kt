package com.arnabcloud.chronos.data

import androidx.room.*
import com.arnabcloud.chronos.model.TimelineItem
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface ChronosDao {
    // Tasks
    @Query("SELECT * FROM tasks ORDER BY date ASC, taskTime ASC")
    fun getAllTasks(): Flow<List<TimelineItem.Task>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TimelineItem.Task)

    @Update
    suspend fun updateTask(task: TimelineItem.Task)

    @Delete
    suspend fun deleteTask(task: TimelineItem.Task)

    @Query("DELETE FROM tasks WHERE id = :taskId")
    suspend fun deleteTaskById(taskId: UUID)

    // Events
    @Query("SELECT * FROM events ORDER BY date ASC, startTime ASC")
    fun getAllEvents(): Flow<List<TimelineItem.Event>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: TimelineItem.Event)

    @Update
    suspend fun updateEvent(event: TimelineItem.Event)

    @Delete
    suspend fun deleteEvent(event: TimelineItem.Event)
    
    @Query("DELETE FROM events WHERE id = :eventId")
    suspend fun deleteEventById(eventId: UUID)
}
