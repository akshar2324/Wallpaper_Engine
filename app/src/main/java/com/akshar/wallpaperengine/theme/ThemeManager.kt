package com.akshar.wallpaperengine.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

val LocalThemeColors = staticCompositionLocalOf { ThemePalette.Abyss }

@Composable
fun WallpaperEngineTheme(
    themeId: String = AppThemeId.ABYSS.id,
    content: @Composable () -> Unit
) {
    val themeColors = ThemePalette.getThemeColors(themeId)

    val colorScheme = if (themeColors.isDark) {
        darkColorScheme(
            background = themeColors.background,
            surface = themeColors.surface,
            surfaceVariant = themeColors.surfaceVariant,
            primary = themeColors.primary,
            onPrimary = themeColors.onPrimary,
            primaryContainer = themeColors.primaryContainer,
            onPrimaryContainer = themeColors.onPrimaryContainer,
            secondary = themeColors.secondary,
            tertiary = themeColors.tertiary,
            onBackground = themeColors.textPrimary,
            onSurface = themeColors.textPrimary,
            onSurfaceVariant = themeColors.textSecondary
        )
    } else {
        lightColorScheme(
            background = themeColors.background,
            surface = themeColors.surface,
            surfaceVariant = themeColors.surfaceVariant,
            primary = themeColors.primary,
            onPrimary = themeColors.onPrimary,
            primaryContainer = themeColors.primaryContainer,
            onPrimaryContainer = themeColors.onPrimaryContainer,
            secondary = themeColors.secondary,
            tertiary = themeColors.tertiary,
            onBackground = themeColors.textPrimary,
            onSurface = themeColors.textPrimary,
            onSurfaceVariant = themeColors.textSecondary
        )
    }

    CompositionLocalProvider(LocalThemeColors provides themeColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = appTypography,
            content = content
        )
    }
}
