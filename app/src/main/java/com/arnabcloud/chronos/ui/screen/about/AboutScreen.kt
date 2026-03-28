package com.arnabcloud.chronos.ui.screen.about

import android.content.Context
import android.content.Intent
import android.net.Uri
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
    val repoUrl = "https://github.com/Arnab-cloud/Chronos"

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
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("$repoUrl/blob/main/changelog.md"))
                        context.startActivity(intent)
                    }
                )
            }

            item { SettingsCategoryHeader(title = "Legal") }
            item {
                SettingsItem(
                    title = "Privacy Policy",
                    icon = Icons.Default.PrivacyTip,
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("$repoUrl/blob/main/PRIVACY.md"))
                        context.startActivity(intent)
                    }
                )
            }
            item {
                SettingsItem(
                    title = "Terms of Service",
                    icon = Icons.Default.Description,
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("$repoUrl/blob/main/TERMS.md"))
                        context.startActivity(intent)
                    }
                )
            }

            item { SettingsCategoryHeader(title = "Support") }
            item {
                SettingsItem(
                    title = "Contact Support",
                    icon = Icons.Default.Email,
                    onClick = {
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:arnab.santra.cse26@heritageit.edu.in")
                            putExtra(Intent.EXTRA_SUBJECT, "Chronos App Support - v$version")
                        }
                        context.startActivity(Intent.createChooser(intent, "Send Email"))
                    }
                )
            }
            item {
                SettingsItem(
                    title = "Star on GitHub ⭐",
                    icon = Icons.Default.Star,
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(repoUrl))
                        context.startActivity(intent)
                    }
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
