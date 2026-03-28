package com.arnabcloud.chronos.ui.screen.about

import android.content.Context
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.arnabcloud.chronos.ui.screen.settings.SettingsCategoryHeader
import com.arnabcloud.chronos.ui.screen.settings.SettingsItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(onBackClick: () -> Unit) {

    val context = LocalContext.current
    val version = remember { context.getAppVersionName() }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "About") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues = padding)
        ) {
            item { SettingsCategoryHeader(title = "App Info") }
            item {
                SettingsItem(
                    title = "App Version",
                    subtitle = version,
                    icon = Icons.Default.Info,
                    onClick = {}
                )
            }
            item {
                SettingsItem(
                    title = "Changelog",
                    icon = Icons.Default.History,
                    onClick = {}
                )
            }

            item { SettingsCategoryHeader(title = "Legal") }
            item {
                SettingsItem(
                    title = "Privacy Policy",
                    icon = Icons.Default.PrivacyTip,
                    onClick = {}
                )
            }
            item {
                SettingsItem(
                    title = "Terms of Service",
                    icon = Icons.Default.Description,
                    onClick = {}
                )
            }

            item { SettingsCategoryHeader(title = "Support") }
            item {
                SettingsItem(
                    title = "Contact Support",
                    icon = Icons.Default.Email,
                    onClick = {}
                )
            }
            item {
                SettingsItem(
                    title = "Rate App",
                    icon = Icons.Default.Star,
                    onClick = {}
                )
            }
        }
    }
}

fun Context.getAppVersionName(): String {
    return try {
        packageManager.getPackageInfo(packageName, 0).versionName ?: "N/A"
    } catch (_: Exception) {
        "N/A"
    }
}