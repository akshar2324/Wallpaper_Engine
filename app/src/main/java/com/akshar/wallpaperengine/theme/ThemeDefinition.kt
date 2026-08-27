package com.akshar.wallpaperengine.theme

import androidx.compose.ui.graphics.Color

enum class AppThemeId(val id: String, val displayName: String, val description: String) {
    ABYSS("abyss", "Aura", "OLED black with restrained electric lime accents"),
    NEON_VIOLET("neon_violet", "Neon Violet", "Energetic futuristic neon with vivid violet & magenta highlights"),
    MIDNIGHT("midnight", "Midnight", "Ultra-dark, sleek, professional OLED black with subtle accents"),
    CRIMSON_NIGHT("crimson_night", "Crimson Night", "Cyberpunk villain aesthetic with deep crimson & obsidian tones"),
    MOONLIGHT("moonlight", "Moonlight", "Cool indigo atmosphere with soft lunar violet lighting"),
    LIGHT("light", "Light Day", "Clean light theme with vibrant purple accents & soft lavender surfaces")
}

data class ThemeColors(
    val isDark: Boolean,
    val background: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val primary: Color,
    val onPrimary: Color,
    val primaryContainer: Color,
    val onPrimaryContainer: Color,
    val secondary: Color,
    val tertiary: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val borderGlow: Color,
    val cardBackground: Color
)

object ThemePalette {
    val Abyss = ThemeColors(
        isDark = true,
        background = AppColors.BackgroundPrimary,
        surface = AppColors.SurfaceSecondary,
        surfaceVariant = AppColors.SurfaceElevated,
        primary = AppColors.ElectricLime,
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = AppColors.PrimaryContainer,
        onPrimaryContainer = Color(0xFFF3E8FF),
        secondary = AppColors.MutedLime,
        tertiary = Color(0xFF8FA6B8),
        textPrimary = AppColors.TextPrimary,
        textSecondary = AppColors.TextSecondary,
        borderGlow = AppColors.BorderSubtle,
        cardBackground = AppColors.SurfaceElevated
    )

    val NeonViolet = ThemeColors(
        isDark = true,
        background = Color(0xFF06020A),
        surface = Color(0xFF0F0A18),
        surfaceVariant = Color(0xFF161022),
        primary = Color(0xFFC084FC),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFF3B0B5E),
        onPrimaryContainer = Color(0xFFFCE7F3),
        secondary = Color(0xFFF43F5E),
        tertiary = Color(0xFF06B6D4),
        textPrimary = Color(0xFFFAF5FF),
        textSecondary = Color(0xFF9CA3AF),
        borderGlow = Color(0x26C084FC),
        cardBackground = Color(0xFF120C1C)
    )

    val Midnight = ThemeColors(
        isDark = true,
        background = Color(0xFF000000),
        surface = Color(0xFF0D0D11),
        surfaceVariant = Color(0xFF14141A),
        primary = Color(0xFF8B5CF6),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFF1E1438),
        onPrimaryContainer = Color(0xFFDDD6FE),
        secondary = Color(0xFF9CA3AF),
        tertiary = Color(0xFF64748B),
        textPrimary = Color(0xFFF3F4F6),
        textSecondary = Color(0xFF9CA3AF),
        borderGlow = Color(0x1AFFFFFF),
        cardBackground = Color(0xFF111116)
    )

    val CrimsonNight = ThemeColors(
        isDark = true,
        background = Color(0xFF080204),
        surface = Color(0xFF12080C),
        surfaceVariant = Color(0xFF1A0F14),
        primary = Color(0xFFEF4444),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFF3D080C),
        onPrimaryContainer = Color(0xFFFEE2E2),
        secondary = Color(0xFFA855F7),
        tertiary = Color(0xFFF97316),
        textPrimary = Color(0xFFFFF1F2),
        textSecondary = Color(0xFF9CA3AF),
        borderGlow = Color(0x26EF4444),
        cardBackground = Color(0xFF150A0E)
    )

    val Moonlight = ThemeColors(
        isDark = true,
        background = Color(0xFF030712),
        surface = Color(0xFF0F172A),
        surfaceVariant = Color(0xFF1E293B),
        primary = Color(0xFF818CF8),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFF1E1C4E),
        onPrimaryContainer = Color(0xFFE0E7FF),
        secondary = Color(0xFF38BDF8),
        tertiary = Color(0xFFC084FC),
        textPrimary = Color(0xFFF8FAFC),
        textSecondary = Color(0xFF94A3B8),
        borderGlow = Color(0x26818CF8),
        cardBackground = Color(0xFF111A2E)
    )

    val Light = ThemeColors(
        isDark = false,
        background = Color(0xFFF8FAFC),
        surface = Color(0xFFFFFFFF),
        surfaceVariant = Color(0xFFF1F5F9),
        primary = Color(0xFF7C3AED),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFEDE9FE),
        onPrimaryContainer = Color(0xFF5B21B6),
        secondary = Color(0xFFD946EF),
        tertiary = Color(0xFF0284C7),
        textPrimary = Color(0xFF0F172A),
        textSecondary = Color(0xFF64748B),
        borderGlow = Color(0xFFE2E8F0),
        cardBackground = Color(0xFFFFFFFF)
    )

    fun getThemeColors(themeId: String): ThemeColors {
        return when (themeId) {
            AppThemeId.NEON_VIOLET.id -> NeonViolet
            AppThemeId.MIDNIGHT.id -> Midnight
            AppThemeId.CRIMSON_NIGHT.id -> CrimsonNight
            AppThemeId.MOONLIGHT.id -> Moonlight
            AppThemeId.LIGHT.id -> Light
            else -> Abyss
        }
    }
}
