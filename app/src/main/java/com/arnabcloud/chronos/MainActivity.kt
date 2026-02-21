package com.arnabcloud.chronos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.arnabcloud.chronos.ui.calendar.ChronosCalendarScreen
import com.arnabcloud.chronos.ui.home.ChronosHomeScreen
import com.arnabcloud.chronos.ui.vault.ChronosVaultScreen
import com.arnabcloud.chronos.ui.theme.ChronosTheme

// --- Navigation Routes ---
sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Timeline : Screen("timeline", "Today", Icons.Default.Timeline)
    object Calendar : Screen("calendar", "Plan", Icons.Default.CalendarMonth)
    object Vault : Screen("vault", "Vault", Icons.Default.Inventory2)
}

val bottomNavItems = listOf(
    Screen.Timeline,
    Screen.Calendar,
    Screen.Vault
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

@Composable
fun MainNavigation() {
    val navController = rememberNavController()
    var showSpeedDial by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                bottomNavItems.forEach { screen ->
                    NavigationBarItem(
                        selected = currentDestination?.route == screen.route,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
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
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                // Speed Dial Menu
                AnimatedVisibility(
                    visible = showSpeedDial,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ExtendedFloatingActionButton(
                            onClick = { showSpeedDial = false },
                            icon = { Icon(Icons.Default.Event, contentDescription = null) },
                            text = { Text("Full Event") },
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        ExtendedFloatingActionButton(
                            onClick = { showSpeedDial = false },
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
        NavHost(
            navController = navController,
            startDestination = Screen.Timeline.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Timeline.route) { ChronosHomeScreen() }
            composable(Screen.Calendar.route) { ChronosCalendarScreen() }
            composable(Screen.Vault.route) { ChronosVaultScreen() }
        }
    }
}
