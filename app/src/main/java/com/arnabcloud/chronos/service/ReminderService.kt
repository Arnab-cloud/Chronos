package com.arnabcloud.chronos.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.AudioAttributes
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import com.arnabcloud.chronos.MainActivity
import com.arnabcloud.chronos.data.SettingsDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ReminderService : Service() {
    private var ringtone: Ringtone? = null
    private var vibrator: Vibrator? = null
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val handler = Handler(Looper.getMainLooper())
    private var stopRunnable: Runnable? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val itemId = intent?.getStringExtra("ITEM_ID")
        val itemTitle = intent?.getStringExtra("ITEM_TITLE") ?: "Chronos Reminder"
        val itemType = intent?.getStringExtra("ITEM_TYPE") ?: "TASK"
        val action = intent?.action

        if (action == ACTION_STOP) {
            stopAlarm()
            stopSelf()
            return START_NOT_STICKY
        }

        showNotification(itemId, itemTitle, itemType)

        serviceScope.launch {
            val settingsDataStore = SettingsDataStore(applicationContext)
            val alarmToneUri = settingsDataStore.alarmToneFlow.first()
            val vibrationEnabled = settingsDataStore.vibrationEnabledFlow.first()
            val silentModeOverride = settingsDataStore.silentModeOverrideFlow.first()
            val alarmDurationMinutes = settingsDataStore.alarmDurationFlow.first()

            startAlarm(alarmToneUri, vibrationEnabled, silentModeOverride)

            if (alarmDurationMinutes > 0) {
                stopRunnable?.let { handler.removeCallbacks(it) }
                stopRunnable = Runnable {
                    stopAlarm()
                    // We might want to keep the notification but stop the sound
                    // Or stop the whole service. Usually, stop the service to stop foreground.
                    stopSelf()
                }
                handler.postDelayed(stopRunnable!!, alarmDurationMinutes * 60 * 1000L)
            }
        }

        return START_STICKY
    }

    private fun startAlarm(
        toneUriString: String,
        vibrationEnabled: Boolean,
        silentModeOverride: Boolean
    ) {
        val alarmUri = if (toneUriString.isNotEmpty()) {
            toneUriString.toUri()
        } else {
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
        }

        ringtone = RingtoneManager.getRingtone(applicationContext, alarmUri)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ringtone?.isLooping = true
        }

        if (silentModeOverride) {
            ringtone?.audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        }

        ringtone?.play()

        if (vibrationEnabled) {
            vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager =
                    getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibratorManager.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                getSystemService(VIBRATOR_SERVICE) as Vibrator
            }

            val pattern = longArrayOf(0, 500, 500)
            vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
        }
    }

    private fun stopAlarm() {
        ringtone?.stop()
        vibrator?.cancel()
        stopRunnable?.let { handler.removeCallbacks(it) }
    }

    private fun showNotification(itemId: String?, title: String, type: String) {
        val channelId = "chronos_alarms"
        val notificationManager =
            getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        val channel = NotificationChannel(
            channelId,
            "Chronos Alarms",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Ongoing alarms for tasks and events"
            setSound(null, null)
            enableVibration(false)
        }
        notificationManager.createNotificationChannel(channel)

        val stopIntent = Intent(this, ReminderService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            0,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val fullScreenIntent = Intent(this, MainActivity::class.java).apply {
            putExtra("SCROLL_TO_ITEM", itemId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val fullScreenPendingIntent = PendingIntent.getActivity(
            this,
            itemId?.hashCode() ?: 0,
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(if (type == "TASK") "Task Alarm" else "Event Alarm")
            .setContentText(title)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setOngoing(true)
            .setAutoCancel(false)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop", stopPendingIntent)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    override fun onDestroy() {
        stopAlarm()
        super.onDestroy()
    }

    companion object {
        const val ACTION_STOP = "com.arnabcloud.chronos.STOP_ALARM"
        const val NOTIFICATION_ID = 1001
    }
}
