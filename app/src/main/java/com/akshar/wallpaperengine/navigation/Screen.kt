package com.akshar.wallpaperengine.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val iconSelected: ImageVector, val iconUnselected: ImageVector) {
    object Home : Screen("home", "Dashboard", Icons.Filled.Home, Icons.Outlined.Home)
    object Library : Screen("library", "Library", Icons.Filled.GridView, Icons.Outlined.GridView)
    object Collections : Screen("collections", "Collections", Icons.Filled.FolderSpecial, Icons.Outlined.FolderSpecial)
    object Schedules : Screen("schedules", "Schedules", Icons.Filled.Schedule, Icons.Outlined.Schedule)
    object History : Screen("history", "History", Icons.Filled.History, Icons.Outlined.History)
    object Settings : Screen("settings", "Settings", Icons.Filled.Settings, Icons.Outlined.Settings)
    object Detail : Screen("detail/{wallpaperId}", "Detail", Icons.Filled.Image, Icons.Outlined.Image) {
        fun createRoute(wallpaperId: Long) = "detail/$wallpaperId"
    }
    object Onboarding : Screen("onboarding", "Welcome", Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome)
}

val bottomNavScreens = listOf(
    Screen.Home,
    Screen.Library,
    Screen.Collections,
    Screen.Schedules,
    Screen.Settings
)
