package com.arnabcloud.chronos.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsDataStore(private val context: Context) {

    companion object {
        val THEME_KEY = stringPreferencesKey("theme")
        val LAYOUT_KEY = stringPreferencesKey("layout")
        val ANIMATIONS_KEY = booleanPreferencesKey("animations")
        val COMPACT_MODE_KEY = booleanPreferencesKey("compact_mode")
        val SORT_ORDER_KEY = stringPreferencesKey("sort_order")
        val VIBRATION_ENABLED_KEY = booleanPreferencesKey("vibration_enabled")
        val HEADS_UP_NOTIFICATIONS_KEY = booleanPreferencesKey("heads_up_notifications")
        val SNOOZE_DURATION_KEY = intPreferencesKey("snooze_duration")
        val REPEAT_ALERTS_KEY = booleanPreferencesKey("repeat_alerts")
        val ACCENT_COLOR_KEY = stringPreferencesKey("accent_color")
        val REMINDER_TYPE_KEY = stringPreferencesKey("reminder_type")
        val NOTIFICATION_TONE_KEY = stringPreferencesKey("notification_tone")
        val ALARM_TONE_KEY = stringPreferencesKey("alarm_tone")
        val SILENT_MODE_OVERRIDE_KEY = booleanPreferencesKey("silent_mode_override")
        val PRE_REMINDER_ENABLED_KEY = booleanPreferencesKey("pre_reminder_enabled")
        val PRE_REMINDER_TIME_KEY = intPreferencesKey("pre_reminder_time")
        val ALARM_DURATION_KEY = intPreferencesKey("alarm_duration") // New key: 0 means infinite
        val DEFAULT_REPETITIVE_TIME_KEY = stringPreferencesKey("default_repetitive_time")
    }

    val themeFlow: Flow<String> = context.dataStore.data.map { it[THEME_KEY] ?: "System Default" }
    val layoutFlow: Flow<String> = context.dataStore.data.map { it[LAYOUT_KEY] ?: "Cards" }
    val animationsFlow: Flow<Boolean> = context.dataStore.data.map { it[ANIMATIONS_KEY] ?: true }
    val compactModeFlow: Flow<Boolean> =
        context.dataStore.data.map { it[COMPACT_MODE_KEY] ?: false }
    val sortOrderFlow: Flow<String> = context.dataStore.data.map { it[SORT_ORDER_KEY] ?: "By Time" }
    val vibrationEnabledFlow: Flow<Boolean> =
        context.dataStore.data.map { it[VIBRATION_ENABLED_KEY] ?: true }
    val headsUpNotificationsFlow: Flow<Boolean> =
        context.dataStore.data.map { it[HEADS_UP_NOTIFICATIONS_KEY] ?: true }
    val snoozeDurationFlow: Flow<Int> = context.dataStore.data.map { it[SNOOZE_DURATION_KEY] ?: 5 }
    val repeatAlertsFlow: Flow<Boolean> =
        context.dataStore.data.map { it[REPEAT_ALERTS_KEY] ?: false }
    val accentColorFlow: Flow<String> =
        context.dataStore.data.map { it[ACCENT_COLOR_KEY] ?: "Default" }
    val reminderTypeFlow: Flow<String> =
        context.dataStore.data.map { it[REMINDER_TYPE_KEY] ?: "Notification" }
    val notificationToneFlow: Flow<String> =
        context.dataStore.data.map { it[NOTIFICATION_TONE_KEY] ?: "" }
    val alarmToneFlow: Flow<String> = context.dataStore.data.map { it[ALARM_TONE_KEY] ?: "" }
    val silentModeOverrideFlow: Flow<Boolean> =
        context.dataStore.data.map { it[SILENT_MODE_OVERRIDE_KEY] ?: false }
    val preReminderEnabledFlow: Flow<Boolean> =
        context.dataStore.data.map { it[PRE_REMINDER_ENABLED_KEY] ?: false }
    val preReminderTimeFlow: Flow<Int> =
        context.dataStore.data.map { it[PRE_REMINDER_TIME_KEY] ?: 10 }
    val alarmDurationFlow: Flow<Int> = context.dataStore.data.map { it[ALARM_DURATION_KEY] ?: 0 }
    val defaultRepetitiveTimeFlow: Flow<String> =
        context.dataStore.data.map { it[DEFAULT_REPETITIVE_TIME_KEY] ?: "09:00" }

    suspend fun setTheme(theme: String) {
        context.dataStore.edit { it[THEME_KEY] = theme }
    }

    suspend fun setLayout(layout: String) {
        context.dataStore.edit { it[LAYOUT_KEY] = layout }
    }

    suspend fun setAnimations(enabled: Boolean) {
        context.dataStore.edit { it[ANIMATIONS_KEY] = enabled }
    }

    suspend fun setCompactMode(enabled: Boolean) {
        context.dataStore.edit { it[COMPACT_MODE_KEY] = enabled }
    }

    suspend fun setSortOrder(order: String) {
        context.dataStore.edit { it[SORT_ORDER_KEY] = order }
    }

    suspend fun setVibrationEnabled(enabled: Boolean) {
        context.dataStore.edit { it[VIBRATION_ENABLED_KEY] = enabled }
    }

    suspend fun setHeadsUpNotifications(enabled: Boolean) {
        context.dataStore.edit { it[HEADS_UP_NOTIFICATIONS_KEY] = enabled }
    }

    suspend fun setSnoozeDuration(minutes: Int) {
        context.dataStore.edit { it[SNOOZE_DURATION_KEY] = minutes }
    }

    suspend fun setRepeatAlerts(enabled: Boolean) {
        context.dataStore.edit { it[REPEAT_ALERTS_KEY] = enabled }
    }

    suspend fun setAccentColor(color: String) {
        context.dataStore.edit { it[ACCENT_COLOR_KEY] = color }
    }

    suspend fun setReminderType(type: String) {
        context.dataStore.edit { it[REMINDER_TYPE_KEY] = type }
    }

    suspend fun setNotificationTone(toneUri: String) {
        context.dataStore.edit { it[NOTIFICATION_TONE_KEY] = toneUri }
    }

    suspend fun setAlarmTone(toneUri: String) {
        context.dataStore.edit { it[ALARM_TONE_KEY] = toneUri }
    }

    suspend fun setSilentModeOverride(enabled: Boolean) {
        context.dataStore.edit { it[SILENT_MODE_OVERRIDE_KEY] = enabled }
    }

    suspend fun setPreReminderEnabled(enabled: Boolean) {
        context.dataStore.edit { it[PRE_REMINDER_ENABLED_KEY] = enabled }
    }

    suspend fun setPreReminderTime(minutes: Int) {
        context.dataStore.edit { it[PRE_REMINDER_TIME_KEY] = minutes }
    }

    suspend fun setAlarmDuration(minutes: Int) {
        context.dataStore.edit { it[ALARM_DURATION_KEY] = minutes }
    }

    suspend fun setDefaultRepetitiveTime(time: String) {
        context.dataStore.edit { it[DEFAULT_REPETITIVE_TIME_KEY] = time }
    }
}
