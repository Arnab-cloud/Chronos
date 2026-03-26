package com.arnabcloud.chronos.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.arnabcloud.chronos.model.RecurrenceType
import com.arnabcloud.chronos.model.TimelineItem
import com.arnabcloud.chronos.receiver.ReminderReceiver
import java.time.LocalDateTime
import java.time.ZoneId

class AllDayDefaultTime {
    // Notify at 9 AM for all-day events
    companion object {
        const val DEFAULT_HOUR = 9
        const val DEFAULT_MINUTE = 0
    }
}

class ReminderManager(private val context: Context) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleReminder(item: TimelineItem) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                // If we can't schedule exact alarms, we might need to ask the user
                // or fallback to non-exact. For now, let's assume we'll handle permission in UI.
                return
            }
        }

        var reminderTime = getReminderTime(item) ?: return

        // Handle periodic tasks: If the initial time is in the past, move to the next occurrence
        if (item is TimelineItem.Task && item.isPeriodic) {
            val now = LocalDateTime.now()
            while (reminderTime.isBefore(now)) {
                reminderTime = calculateNextDateTime(reminderTime, item.recurrence)
            }
        }

        val epochMilli = reminderTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        // For non-periodic items, don't schedule if it's in the past
        if (!(item is TimelineItem.Task && item.isPeriodic) && epochMilli <= System.currentTimeMillis()) return

        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("ITEM_ID", item.id.toString())
            putExtra("ITEM_TITLE", item.title)
            putExtra("ITEM_TYPE", if (item is TimelineItem.Task) "TASK" else "EVENT")
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            item.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            epochMilli,
            pendingIntent
        )
    }

    fun cancelReminder(item: TimelineItem) {
        val intent = Intent(context, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            item.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    private fun getReminderTime(item: TimelineItem): LocalDateTime? {
        return when (item) {
            is TimelineItem.Event -> {
                if (item.isAllDay) {
                    item.date.atTime(
                        AllDayDefaultTime.DEFAULT_HOUR,
                        AllDayDefaultTime.DEFAULT_MINUTE
                    )
                } else {
                    item.date.atTime(item.startTime)
                }
            }

            is TimelineItem.Task -> {
                item.deadlineTime?.let {
                    item.deadlineDate?.atTime(it)
                } ?: item.taskTime?.let {
                    item.date.atTime(it)
                }
            }
        }
    }

    private fun calculateNextDateTime(
        current: LocalDateTime,
        recurrence: RecurrenceType?
    ): LocalDateTime {
        return when (recurrence) {
            RecurrenceType.DAILY -> current.plusDays(1)
            RecurrenceType.WEEKLY -> current.plusWeeks(1)
            RecurrenceType.MONTHLY -> current.plusMonths(1)
            RecurrenceType.YEARLY -> current.plusYears(1)
            null -> current
        }
    }
}
