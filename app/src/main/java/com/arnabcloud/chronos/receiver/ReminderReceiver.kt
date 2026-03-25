package com.arnabcloud.chronos.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.arnabcloud.chronos.MainActivity
import com.arnabcloud.chronos.data.ChronosDatabase
import com.arnabcloud.chronos.data.SettingsDataStore
import com.arnabcloud.chronos.model.RecurrenceType
import com.arnabcloud.chronos.model.TimelineItem
import com.arnabcloud.chronos.service.ReminderService
import com.arnabcloud.chronos.util.ReminderManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.UUID

class ReminderReceiver : BroadcastReceiver() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onReceive(context: Context, intent: Intent) {
        val itemId = intent.getStringExtra("ITEM_ID")
        val itemTitle = intent.getStringExtra("ITEM_TITLE") ?: "Chronos Reminder"
        val itemType = intent.getStringExtra("ITEM_TYPE") ?: "TASK"

        val pendingResult = goAsync()
        scope.launch {
            try {
                // Handle periodic re-scheduling
                if (itemType == "TASK" && itemId != null) {
                    handlePeriodicTask(context, itemId)
                }

                val settingsDataStore = SettingsDataStore(context)
                val reminderType = settingsDataStore.reminderTypeFlow.first()

                if (reminderType == "Alarm") {
                    startAlarmService(context, itemId, itemTitle, itemType)
                } else {
                    showSimpleNotification(context, itemId, itemTitle, itemType)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    private suspend fun handlePeriodicTask(context: Context, itemId: String) {
        val db = ChronosDatabase.getDatabase(context)
        val dao = db.chronosDao()
        val task = dao.getTaskById(UUID.fromString(itemId))

        if (task != null && task.isPeriodic) {
            val nextDate = calculateNextDate(task.date, task.recurrence)
            val nextDeadline = task.deadlineDate?.let { calculateNextDate(it, task.recurrence) }
            
            val updatedTask = task.copy(
                date = nextDate,
                deadlineDate = nextDeadline,
                isCompleted = false
            )
            
            dao.updateTask(updatedTask)
            
            // Schedule the next reminder
            val reminderManager = ReminderManager(context)
            reminderManager.scheduleReminder(updatedTask)
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

    private fun startAlarmService(context: Context, itemId: String?, title: String, type: String) {
        val serviceIntent = Intent(context, ReminderService::class.java).apply {
            putExtra("ITEM_ID", itemId)
            putExtra("ITEM_TITLE", title)
            putExtra("ITEM_TYPE", type)
        }

        ContextCompat.startForegroundService(context, serviceIntent)
    }

    private fun showSimpleNotification(
        context: Context,
        itemId: String?,
        title: String,
        type: String
    ) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "chronos_reminders"

        if (notificationManager.getNotificationChannel(channelId) == null) {
            val channel = NotificationChannel(
                channelId,
                "Chronos Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for tasks and events"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("SCROLL_TO_ITEM", itemId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            itemId?.hashCode() ?: 0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(if (type == "TASK") "Task Reminder" else "Event Reminder")
            .setContentText(title)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(itemId?.hashCode() ?: 0, notification)
    }
}
