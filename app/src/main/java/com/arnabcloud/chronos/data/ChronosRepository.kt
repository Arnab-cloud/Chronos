package com.arnabcloud.chronos.data

import com.arnabcloud.chronos.model.TimelineItem
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class ChronosRepository(private val chronosDao: ChronosDao) {
    val allTasks: Flow<List<TimelineItem.Task>> = chronosDao.getAllTasks()
    val allEvents: Flow<List<TimelineItem.Event>> = chronosDao.getAllEvents()

    suspend fun insertTask(task: TimelineItem.Task) {
        chronosDao.insertTask(task)
    }

    suspend fun updateTask(task: TimelineItem.Task) {
        chronosDao.updateTask(task)
    }

    suspend fun deleteTask(task: TimelineItem.Task) {
        chronosDao.deleteTask(task)
    }

    suspend fun deleteTaskById(taskId: UUID) {
        chronosDao.deleteTaskById(taskId)
    }

    suspend fun insertEvent(event: TimelineItem.Event) {
        chronosDao.insertEvent(event)
    }

    suspend fun updateEvent(event: TimelineItem.Event) {
        chronosDao.updateEvent(event)
    }

    suspend fun deleteEvent(event: TimelineItem.Event) {
        chronosDao.deleteEvent(event)
    }

    suspend fun deleteEventById(eventId: UUID) {
        chronosDao.deleteEventById(eventId)
    }
}
