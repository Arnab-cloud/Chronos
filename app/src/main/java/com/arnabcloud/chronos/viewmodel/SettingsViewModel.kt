package com.arnabcloud.chronos.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.arnabcloud.chronos.data.SettingsDataStore
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val settingsDataStore = SettingsDataStore(application)

    val theme: StateFlow<String> = settingsDataStore.themeFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), "System Default"
    )
    val layout: StateFlow<String> = settingsDataStore.layoutFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), "Cards"
    )
    val animationsEnabled: StateFlow<Boolean> = settingsDataStore.animationsFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), true
    )
    val compactModeEnabled: StateFlow<Boolean> = settingsDataStore.compactModeFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), false
    )
    val sortOrder: StateFlow<String> = settingsDataStore.sortOrderFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), "By Time"
    )
    val vibrationEnabled: StateFlow<Boolean> = settingsDataStore.vibrationEnabledFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), true
    )
    val headsUpNotifications: StateFlow<Boolean> =
        settingsDataStore.headsUpNotificationsFlow.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), true
        )
    val snoozeDuration: StateFlow<Int> = settingsDataStore.snoozeDurationFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), 5
    )
    val repeatAlerts: StateFlow<Boolean> = settingsDataStore.repeatAlertsFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), false
    )
    val accentColor: StateFlow<String> = settingsDataStore.accentColorFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), "Default"
    )
    val silentModeOverride: StateFlow<Boolean> = settingsDataStore.silentModeOverrideFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), false
    )
    val reminderType: StateFlow<String> = settingsDataStore.reminderTypeFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), "Notification"
    )
    val notificationTone: StateFlow<String> = settingsDataStore.notificationToneFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), ""
    )
    val alarmTone: StateFlow<String> = settingsDataStore.alarmToneFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), ""
    )

    fun setTheme(theme: String) = viewModelScope.launch { settingsDataStore.setTheme(theme) }
    fun setLayout(layout: String) = viewModelScope.launch { settingsDataStore.setLayout(layout) }
    fun setAnimations(enabled: Boolean) =
        viewModelScope.launch { settingsDataStore.setAnimations(enabled) }

    fun setCompactMode(enabled: Boolean) =
        viewModelScope.launch { settingsDataStore.setCompactMode(enabled) }

    fun setSortOrder(order: String) =
        viewModelScope.launch { settingsDataStore.setSortOrder(order) }

    fun setVibrationEnabled(enabled: Boolean) =
        viewModelScope.launch { settingsDataStore.setVibrationEnabled(enabled) }

    fun setHeadsUpNotifications(enabled: Boolean) =
        viewModelScope.launch { settingsDataStore.setHeadsUpNotifications(enabled) }

    fun setSnoozeDuration(minutes: Int) =
        viewModelScope.launch { settingsDataStore.setSnoozeDuration(minutes) }

    fun setRepeatAlerts(enabled: Boolean) =
        viewModelScope.launch { settingsDataStore.setRepeatAlerts(enabled) }

    fun setAccentColor(color: String) =
        viewModelScope.launch { settingsDataStore.setAccentColor(color) }

    fun setSilentModeOverride(enabled: Boolean) =
        viewModelScope.launch { settingsDataStore.setSilentModeOverride(enabled) }

    fun setReminderType(type: String) =
        viewModelScope.launch { settingsDataStore.setReminderType(type) }

    fun setNotificationTone(toneUri: String) =
        viewModelScope.launch { settingsDataStore.setNotificationTone(toneUri) }

    fun setAlarmTone(toneUri: String) =
        viewModelScope.launch { settingsDataStore.setAlarmTone(toneUri) }
}
