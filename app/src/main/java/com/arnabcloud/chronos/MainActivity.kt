package com.arnabcloud.chronos

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.arnabcloud.chronos.ui.components.AddTaskDialog
import com.arnabcloud.chronos.ui.screen.calender.ChronosCalendarScreen
import com.arnabcloud.chronos.ui.screen.home.ChronosTimelineScreen
import com.arnabcloud.chronos.ui.screen.settings.AboutScreen
import com.arnabcloud.chronos.ui.screen.settings.SettingsScreen
import com.arnabcloud.chronos.ui.screen.vault.ChronosVaultScreen
import com.arnabcloud.chronos.ui.theme.ChronosTheme
import com.arnabcloud.chronos.viewmodel.ChronosViewModel
import com.arnabcloud.chronos.viewmodel.SettingsViewModel
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

sealed class ActiveSettingsScreen {
    object Main : ActiveSettingsScreen()
    object About : ActiveSettingsScreen()
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settingsViewModel: SettingsViewModel = viewModel()
            val themePreference by settingsViewModel.theme.collectAsState()
            val compactMode by settingsViewModel.compactModeEnabled.collectAsState()
            val accentColor by settingsViewModel.accentColor.collectAsState()

            val darkTheme = when (themePreference) {
                "Light" -> false
                "Dark" -> true
                else -> isSystemInDarkTheme()
            }

            ChronosTheme(
                darkTheme = darkTheme,
                compactMode = compactMode,
                accentColor = accentColor
            ) {
                RequestPermissions()
                MainNavigation(settingsViewModel = settingsViewModel)
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
fun MainNavigation(
    chronosViewModel: ChronosViewModel = viewModel(),
    settingsViewModel: SettingsViewModel = viewModel()
) {
    val pagerState = rememberPagerState(pageCount = { bottomNavItems.size })
    val coroutineScope = rememberCoroutineScope()
    var showSpeedDial by remember { mutableStateOf(false) }
    var activeDialog by remember { mutableStateOf<DialogType?>(null) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    var activeSettingsScreen by remember { mutableStateOf<ActiveSettingsScreen?>(null) }
    val animationsEnabled by settingsViewModel.animationsEnabled.collectAsState()

    when (activeSettingsScreen) {
        ActiveSettingsScreen.Main -> {
            BackHandler { activeSettingsScreen = null }
            SettingsScreen(
                onBackClick = { activeSettingsScreen = null },
                viewModel = settingsViewModel
            )
            return
        }
        ActiveSettingsScreen.About -> {
            BackHandler { activeSettingsScreen = null }
            AboutScreen(onBackClick = { activeSettingsScreen = null })
            return
        }
        null -> { /* Continue to main UI */ }
    }

    activeDialog?.let { dialogType ->
        AddTaskDialog(
            isEvent = dialogType is DialogType.Event,
            onDismiss = { activeDialog = null },
            onConfirm = { newItem ->
                chronosViewModel.addItem(newItem)
                activeDialog = null
            }
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = animationsEnabled,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(300.dp)
            ) {
                Spacer(Modifier.height(12.dp))
                Text(
                    "Chronos",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.titleLarge
                )
                HorizontalDivider()
                NavigationDrawerItem(
                    label = { Text("Settings") },
                    selected = activeSettingsScreen is ActiveSettingsScreen.Main,
                    onClick = {
                        coroutineScope.launch {
                            if (animationsEnabled) drawerState.close() else drawerState.snapTo(DrawerValue.Closed)
                            activeSettingsScreen = ActiveSettingsScreen.Main
                        }
                    },
                    icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                NavigationDrawerItem(
                    label = { Text("About") },
                    selected = activeSettingsScreen is ActiveSettingsScreen.About,
                    onClick = {
                        coroutineScope.launch {
                            if (animationsEnabled) drawerState.close() else drawerState.snapTo(DrawerValue.Closed)
                            activeSettingsScreen = ActiveSettingsScreen.About
                        }
                    },
                    icon = { Icon(Icons.Default.Info, contentDescription = null) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(bottomNavItems[pagerState.currentPage].label) },
                    actions = {
                        IconButton(onClick = { 
                            coroutineScope.launch { 
                                if (animationsEnabled) drawerState.open() else drawerState.snapTo(DrawerValue.Open)
                            } 
                        }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    }
                )
            },
            bottomBar = {
                NavigationBar {
                    bottomNavItems.forEachIndexed { index, screen ->
                        NavigationBarItem(
                            selected = pagerState.currentPage == index,
                            onClick = {
                                coroutineScope.launch {
                                    if (animationsEnabled) {
                                        pagerState.animateScrollToPage(page = index)
                                    } else {
                                        pagerState.scrollToPage(index)
                                    }
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
                        enter = if (animationsEnabled) fadeIn() + expandVertically() else EnterTransition.None,
                        exit = if (animationsEnabled) fadeOut() + shrinkVertically() else ExitTransition.None
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
                userScrollEnabled = animationsEnabled
            ) { page ->
                when (bottomNavItems[page]) {
                    Screen.Vault -> ChronosVaultScreen(chronosViewModel)
                    Screen.Timeline -> ChronosTimelineScreen(chronosViewModel)
                    Screen.Calendar -> ChronosCalendarScreen(chronosViewModel)
                }
            }
        }
    }
}
