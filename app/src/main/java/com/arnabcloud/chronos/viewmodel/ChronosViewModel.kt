package com.arnabcloud.chronos.viewmodel

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.arnabcloud.chronos.model.TimelineItem
import java.time.LocalDate
import java.time.LocalTime
import java.util.*

class ChronosViewModel : ViewModel() {
    private val _items = mutableStateListOf<TimelineItem>()
    val items: List<TimelineItem> get() = _items

    init {
        // Sample data
        _items.addAll(
            listOf(
                TimelineItem(
                    title = "Design Sync",
                    startTime = LocalTime.of(10, 0),
                    isTask = false,
                    details = "Weekly team sync"
                ),
                TimelineItem(
                    title = "Update UI Components",
                    startTime = LocalTime.of(14, 0),
                    details = "Apply M3 guidelines",
                    deadlineDate = LocalDate.now().plusDays(2)
                ),
                TimelineItem(
                    title = "Project Launch",
                    date = LocalDate.now().plusDays(1),
                    isAllDay = true,
                    isTask = false
                ),
                TimelineItem(
                    title = "Submit Report",
                    date = LocalDate.now(),
                    startTime = LocalTime.of(17, 0),
                    deadlineDate = LocalDate.now()
                )
            )
        )
    }

    fun addItem(item: TimelineItem) {
        _items.add(item)
        scheduleReminder(item)
    }

    fun removeItem(item: TimelineItem) {
        _items.remove(item)
    }

    fun toggleComplete(item: TimelineItem) {
        val index = _items.indexOfFirst { it.id == item.id }
        if (index != -1) {
            _items[index] = _items[index].copy(isCompleted = !item.isCompleted)
        }
    }

    fun moveItems(itemIds: List<UUID>, newDate: LocalDate) {
        itemIds.forEach { id ->
            val index = _items.indexOfFirst { it.id == id }
            if (index != -1) {
                _items[index] = _items[index].copy(date = newDate)
            }
        }
    }

    fun getItemsForDate(date: LocalDate): List<TimelineItem> {
        return _items.filter { it.date == date }.sortedWith(
            compareBy<TimelineItem> { !it.isAllDay }
                .thenBy { it.startTime ?: LocalTime.MIN }
        )
    }

    private fun scheduleReminder(item: TimelineItem) {
        // Placeholder for AlarmManager/WorkManager logic
        if (item.startTime != null || item.isAllDay) {
            Log.d("ChronosVM", "Reminder scheduled for ${item.title} at ${item.date} ${item.startTime}")
        }
    }
}
