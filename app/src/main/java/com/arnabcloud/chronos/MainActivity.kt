package com.arnabcloud.chronos

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddTask
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.arnabcloud.chronos.ui.components.AddTaskDialog
import com.arnabcloud.chronos.ui.screen.calender.ChronosCalendarScreen
import com.arnabcloud.chronos.ui.screen.home.ChronosTimelineScreen
import com.arnabcloud.chronos.ui.theme.ChronosTheme
import com.arnabcloud.chronos.ui.screen.vault.ChronosVaultScreen
import com.arnabcloud.chronos.viewmodel.ChronosViewModel
import kotlinx.coroutines.launch

// --- Navigation Routes ---
sealed class Screen(val label: String, val icon: ImageVector) {
    object Vault : Screen(label = "Tasks", icon = Icons.Default.Inventory2)
    object Timeline : Screen(label = "Today", icon = Icons.Default.Timeline)
    object Calendar : Screen(label = "Plan", icon = Icons.Default.CalendarMonth)
}

val bottomNavItems = listOf(
    Screen.Vault,
    Screen.Timeline,
    Screen.Calendar
)

sealed class DialogType {
    object Task : DialogType()
    object Event : DialogType()

}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ChronosTheme {
                RequestPermissions()
                MainNavigation()
            }
        }
    }
}

@Composable
fun RequestPermissions() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { _ ->
            // Handle permission result if needed
        }

        LaunchedEffect(Unit) {
            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainNavigation(viewModel: ChronosViewModel = viewModel()) {
    val pagerState = rememberPagerState(pageCount = { bottomNavItems.size })
    val coroutineScope = rememberCoroutineScope()
    var showSpeedDial by remember { mutableStateOf(value = false) }
    var activeDialog by remember { mutableStateOf<DialogType?>(value = null) }

    activeDialog?.let { dialogType ->
        AddTaskDialog(
            isEvent = dialogType is DialogType.Event,
            onDismiss = { activeDialog = null },
            onConfirm = { newItem ->
                viewModel.addItem(newItem)
                activeDialog = null
            }
        )
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                bottomNavItems.forEachIndexed { index, screen ->
                    NavigationBarItem(
                        selected = pagerState.currentPage == index,
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(page = index)
                            }
                        },
                        label = { Text(text = screen.label) },
                        icon = { Icon(imageVector = screen.icon, contentDescription = null) }
                    )
                }
            }
        },
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(space = 12.dp),
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                // Speed Dial Menu
                AnimatedVisibility(
                    visible = showSpeedDial,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(space = 12.dp)
                    ) {
                        ExtendedFloatingActionButton(
                            onClick = {
                                showSpeedDial = false
                                activeDialog = DialogType.Event
                            },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.Event,
                                    contentDescription = null
                                )
                            },
                            text = { Text(text = "Full Event") },
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        ExtendedFloatingActionButton(
                            onClick = {
                                showSpeedDial = false
                                activeDialog = DialogType.Task
                            },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.AddTask,
                                    contentDescription = null
                                )
                            },
                            text = { Text(text = "Quick Task") },
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }

                // Main FAB
                FloatingActionButton(
                    onClick = { showSpeedDial = !showSpeedDial },
                    shape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(
                        imageVector = if (showSpeedDial) Icons.Default.Close else Icons.Default.Add,
                        contentDescription = "New"
                    )
                }
            }
        }
    ) { innerPadding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.padding(paddingValues = innerPadding),
            userScrollEnabled = true
        ) { page ->
            when (bottomNavItems[page]) {
                Screen.Vault -> ChronosVaultScreen(viewModel)
                Screen.Timeline -> ChronosTimelineScreen(viewModel)
                Screen.Calendar -> ChronosCalendarScreen(viewModel)
            }
        }
    }
}
