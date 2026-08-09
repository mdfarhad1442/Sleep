package com.example.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.data.SleepSchedule
import com.example.ui.screens.AddEditScheduleDialog
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.ScheduleListScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.StatisticsScreen
import com.example.ui.viewmodel.SettingsViewModel
import com.example.ui.viewmodel.SleepViewModel
import com.example.ui.viewmodel.StatisticsViewModel

sealed class Screen(val route: String, val title: String, val icon: ImageVector?) {
    object Splash : Screen("splash", "Splash", null)
    object Onboarding : Screen("onboarding", "Onboarding", null)
    object Dashboard : Screen("dashboard", "Dashboard", Icons.Default.Home)
    object Schedules : Screen("schedules", "Schedules", Icons.Default.Bedtime)
    object Statistics : Screen("statistics", "Statistics", Icons.Default.BarChart)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
}

@Composable
fun MainAppNavigation(
    navController: NavHostController = rememberNavController(),
    sleepViewModel: SleepViewModel = viewModel(),
    settingsViewModel: SettingsViewModel = viewModel(),
    statisticsViewModel: StatisticsViewModel = viewModel()
) {
    val schedules by sleepViewModel.allSchedules.collectAsState()
    val onboardingCompleted by sleepViewModel.onboardingCompleted.collectAsState()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    var showAddEditDialog by remember { mutableStateOf(false) }
    var editingSchedule by remember { mutableStateOf<SleepSchedule?>(null) }

    val bottomNavItems = listOf(
        Screen.Dashboard,
        Screen.Schedules,
        Screen.Statistics,
        Screen.Settings
    )

    val showBottomBar = currentRoute in bottomNavItems.map { it.route }

    Scaffold(
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomBar,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                NavigationBar(
                    containerColor = Color(0xFF1E293B),
                    contentColor = Color.White
                ) {
                    bottomNavItems.forEach { screen ->
                        val selected = currentRoute == screen.route
                        NavigationBarItem(
                            icon = {
                                screen.icon?.let { icon ->
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = screen.title
                                    )
                                }
                            },
                            label = { Text(screen.title) },
                            selected = selected,
                            onClick = {
                                if (currentRoute != screen.route) {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.White,
                                unselectedIconColor = Color(0xFF94A3B8),
                                selectedTextColor = Color.White,
                                unselectedTextColor = Color(0xFF94A3B8),
                                indicatorColor = Color(0xFF6366F1)
                            ),
                            modifier = Modifier.testTag("nav_tab_${screen.route}")
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            NavHost(
                navController = navController,
                startDestination = Screen.Splash.route
            ) {
                composable(Screen.Splash.route) {
                    SplashScreen(
                        onSplashFinished = {
                            if (onboardingCompleted) {
                                navController.navigate(Screen.Dashboard.route) {
                                    popUpTo(Screen.Splash.route) { inclusive = true }
                                }
                            } else {
                                navController.navigate(Screen.Onboarding.route) {
                                    popUpTo(Screen.Splash.route) { inclusive = true }
                                }
                            }
                        }
                    )
                }

                composable(Screen.Onboarding.route) {
                    OnboardingScreen(
                        onFinishOnboarding = {
                            sleepViewModel.setOnboardingCompleted(true)
                            navController.navigate(Screen.Dashboard.route) {
                                popUpTo(Screen.Onboarding.route) { inclusive = true }
                            }
                        }
                    )
                }

                composable(Screen.Dashboard.route) {
                    DashboardScreen(
                        viewModel = sleepViewModel,
                        schedules = schedules,
                        onNavigateToSchedules = {
                            navController.navigate(Screen.Schedules.route)
                        },
                        onNavigateToSettings = {
                            navController.navigate(Screen.Settings.route)
                        },
                        onAddScheduleRequested = {
                            editingSchedule = null
                            showAddEditDialog = true
                        }
                    )
                }

                composable(Screen.Schedules.route) {
                    ScheduleListScreen(
                        viewModel = sleepViewModel,
                        schedules = schedules,
                        onAddScheduleRequested = {
                            editingSchedule = null
                            showAddEditDialog = true
                        },
                        onEditScheduleRequested = { schedule ->
                            editingSchedule = schedule
                            showAddEditDialog = true
                        }
                    )
                }

                composable(Screen.Statistics.route) {
                    StatisticsScreen(viewModel = statisticsViewModel)
                }

                composable(Screen.Settings.route) {
                    SettingsScreen(viewModel = settingsViewModel)
                }
            }
        }

        if (showAddEditDialog) {
            AddEditScheduleDialog(
                initialSchedule = editingSchedule,
                onDismiss = {
                    showAddEditDialog = false
                    editingSchedule = null
                },
                onSave = { title, startTime, endTime, repeatDays, strictMode ->
                    val scheduleToEdit = editingSchedule
                    if (scheduleToEdit == null) {
                        sleepViewModel.addSchedule(title, startTime, endTime, repeatDays, strictMode)
                    } else {
                        val updated = scheduleToEdit.copy(
                            title = title,
                            startTime = startTime,
                            endTime = endTime,
                            repeatDays = repeatDays,
                            strictModeEnabled = strictMode
                        )
                        sleepViewModel.updateSchedule(updated)
                    }
                    showAddEditDialog = false
                    editingSchedule = null
                }
            )
        }
    }
}
