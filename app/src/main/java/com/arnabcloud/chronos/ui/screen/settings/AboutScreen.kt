package com.arnabcloud.chronos.ui.screen.settings

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
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(onBackClick: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("About") },
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
            item {
                SettingsCategoryHeader(title = "About & Support")
            }
            item {
                SettingsItem(
                    title = "App Version",
                    subtitle = "1.0.0",
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
