package com.akshar.wallpaperengine.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val title: String,
    val iconSelected: ImageVector,
    val iconUnselected: ImageVector
) {
    object Onboarding : Screen("onboarding", "Welcome", Icons.Filled.Star, Icons.Outlined.Star)
    object Home : Screen("home", "Home", Icons.Filled.Home, Icons.Outlined.Home)
    object Library : Screen("library", "Library", Icons.Filled.PhotoLibrary, Icons.Outlined.PhotoLibrary)
    object Collections : Screen("collections", "Collections", Icons.Filled.FolderSpecial, Icons.Outlined.FolderSpecial)
    object Schedules : Screen("schedules", "Rotation", Icons.Filled.Schedule, Icons.Outlined.Schedule)
    object Explore : Screen("explore", "Explore", Icons.Filled.Explore, Icons.Outlined.Explore)
    object History : Screen("history", "History", Icons.Filled.History, Icons.Outlined.History)
    object Settings : Screen("settings", "Settings", Icons.Filled.Settings, Icons.Outlined.Settings)
    object ShaderStudio : Screen("shader_studio", "Shader Studio", Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome)
    object Detail : Screen("detail/{wallpaperId}", "Wallpaper", Icons.Filled.Image, Icons.Outlined.Image) {
        fun createRoute(wallpaperId: Long) = "detail/$wallpaperId"
    }
}

// Updated bottom navigation
val bottomNavScreens = listOf(
    Screen.Home,
    Screen.Library,
    Screen.Explore,
    Screen.Schedules,
    Screen.Settings
)
