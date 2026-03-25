package com.arnabcloud.chronos.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

sealed class TimelineItem {
    abstract val id: UUID
    abstract val title: String
    abstract val details: String
    abstract val date: LocalDate

    @Entity(tableName = "tasks")
    data class Task(
        @PrimaryKey override val id: UUID = UUID.randomUUID(),
        override val title: String,
        override val details: String = "",
        override val date: LocalDate = LocalDate.now(),
        val taskTime: LocalTime? = null,
        val deadlineDate: LocalDate? = null,
        val deadlineTime: LocalTime? = null,
        val isCompleted: Boolean = false,
        val priority: Priority = Priority.MEDIUM,
        val isPeriodic: Boolean = false,
        val recurrence: RecurrenceType? = null
    ) : TimelineItem() {
        fun isMissed(): Boolean = !isCompleted && deadlineDate?.isBefore(LocalDate.now()) == true
    }

    @Entity(tableName = "events")
    data class Event(
        @PrimaryKey override val id: UUID = UUID.randomUUID(),
        override val title: String,
        override val details: String = "",
        override val date: LocalDate = LocalDate.now(),
        val startTime: LocalTime,
        val endTime: LocalTime,
        val isAllDay: Boolean = false,
        val location: String? = null
    ) : TimelineItem()
}

enum class Priority { LOW, MEDIUM, HIGH }

enum class RecurrenceType { DAILY, WEEKLY, MONTHLY, YEARLY }
