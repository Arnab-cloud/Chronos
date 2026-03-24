package com.arnabcloud.chronos.ui.screen.settings

import android.app.Activity
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Announcement
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Animation
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.SettingsSuggest
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.ViewStream
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import com.arnabcloud.chronos.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    viewModel: SettingsViewModel = viewModel()
) {
    val context = LocalContext.current
    val theme by viewModel.theme.collectAsState()
    val layout by viewModel.layout.collectAsState()
    val animationsEnabled by viewModel.animationsEnabled.collectAsState()
    val compactModeEnabled by viewModel.compactModeEnabled.collectAsState()
    val sortOrder by viewModel.sortOrder.collectAsState()
    val vibrationEnabled by viewModel.vibrationEnabled.collectAsState()
    val headsUpNotifications by viewModel.headsUpNotifications.collectAsState()
    val snoozeDuration by viewModel.snoozeDuration.collectAsState()
    val repeatAlerts by viewModel.repeatAlerts.collectAsState()
    val accentColor by viewModel.accentColor.collectAsState()
    val silentModeOverride by viewModel.silentModeOverride.collectAsState()
    val reminderTone by viewModel.reminderTone.collectAsState()

    var showThemeDialog by remember { mutableStateOf(false) }
    var showLayoutDialog by remember { mutableStateOf(false) }
    var showSortDialog by remember { mutableStateOf(false) }
    var showSnoozeDialog by remember { mutableStateOf(false) }
    var showAccentColorDialog by remember { mutableStateOf(false) }

    val ringtonePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri =
                result.data?.getParcelableExtra<Uri>(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
            viewModel.setReminderTone(uri?.toString() ?: "")
        }
    }

    val reminderToneTitle = remember(reminderTone) {
        if (reminderTone.isEmpty()) {
            "Default"
        } else {
            try {
                RingtoneManager.getRingtone(context, reminderTone.toUri())?.getTitle(context)
                    ?: "Unknown"
            } catch (_: Exception) {
                "Default"
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // --- Appearance ---
            item { SettingsCategoryHeader(title = "Appearance") }
            item {
                SettingsItem(
                    title = "Theme",
                    subtitle = theme,
                    icon = Icons.Default.Palette,
                    onClick = { showThemeDialog = true }
                )
            }
            item {
                SettingsItem(
                    title = "Accent Color",
                    subtitle = accentColor,
                    icon = Icons.Default.ColorLens,
                    onClick = { showAccentColorDialog = true }
                )
            }
            item {
                SettingsSwitchItem(
                    title = "Compact Mode",
                    subtitle = "Reduce spacing and font size",
                    icon = Icons.Default.FormatSize,
                    checked = compactModeEnabled,
                    onCheckedChange = { viewModel.setCompactMode(it) }
                )
            }
            item {
                SettingsItem(
                    title = "List Layout",
                    subtitle = layout,
                    icon = Icons.Default.ViewStream,
                    onClick = { showLayoutDialog = true }
                )
            }
            item {
                SettingsSwitchItem(
                    title = "Animations",
                    subtitle = "Enable UI transitions",
                    icon = Icons.Default.Animation,
                    checked = animationsEnabled,
                    onCheckedChange = { viewModel.setAnimations(it) }
                )
            }

            // --- Notifications ---
            item { SettingsCategoryHeader(title = "Notifications") }
            item {
                SettingsItem(
                    title = "Reminder Tone",
                    subtitle = reminderToneTitle,
                    icon = Icons.Default.Notifications,
                    onClick = {
                        val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
                            putExtra(
                                RingtoneManager.EXTRA_RINGTONE_TYPE,
                                RingtoneManager.TYPE_NOTIFICATION
                            )
                            putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Reminder Tone")
                            putExtra(
                                RingtoneManager.EXTRA_RINGTONE_EXISTING_URI,
                                if (reminderTone.isNotEmpty()) reminderTone.toUri() else null
                            )
                            putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
                            putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true)
                        }
                        ringtonePickerLauncher.launch(intent)
                    }
                )
            }
            item {
                SettingsSwitchItem(
                    title = "Vibration",
                    subtitle = "Vibrate on reminders",
                    icon = Icons.Default.Vibration,
                    checked = vibrationEnabled,
                    onCheckedChange = { viewModel.setVibrationEnabled(it) }
                )
            }
            item {
                SettingsSwitchItem(
                    title = "Heads-up Notifications",
                    subtitle = "Show banners on top of screen",
                    icon = Icons.AutoMirrored.Filled.Announcement,
                    checked = headsUpNotifications,
                    onCheckedChange = { viewModel.setHeadsUpNotifications(it) }
                )
            }
            item {
                SettingsSwitchItem(
                    title = "Silent Mode Override",
                    subtitle = "Play sound even in silent mode",
                    icon = Icons.AutoMirrored.Filled.VolumeUp,
                    checked = silentModeOverride,
                    onCheckedChange = { viewModel.setSilentModeOverride(it) }
                )
            }
            item {
                SettingsItem(
                    title = "System Notification Channels",
                    subtitle = "Manage Android system settings",
                    icon = Icons.Default.SettingsSuggest,
                    onClick = {
                        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                        }
                        context.startActivity(intent)
                    }
                )
            }

            // --- Reminder Behavior ---
            item { SettingsCategoryHeader(title = "Reminder Behavior") }
            item {
                SettingsItem(
                    title = "Snooze Duration",
                    subtitle = "$snoozeDuration minutes",
                    icon = Icons.Default.Snooze,
                    onClick = { showSnoozeDialog = true }
                )
            }
            item {
                SettingsSwitchItem(
                    title = "Repeat Alerts",
                    subtitle = "Repeat until dismissed",
                    icon = Icons.Default.Repeat,
                    checked = repeatAlerts,
                    onCheckedChange = { viewModel.setRepeatAlerts(it) }
                )
            }
            item {
                SettingsItem(
                    title = "Sort Order",
                    subtitle = sortOrder,
                    icon = Icons.AutoMirrored.Filled.Sort,
                    onClick = { showSortDialog = true }
                )
            }
        }
    }

    // Dialogs
    if (showThemeDialog) {
        ThemeSelectionDialog(
            currentTheme = theme,
            onDismiss = { showThemeDialog = false },
            onSelect = {
                viewModel.setTheme(it)
                showThemeDialog = false
            }
        )
    }

    if (showAccentColorDialog) {
        AccentColorSelectionDialog(
            currentColor = accentColor,
            onDismiss = { showAccentColorDialog = false },
            onSelect = {
                viewModel.setAccentColor(it)
                showAccentColorDialog = false
            }
        )
    }

    if (showLayoutDialog) {
        LayoutSelectionDialog(
            currentLayout = layout,
            onDismiss = { showLayoutDialog = false },
            onSelect = {
                viewModel.setLayout(it)
                showLayoutDialog = false
            }
        )
    }

    if (showSortDialog) {
        SortSelectionDialog(
            currentSort = sortOrder,
            onDismiss = { showSortDialog = false },
            onSelect = {
                viewModel.setSortOrder(it)
                showSortDialog = false
            }
        )
    }

    if (showSnoozeDialog) {
        SnoozeSelectionDialog(
            currentDuration = snoozeDuration,
            onDismiss = { showSnoozeDialog = false },
            onSelect = {
                viewModel.setSnoozeDuration(it)
                showSnoozeDialog = false
            }
        )
    }
}
