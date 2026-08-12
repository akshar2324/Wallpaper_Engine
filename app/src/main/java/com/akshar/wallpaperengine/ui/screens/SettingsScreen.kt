package com.akshar.wallpaperengine.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
    val context = LocalContext.current

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json"),
        onResult = { uri: Uri? ->
            if (uri != null) {
                viewModel.exportBackup(context, uri) { success ->
                    Toast.makeText(context, if (success) "Backup Exported" else "Export Failed", Toast.LENGTH_SHORT).show()
                }
            }
        }
    )

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri: Uri? ->
            if (uri != null) {
                viewModel.importBackup(context, uri) { success ->
                    Toast.makeText(context, if (success) "Backup Imported" else "Import Failed", Toast.LENGTH_SHORT).show()
                }
            }
        }
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Text(
            text = "SETTINGS",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = theme.textPrimary, letterSpacing = 1.dp),
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Section 1: THEME PALETTES
        SettingsSectionHeader("THEME PALETTES")

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

        Spacer(modifier = Modifier.height(32.dp))

        // Section 2: APPEARANCE & SHADERS
        SettingsSectionHeader("APPEARANCE & SHADERS")

        Card(
            colors = CardDefaults.cardColors(containerColor = theme.background),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, theme.borderGlow.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                SettingsToggleRow(
                    label = "Enable GPU Shaders",
                    checked = prefs.shaderEnabled,
                    onCheckedChange = { viewModel.toggleShader(it) },
                    testTag = "shader_toggle_switch"
                )

                if (prefs.shaderEnabled) {
                    Divider(color = theme.borderGlow.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 12.dp))

                    Text("SHADER STYLE", style = MaterialTheme.typography.labelSmall.copy(color = theme.textSecondary, letterSpacing = 1.dp))
                    Spacer(modifier = Modifier.height(8.dp))

                    ShaderStyle.values().forEach { style ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = prefs.shaderStyle == style.id,
                                onClick = { viewModel.selectShaderStyle(style.id) },
                                colors = RadioButtonDefaults.colors(selectedColor = theme.primary, unselectedColor = theme.textSecondary)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(style.displayName, style = MaterialTheme.typography.bodyMedium.copy(color = theme.textPrimary, fontWeight = FontWeight.Bold))
                                Text(style.description, style = MaterialTheme.typography.bodySmall.copy(color = theme.textSecondary))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text("SHADER INTENSITY", style = MaterialTheme.typography.labelSmall.copy(color = theme.textSecondary, letterSpacing = 1.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ShaderIntensity.values().forEach { intensity ->
                            FilterChip(
                                selected = prefs.shaderIntensity == intensity.id,
                                onClick = { viewModel.selectShaderIntensity(intensity.id) },
                                label = { Text(intensity.name) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = theme.primary.copy(alpha = 0.2f),
                                    selectedLabelColor = theme.primary
                                ),
                                border = FilterChipDefaults.filterChipBorder(borderColor = theme.borderGlow.copy(alpha = 0.2f))
                            )
                        }
                    }
                }

                Divider(color = theme.borderGlow.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 12.dp))

                SettingsToggleRow(
                    label = "Reduce Motion",
                    checked = prefs.reduceMotion,
                    onCheckedChange = { viewModel.toggleReduceMotion(it) }
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Section 3: PERFORMANCE
        SettingsSectionHeader("PERFORMANCE PROFILE")

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
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = theme.primary.copy(alpha = 0.2f),
                        selectedLabelColor = theme.primary
                    ),
                    border = FilterChipDefaults.filterChipBorder(borderColor = theme.borderGlow.copy(alpha = 0.2f))
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Section 4: DATA & BACKUP
        SettingsSectionHeader("BACKUP & RESTORE")

        Card(
            colors = CardDefaults.cardColors(containerColor = theme.background),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, theme.borderGlow.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Metadata Backup", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold, color = theme.textPrimary))
                Text("Export or import your collections, playlists, and schedules.", style = MaterialTheme.typography.bodySmall.copy(color = theme.textSecondary))

                Spacer(modifier = Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { exportLauncher.launch("wallpaper_engine_backup.json") },
                        colors = ButtonDefaults.buttonColors(containerColor = theme.primary),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("EXPORT")
                    }
                    Button(
                        onClick = { importLauncher.launch(arrayOf("application/json")) },
                        colors = ButtonDefaults.buttonColors(containerColor = theme.surfaceVariant, contentColor = theme.textPrimary),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("IMPORT")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Section 5: ABOUT
        SettingsSectionHeader("ABOUT ENGINE")

        Card(
            colors = CardDefaults.cardColors(containerColor = theme.background),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, theme.borderGlow.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Wallpaper Engine v1.0.0", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold, color = theme.textPrimary))
                Spacer(modifier = Modifier.height(4.dp))
                Text("High-performance OLED wallpaper rotation engine built with Jetpack Compose, Room & WorkManager.", style = MaterialTheme.typography.bodySmall.copy(color = theme.textSecondary))
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    val theme = LocalThemeColors.current
    Text(
        text = title,
        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = theme.textSecondary, letterSpacing = 1.dp),
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
private fun SettingsToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String = ""
) {
    val theme = LocalThemeColors.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge.copy(color = theme.textPrimary, fontWeight = FontWeight.Medium))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.testTag(testTag),
            colors = SwitchDefaults.colors(
                checkedThumbColor = theme.onPrimary,
                checkedTrackColor = theme.primary,
                uncheckedThumbColor = theme.textSecondary,
                uncheckedTrackColor = theme.surface
            )
        )
    }
}
