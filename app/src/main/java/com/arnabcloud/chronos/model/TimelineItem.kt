package com.arnabcloud.chronos.model

import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

data class TimelineItem(
    val id: UUID = UUID.randomUUID(),
    val title: String,
    val details: String = "",
    val date: LocalDate = LocalDate.now(),
    val startTime: LocalTime? = null,
    val isAllDay: Boolean = false,
    val deadlineDate: LocalDate? = null,
    val isTask: Boolean = true,
    val isCompleted: Boolean = false
)
