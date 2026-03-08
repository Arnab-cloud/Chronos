package com.arnabcloud.chronos.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.arnabcloud.chronos.MainActivity

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val itemId = intent.getStringExtra("ITEM_ID")
        val itemTitle = intent.getStringExtra("ITEM_TITLE") ?: "Chronos Reminder"
        val itemType = intent.getStringExtra("ITEM_TYPE") ?: "TASK"

        showNotification(context, itemId, itemTitle, itemType)
    }

    private fun showNotification(context: Context, itemId: String?, title: String, type: String) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "chronos_reminders"

        val channel = NotificationChannel(
            channelId,
            "Chronos Reminders",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notifications for tasks and events"
        }
        notificationManager.createNotificationChannel(channel)

        val activityIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            // In a real app, you'd pass the itemId to navigate to the specific item
            putExtra("SCROLL_TO_ITEM", itemId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            itemId?.hashCode() ?: 0,
            activityIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Using system icon for now
            .setContentTitle(if (type == "TASK") "Task Reminder" else "Event Starting")
            .setContentText(title)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(itemId?.hashCode() ?: 0, notification)
    }
}
