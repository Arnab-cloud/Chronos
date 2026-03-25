package com.arnabcloud.chronos.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.arnabcloud.chronos.data.ChronosDatabase
import com.arnabcloud.chronos.data.ChronosRepository
import com.arnabcloud.chronos.data.SettingsDataStore
import com.arnabcloud.chronos.model.RecurrenceType
import com.arnabcloud.chronos.model.TimelineItem
import com.arnabcloud.chronos.util.ReminderManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

class ChronosViewModel(applicationContext: Application) : AndroidViewModel(applicationContext) {
    private val repository: ChronosRepository
    private val reminderManager = ReminderManager(context = applicationContext)
    private val settingsDataStore = SettingsDataStore(applicationContext)

    val items: StateFlow<List<TimelineItem>>

    init {
        val dao = ChronosDatabase.getDatabase(applicationContext).chronosDao()
        repository = ChronosRepository(dao)

        // Combine Tasks and Events into a single list of TimelineItems with dynamic sorting
        items = combine(
            repository.allTasks,
            repository.allEvents,
            settingsDataStore.sortOrderFlow
        ) { tasks, events, sortOrder ->
            val allItems = tasks + events
            when (sortOrder) {
                "By Priority" -> allItems.sortedWith(
                    compareByDescending<TimelineItem> {
                        if (it is TimelineItem.Task) it.priority.ordinal else -1
                    }.thenBy { it.date }
                )

                "By Creation Date" -> allItems.sortedBy { it.id.timestamp() } // UUID v7 if used, otherwise fallback
                "By Time" -> allItems.sortedWith(
                    compareBy<TimelineItem> { it.date }.thenBy {
                        when (it) {
                            is TimelineItem.Event -> it.startTime
                            is TimelineItem.Task -> it.taskTime ?: LocalTime.MAX
                        }
                    }
                )

                else -> allItems.sortedBy { it.date }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    // Fallback for non-v7 UUIDs to sort by "creation" (simplified)
    private fun UUID.timestamp(): Long = this.mostSignificantBits

    fun addItem(item: TimelineItem) {
        viewModelScope.launch {
            when (item) {
                is TimelineItem.Task -> repository.insertTask(item)
                is TimelineItem.Event -> repository.insertEvent(item)
            }
            reminderManager.scheduleReminder(item)
        }
    }

    fun removeItem(item: TimelineItem) {
        viewModelScope.launch {
            reminderManager.cancelReminder(item)
            when (item) {
                is TimelineItem.Task -> repository.deleteTask(item)
                is TimelineItem.Event -> repository.deleteEvent(item)
            }
        }
    }

    fun updateItem(updatedItem: TimelineItem) {
        viewModelScope.launch {
            when (updatedItem) {
                is TimelineItem.Task -> repository.updateTask(updatedItem)
                is TimelineItem.Event -> repository.updateEvent(updatedItem)
            }
            reminderManager.scheduleReminder(updatedItem)
        }
    }

    fun toggleComplete(item: TimelineItem) {
        if (item is TimelineItem.Task) {
            viewModelScope.launch {
                if (item.isPeriodic && !item.isCompleted) {
                    // For periodic tasks, instead of marking completed, move to next date
                    val nextDate = calculateNextDate(item.date, item.recurrence)
                    val nextDeadline =
                        item.deadlineDate?.let { calculateNextDate(it, item.recurrence) }

                    val updated = item.copy(
                        date = nextDate,
                        deadlineDate = nextDeadline,
                        isCompleted = false // Keep it active for the next occurrence
                    )
                    repository.updateTask(updated)
                    reminderManager.scheduleReminder(updated)
                } else {
                    val updated = item.copy(isCompleted = !item.isCompleted)
                    repository.updateTask(updated)
                    if (updated.isCompleted) {
                        reminderManager.cancelReminder(updated)
                    } else {
                        reminderManager.scheduleReminder(updated)
                    }
                }
            }
        }
    }

    private fun calculateNextDate(currentDate: LocalDate, recurrence: RecurrenceType?): LocalDate {
        return when (recurrence) {
            RecurrenceType.DAILY -> currentDate.plusDays(1)
            RecurrenceType.WEEKLY -> currentDate.plusWeeks(1)
            RecurrenceType.MONTHLY -> currentDate.plusMonths(1)
            RecurrenceType.YEARLY -> currentDate.plusYears(1)
            null -> currentDate
        }
    }

    fun moveItems(itemIds: List<UUID>, newDate: LocalDate) {
        viewModelScope.launch {
            items.value.filter { it.id in itemIds }.forEach { item ->
                val updatedItem = when (item) {
                    is TimelineItem.Task -> item.copy(deadlineDate = newDate)
                    is TimelineItem.Event -> item.copy(date = newDate)
                }
                updateItem(updatedItem)
            }
        }
    }

    fun getItemsForDate(date: LocalDate): List<TimelineItem> {
        return items.value.filter { item ->
            when (item) {
                is TimelineItem.Task -> item.deadlineDate == date || item.date == date
                is TimelineItem.Event -> item.date == date
            }
        }
    }
}
