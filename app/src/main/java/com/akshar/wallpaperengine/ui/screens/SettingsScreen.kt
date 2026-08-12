package com.akshar.wallpaperengine.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.akshar.wallpaperengine.shader.ShaderIntensity
import com.akshar.wallpaperengine.shader.ShaderStyle
import com.akshar.wallpaperengine.theme.AppThemeId
import com.akshar.wallpaperengine.theme.LocalThemeColors
import com.akshar.wallpaperengine.ui.components.ThemePreviewCard
import com.akshar.wallpaperengine.ui.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    val theme = LocalThemeColors.current
    val prefs by viewModel.preferences.collectAsState()
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Text(
            text = "Engine Settings",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold, color = theme.textPrimary)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Section 1: APPEARANCE
        Text("APPEARANCE & SHADERS", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = theme.primary, letterSpacing = 1.sp))
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = theme.surfaceVariant),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Enable GPU Shaders", style = MaterialTheme.typography.bodyLarge.copy(color = theme.textPrimary, fontWeight = FontWeight.SemiBold))
                    Switch(
                        checked = prefs.shaderEnabled,
                        onCheckedChange = { viewModel.toggleShader(it) },
                        modifier = Modifier.testTag("shader_toggle_switch")
                    )
                }

                if (prefs.shaderEnabled) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Shader Style", style = MaterialTheme.typography.labelMedium.copy(color = theme.textSecondary))
                    Spacer(modifier = Modifier.height(6.dp))

                    ShaderStyle.values().forEach { style ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = prefs.shaderStyle == style.id,
                                onClick = { viewModel.selectShaderStyle(style.id) }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(style.displayName, style = MaterialTheme.typography.bodyMedium.copy(color = theme.textPrimary, fontWeight = FontWeight.Bold))
                                Text(style.description, style = MaterialTheme.typography.bodySmall.copy(color = theme.textSecondary))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Shader Intensity", style = MaterialTheme.typography.labelMedium.copy(color = theme.textSecondary))
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ShaderIntensity.values().forEach { intensity ->
                            FilterChip(
                                selected = prefs.shaderIntensity == intensity.id,
                                onClick = { viewModel.selectShaderIntensity(intensity.id) },
                                label = { Text(intensity.name) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Reduce Motion", style = MaterialTheme.typography.bodyLarge.copy(color = theme.textPrimary, fontWeight = FontWeight.SemiBold))
                    Switch(
                        checked = prefs.reduceMotion,
                        onCheckedChange = { viewModel.toggleReduceMotion(it) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Section 2: THEME PALETTES
        Text("THEME PALETTES", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = theme.primary, letterSpacing = 1.sp))
        Spacer(modifier = Modifier.height(8.dp))

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            AppThemeId.values().forEach { appTheme ->
                ThemePreviewCard(
                    themeId = appTheme,
                    isSelected = prefs.themeId == appTheme.id,
                    onSelect = { viewModel.selectTheme(appTheme.id) },
                    modifier = Modifier.testTag("theme_card_${appTheme.id}")
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Section 3: PERFORMANCE
        Text("PERFORMANCE PROFILE", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = theme.primary, letterSpacing = 1.sp))
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("BALANCED", "PERFORMANCE", "QUALITY").forEach { mode ->
                FilterChip(
                    selected = prefs.performanceMode == mode,
                    onClick = { viewModel.selectPerformanceMode(mode) },
                    label = { Text(mode) },
                    modifier = Modifier.weight(1f),
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = theme.primary)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Section 4: ABOUT
        Text("ABOUT ENGINE", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = theme.primary, letterSpacing = 1.sp))
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = theme.surfaceVariant),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Wallpaper Engine v1.0.0", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = theme.textPrimary))
                Text("High-performance OLED wallpaper rotation engine built with Jetpack Compose, Room & WorkManager.", style = MaterialTheme.typography.bodySmall.copy(color = theme.textSecondary))
            }
        }
    }
}
