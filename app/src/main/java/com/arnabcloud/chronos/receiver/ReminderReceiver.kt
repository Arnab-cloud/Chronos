package com.arnabcloud.chronos.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.arnabcloud.chronos.service.ReminderService

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val itemId = intent.getStringExtra("ITEM_ID")
        val itemTitle = intent.getStringExtra("ITEM_TITLE") ?: "Chronos Reminder"
        val itemType = intent.getStringExtra("ITEM_TYPE") ?: "TASK"

        startAlarmService(context, itemId, itemTitle, itemType)
    }

    private fun startAlarmService(context: Context, itemId: String?, title: String, type: String) {
        val serviceIntent = Intent(context, ReminderService::class.java).apply {
            putExtra("ITEM_ID", itemId)
            putExtra("ITEM_TITLE", title)
            putExtra("ITEM_TYPE", type)
        }

        ContextCompat.startForegroundService(context, serviceIntent)
    }
}
