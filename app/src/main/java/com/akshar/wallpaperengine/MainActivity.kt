package com.akshar.wallpaperengine

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.akshar.wallpaperengine.navigation.Screen
import com.akshar.wallpaperengine.navigation.bottomNavScreens
import com.akshar.wallpaperengine.shader.ShaderBackground
import com.akshar.wallpaperengine.theme.LocalThemeColors
import com.akshar.wallpaperengine.theme.WallpaperEngineTheme
import com.akshar.wallpaperengine.ui.screens.*
import com.akshar.wallpaperengine.ui.viewmodel.*

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val app = application as WallpaperEngineApplication

        setContent {
            val prefs by app.userPreferencesRepository.userPreferencesFlow
                .collectAsState(initial = com.akshar.wallpaperengine.data.preferences.UserPreferences())

            WallpaperEngineTheme(themeId = prefs.themeId) {
                ShaderBackground(
                    shaderStyle = prefs.shaderStyle,
                    isEnabled = prefs.shaderEnabled,
                    intensity = prefs.shaderIntensity,
                    reduceMotion = prefs.reduceMotion
                ) {
                    WallpaperEngineApp(
                        app = app,
                        isOnboardingCompleted = prefs.onboardingCompleted
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WallpaperEngineApp(
    app: WallpaperEngineApplication,
    isOnboardingCompleted: Boolean
) {
    val theme = LocalThemeColors.current
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val startDestination = if (isOnboardingCompleted) Screen.Home.route else Screen.Onboarding.route

    val settingsViewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModel.Factory(
            userPreferencesRepository = app.userPreferencesRepository,
            healthManager = app.libraryHealthManager,
            analyticsManager = app.analyticsManager,
            backupRestoreEngine = app.backupRestoreEngine
        )
    )

    Scaffold(
        bottomBar = {
            if (currentRoute in bottomNavScreens.map { it.route }) {
                NavigationBar(
                    containerColor = theme.surface,
                    contentColor = theme.primary,
                    tonalElevation = 0.dp,
                    modifier = Modifier
                        .border(
                            width = 1.dp,
                            color = Color.White.copy(alpha = 0.08f),
                            shape = androidx.compose.ui.graphics.RectangleShape
                        )
                        .testTag("bottom_navigation_bar")
                ) {
                    bottomNavScreens.forEach { screen ->
                        val selected = currentRoute == screen.route
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    imageVector = if (selected) screen.iconSelected else screen.iconUnselected,
                                    contentDescription = screen.title
                                )
                            },
                            label = {
                                Text(
                                    text = screen.title,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                    ),
                                    maxLines = 1,
                                    softWrap = false,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                            },
                            selected = selected,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = theme.primary,
                                selectedTextColor = theme.primary,
                                unselectedIconColor = theme.textSecondary,
                                unselectedTextColor = theme.textSecondary,
                                indicatorColor = theme.primary.copy(alpha = 0.15f)
                            ),
                            modifier = Modifier.testTag("nav_item_${screen.route}")
                        )
                    }
                }
            }
        },
        containerColor = androidx.compose.ui.graphics.Color.Transparent
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Onboarding.route) {
                OnboardingScreen(
                    onComplete = {
                        settingsViewModel.setOnboardingCompleted(true)
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Home.route) {
                val homeViewModel: HomeViewModel = viewModel(
                    factory = HomeViewModel.Factory(
                        app.wallpaperRepository,
                        app.scheduleRepository,
                        app.wallpaperService
                    )
                )
                HomeScreen(
                    viewModel = homeViewModel,
                    onNavigateToLibrary = { navController.navigate(Screen.Library.route) },
                    onNavigateToDetail = { id -> navController.navigate(Screen.Detail.createRoute(id)) },
                    onNavigateToSchedules = { navController.navigate(Screen.Schedules.route) }
                )
            }

            composable(Screen.Library.route) {
                val libraryViewModel: LibraryViewModel = viewModel(
                    factory = LibraryViewModel.Factory(
                        app.wallpaperRepository,
                        app.collectionRepository,
                        app.tagRepository
                    )
                )
                LibraryScreen(
                    viewModel = libraryViewModel,
                    onNavigateToDetail = { id -> navController.navigate(Screen.Detail.createRoute(id)) }
                )
            }

            composable(Screen.Explore.route) {
                val exploreViewModel: com.akshar.wallpaperengine.ui.viewmodel.ExploreViewModel = viewModel(
                    factory = com.akshar.wallpaperengine.ui.viewmodel.ExploreViewModel.Factory(
                        app.remoteWallpaperManager
                    )
                )
                com.akshar.wallpaperengine.ui.screens.ExploreScreen(viewModel = exploreViewModel)
            }

            composable(Screen.Collections.route) {
                val collectionsViewModel: CollectionsViewModel = viewModel(
                    factory = CollectionsViewModel.Factory(
                        app.collectionRepository,
                        app.wallpaperRepository
                    )
                )
                CollectionsScreen(
                    viewModel = collectionsViewModel,
                    onNavigateToDetail = { id -> navController.navigate(Screen.Detail.createRoute(id)) }
                )
            }

            composable(Screen.Schedules.route) {
                val schedulesViewModel: SchedulesViewModel = viewModel(
                    factory = SchedulesViewModel.Factory(
                        app.scheduleRepository,
                        app.collectionRepository,
                        app.wallpaperScheduler
                    )
                )
                SchedulesScreen(viewModel = schedulesViewModel)
            }

            composable(Screen.History.route) {
                val historyViewModel: HistoryViewModel = viewModel(
                    factory = HistoryViewModel.Factory(
                        app.historyRepository,
                        app.wallpaperRepository,
                        app.wallpaperService
                    )
                )
                HistoryScreen(
                    viewModel = historyViewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    viewModel = settingsViewModel,
                    onNavigateToShaderStudio = { navController.navigate(Screen.ShaderStudio.route) }
                )
            }

            composable(Screen.ShaderStudio.route) {
                com.akshar.wallpaperengine.ui.screens.ShaderStudioScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.Detail.route,
                arguments = listOf(navArgument("wallpaperId") { type = NavType.LongType })
            ) { backStackEntry ->
                val wallpaperId = backStackEntry.arguments?.getLong("wallpaperId") ?: 0L
                val detailViewModel: WallpaperDetailViewModel = viewModel(
                    factory = WallpaperDetailViewModel.Factory(
                        wallpaperId,
                        app.wallpaperRepository,
                        app.collectionRepository,
                        app.tagRepository,
                        app.historyRepository,
                        app.wallpaperService
                    )
                )
                WallpaperDetailScreen(
                    viewModel = detailViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
