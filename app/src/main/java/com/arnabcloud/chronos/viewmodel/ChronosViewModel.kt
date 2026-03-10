package com.arnabcloud.chronos.viewmodel

import android.app.Application
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import com.arnabcloud.chronos.model.Priority
import com.arnabcloud.chronos.model.TimelineItem
import com.arnabcloud.chronos.util.ReminderManager
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

class ChronosViewModel(applicationContext: Application) : AndroidViewModel(applicationContext) {
    private val _items = mutableStateListOf<TimelineItem>()
    private val reminderManager = ReminderManager(context = applicationContext)
    val items: List<TimelineItem> get() = _items


    init {
        // Sample data
        _items.addAll(
            elements = listOf(
                TimelineItem.Event(
                    title = "Design Sync",
                    startTime = LocalTime.of(10, 0),
                    endTime = LocalTime.of(11, 0),
                    details = "Weekly team sync"
                ),
                TimelineItem.Task(
                    title = "Update UI Components",
                    details = "Apply M3 guidelines",
                    date = LocalDate.now(),
                    deadlineDate = LocalDate.now().plusDays(2)
                ),
                TimelineItem.Event(
                    title = "Project Launch",
                    date = LocalDate.now().plusDays(1),
                    startTime = LocalTime.MIDNIGHT,
                    endTime = LocalTime.MAX,
                    isAllDay = true
                ),
                TimelineItem.Task(
                    title = "Submit Report",
                    date = LocalDate.now(),
                    deadlineDate = LocalDate.now(),
                    priority = Priority.HIGH
                )
            )
        )

        // Schedule reminders for initial sample data
        _items.forEach { reminderManager.scheduleReminder(it) }
    }

    fun addItem(item: TimelineItem) {
        _items.add(item)
        reminderManager.scheduleReminder(item)
    }

    fun removeItem(item: TimelineItem) {
        reminderManager.cancelReminder(item)
        _items.remove(item)
    }

    fun updateItem(updatedItem: TimelineItem) {
        val index = _items.indexOfFirst { it.id == updatedItem.id }
        if (index != -1) {
            _items[index] = updatedItem
            reminderManager.scheduleReminder(updatedItem)
        }
    }

    fun toggleComplete(item: TimelineItem) {
        if (item is TimelineItem.Task) {
            val index = _items.indexOfFirst { it.id == item.id }
            if (index != -1) {
                val updated = item.copy(isCompleted = !item.isCompleted)
                _items[index] = updated
                if (updated.isCompleted) {
                    reminderManager.cancelReminder(updated)
                } else {
                    reminderManager.scheduleReminder(updated)
                }
            }
        }
    }

    fun moveItems(itemIds: List<UUID>, newDate: LocalDate) {
        itemIds.forEach { id ->
            val index = _items.indexOfFirst { it.id == id }
            if (index != -1) {
                val updatedItem = when (val item = _items[index]) {
                    is TimelineItem.Task -> item.copy(deadlineDate = newDate)
                    is TimelineItem.Event -> item.copy(date = newDate)
                }
                _items[index] = updatedItem
                reminderManager.scheduleReminder(updatedItem)
            }
        }
    }

    fun getItemsForDate(date: LocalDate): List<TimelineItem> {
        return _items.filter { item ->
            when (item) {
                is TimelineItem.Task -> item.deadlineDate == date || item.date == date
                is TimelineItem.Event -> item.date == date
            }
        }.sortedWith(
            compareBy<TimelineItem> {
                when (it) {
                    is TimelineItem.Event -> if (it.isAllDay) 0 else 1
                    is TimelineItem.Task -> 2
                }
            }.thenBy {
                when (it) {
                    is TimelineItem.Event -> it.startTime
                    is TimelineItem.Task -> LocalTime.MAX
                }
            }
        )
    }
}
