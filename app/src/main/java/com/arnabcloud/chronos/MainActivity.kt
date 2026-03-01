package com.arnabcloud.chronos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import com.arnabcloud.chronos.ui.calendar.ChronosCalendarScreen
import com.arnabcloud.chronos.ui.home.ChronosTimelineScreen
import com.arnabcloud.chronos.ui.theme.ChronosTheme
import com.arnabcloud.chronos.ui.vault.AddTaskDialog
import com.arnabcloud.chronos.ui.vault.ChronosVaultScreen
import com.arnabcloud.chronos.viewmodel.ChronosViewModel
import kotlinx.coroutines.launch

// --- Navigation Routes ---
sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Vault : Screen("vault", "Tasks", Icons.Default.Inventory2)
    object Timeline : Screen("timeline", "Today", Icons.Default.Timeline)
    object Calendar : Screen("calendar", "Plan", Icons.Default.CalendarMonth)
}

val bottomNavItems = listOf(
    Screen.Vault,
    Screen.Timeline,
    Screen.Calendar
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ChronosTheme {
                MainNavigation()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainNavigation(viewModel: ChronosViewModel = viewModel()) {
    val pagerState = rememberPagerState(pageCount = { bottomNavItems.size })
    val coroutineScope = rememberCoroutineScope()

    var showSpeedDial by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var isEventCreation by remember { mutableStateOf(false) }

    if (showAddDialog) {
        AddTaskDialog(
            isEvent = isEventCreation,
            onDismiss = { showAddDialog = false },
            onConfirm = { newItem ->
                viewModel.addItem(newItem)
                showAddDialog = false
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
                                pagerState.animateScrollToPage(index)
                            }
                        },
                        label = { Text(screen.label) },
                        icon = { Icon(screen.icon, contentDescription = null) }
                    )
                }
            }
        },
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp),
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
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ExtendedFloatingActionButton(
                            onClick = {
                                showSpeedDial = false
                                isEventCreation = true
                                showAddDialog = true
                            },
                            icon = { Icon(Icons.Default.Event, contentDescription = null) },
                            text = { Text("Full Event") },
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        ExtendedFloatingActionButton(
                            onClick = {
                                showSpeedDial = false
                                isEventCreation = false
                                showAddDialog = true
                            },
                            icon = { Icon(Icons.Default.AddTask, contentDescription = null) },
                            text = { Text("Quick Task") },
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
                        if (showSpeedDial) Icons.Default.Close else Icons.Default.Add,
                        contentDescription = "New"
                    )
                }
            }
        }
    ) { innerPadding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.padding(innerPadding),
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
