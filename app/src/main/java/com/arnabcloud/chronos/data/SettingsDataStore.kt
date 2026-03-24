package com.arnabcloud.chronos.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
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
        val REMINDER_TONE_KEY = stringPreferencesKey("reminder_tone")
        val SILENT_MODE_OVERRIDE_KEY = booleanPreferencesKey("silent_mode_override")
    }

    val themeFlow: Flow<String> = context.dataStore.data.map { it[THEME_KEY] ?: "System Default" }
    val layoutFlow: Flow<String> = context.dataStore.data.map { it[LAYOUT_KEY] ?: "Cards" }
    val animationsFlow: Flow<Boolean> = context.dataStore.data.map { it[ANIMATIONS_KEY] ?: true }
    val compactModeFlow: Flow<Boolean> = context.dataStore.data.map { it[COMPACT_MODE_KEY] ?: false }
    val sortOrderFlow: Flow<String> = context.dataStore.data.map { it[SORT_ORDER_KEY] ?: "By Time" }
    val vibrationEnabledFlow: Flow<Boolean> = context.dataStore.data.map { it[VIBRATION_ENABLED_KEY] ?: true }
    val headsUpNotificationsFlow: Flow<Boolean> = context.dataStore.data.map { it[HEADS_UP_NOTIFICATIONS_KEY] ?: true }
    val snoozeDurationFlow: Flow<Int> = context.dataStore.data.map { it[SNOOZE_DURATION_KEY] ?: 5 }
    val repeatAlertsFlow: Flow<Boolean> = context.dataStore.data.map { it[REPEAT_ALERTS_KEY] ?: false }
    val accentColorFlow: Flow<String> = context.dataStore.data.map { it[ACCENT_COLOR_KEY] ?: "Default" }
    val reminderToneFlow: Flow<String> = context.dataStore.data.map { it[REMINDER_TONE_KEY] ?: "" }
    val silentModeOverrideFlow: Flow<Boolean> = context.dataStore.data.map { it[SILENT_MODE_OVERRIDE_KEY] ?: false }

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

    suspend fun setReminderTone(toneUri: String) {
        context.dataStore.edit { it[REMINDER_TONE_KEY] = toneUri }
    }

    suspend fun setSilentModeOverride(enabled: Boolean) {
        context.dataStore.edit { it[SILENT_MODE_OVERRIDE_KEY] = enabled }
    }
}
