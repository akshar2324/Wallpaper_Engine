package com.akshar.wallpaperengine.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
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
    modifier: Modifier = Modifier,
    onNavigateToShaderStudio: () -> Unit = {}
) {
    val theme = LocalThemeColors.current
    val prefs by viewModel.preferences.collectAsState()
    val healthReport by viewModel.healthReport.collectAsState()
    val analytics by viewModel.analytics.collectAsState()
    val isScanning by viewModel.isScanning.collectAsState()

    var showBackupDialog by remember { mutableStateOf(false) }
    var backupJsonContent by remember { mutableStateOf("") }
    var restoreInputJson by remember { mutableStateOf("") }
    var showRestoreDialog by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Medium, color = theme.textPrimary),
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Section 1: LIBRARY HEALTH & STORAGE WIZARD
        SettingsSectionHeader("Library")

        Card(
            colors = CardDefaults.cardColors(containerColor = theme.surface),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, theme.borderGlow.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Storage Health", style = MaterialTheme.typography.bodyLarge.copy(color = theme.textPrimary, fontWeight = FontWeight.Bold))
                        val storageMb = healthReport?.let { String.format("%.1f MB", it.totalStorageBytes / (1024.0 * 1024.0)) } ?: "-- MB"
                        Text("Total: $storageMb • ${healthReport?.totalWallpapers ?: "--"} Wallpapers", style = MaterialTheme.typography.bodySmall.copy(color = theme.textSecondary))
                    }

                    Button(
                        onClick = { viewModel.scanLibraryHealth() },
                        enabled = !isScanning,
                        colors = ButtonDefaults.buttonColors(containerColor = theme.primary.copy(alpha = 0.2f), contentColor = theme.primary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(if (isScanning) "SCANNING..." else "SCAN")
                    }
                }

                if (healthReport != null) {
                    val report = healthReport!!
                    HorizontalDivider(color = theme.borderGlow.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Broken URIs: ${report.brokenWallpapers.size}", style = MaterialTheme.typography.bodyMedium.copy(color = if (report.brokenWallpapers.isNotEmpty()) Color(0xFFFF5252) else theme.textPrimary))
                            Text("Missing/unreadable files", style = MaterialTheme.typography.bodySmall.copy(color = theme.textSecondary))
                        }
                        if (report.brokenWallpapers.isNotEmpty()) {
                            OutlinedButton(
                                onClick = {
                                    viewModel.cleanBrokenWallpapers { count ->
                                        statusMessage = "Purged $count broken wallpapers"
                                    }
                                },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("CLEAN")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Duplicate Clusters: ${report.duplicateClusters.size}", style = MaterialTheme.typography.bodyMedium.copy(color = theme.textPrimary))
                            Text("Identical/visually matching images", style = MaterialTheme.typography.bodySmall.copy(color = theme.textSecondary))
                        }
                        if (report.duplicateClusters.isNotEmpty()) {
                            OutlinedButton(
                                onClick = {
                                    viewModel.cleanDuplicates { count ->
                                        statusMessage = "Deduplicated $count wallpapers"
                                    }
                                },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("RESOLVE")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Unused (>30d): ${report.unusedWallpapers.size}", style = MaterialTheme.typography.bodyMedium.copy(color = theme.textPrimary))
                            Text("Non-favorite stale wallpapers", style = MaterialTheme.typography.bodySmall.copy(color = theme.textSecondary))
                        }
                        if (report.unusedWallpapers.isNotEmpty()) {
                            OutlinedButton(
                                onClick = {
                                    viewModel.cleanUnusedWallpapers { count ->
                                        statusMessage = "Purged $count unused wallpapers"
                                    }
                                },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("CLEAN")
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Section 2: ENGINE ANALYTICS & INSIGHTS
        SettingsSectionHeader("Wallpaper")

        Card(
            colors = CardDefaults.cardColors(containerColor = theme.surface),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, theme.borderGlow.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                if (analytics != null) {
                    val stats = analytics!!
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Total Rotations", style = MaterialTheme.typography.labelSmall.copy(color = theme.textSecondary))
                            Text("${stats.totalRotations}", style = MaterialTheme.typography.titleLarge.copy(color = theme.primary, fontWeight = FontWeight.Bold))
                        }
                        Column {
                            Text("OLED Dark Ratio", style = MaterialTheme.typography.labelSmall.copy(color = theme.textSecondary))
                            Text("${stats.darkOledPercentage}%", style = MaterialTheme.typography.titleLarge.copy(color = theme.textPrimary, fontWeight = FontWeight.Bold))
                        }
                        Column {
                            Text("Avg Rating", style = MaterialTheme.typography.labelSmall.copy(color = theme.textSecondary))
                            Text(String.format("%.1f ★", stats.averageRating), style = MaterialTheme.typography.titleLarge.copy(color = Color(0xFFFFD700), fontWeight = FontWeight.Bold))
                        }
                    }

                    HorizontalDivider(color = theme.borderGlow.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 12.dp))

                    Text("Rotation Source Breakdown", style = MaterialTheme.typography.labelSmall.copy(color = theme.textSecondary, letterSpacing = 1.sp))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("• Scheduled: ${stats.scheduledRotations}  • Context: ${stats.contextTriggerRotations}  • Manual: ${stats.manualRotations}", style = MaterialTheme.typography.bodySmall.copy(color = theme.textPrimary))

                    if (stats.topStyles.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Top Styles: " + stats.topStyles.joinToString(", ") { "${it.first} (${it.second})" }, style = MaterialTheme.typography.bodySmall.copy(color = theme.textSecondary))
                    }
                } else {
                    Text("No usage analytics recorded yet.", style = MaterialTheme.typography.bodySmall.copy(color = theme.textSecondary))
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Section 3: BACKUP & RESTORE
        SettingsSectionHeader("Backup")

        Card(
            colors = CardDefaults.cardColors(containerColor = theme.surface),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, theme.borderGlow.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Export or restore your full wallpaper library, collections, tags, schedules, and history.", style = MaterialTheme.typography.bodySmall.copy(color = theme.textSecondary))
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = {
                            viewModel.exportBackup { json ->
                                backupJsonContent = json
                                showBackupDialog = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = theme.primary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("EXPORT JSON")
                    }

                    OutlinedButton(
                        onClick = { showRestoreDialog = true },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("RESTORE JSON")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Section 4: THEME PALETTES
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

        // Section 5: APPEARANCE & SHADERS
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
                    HorizontalDivider(color = theme.borderGlow.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 12.dp))

                    Text("SHADER STYLE", style = MaterialTheme.typography.labelSmall.copy(color = theme.textSecondary, letterSpacing = 1.sp))
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
                    Text("SHADER INTENSITY", style = MaterialTheme.typography.labelSmall.copy(color = theme.textSecondary, letterSpacing = 1.sp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ShaderIntensity.values().forEach { intensity ->
                            val isSelected = prefs.shaderIntensity == intensity.id
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.selectShaderIntensity(intensity.id) },
                                label = { Text(intensity.name) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = theme.primary.copy(alpha = 0.2f),
                                    selectedLabelColor = theme.primary
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    borderColor = theme.borderGlow.copy(alpha = 0.2f),
                                    selectedBorderColor = theme.primary
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = onNavigateToShaderStudio,
                        colors = ButtonDefaults.buttonColors(containerColor = theme.primary.copy(alpha = 0.2f), contentColor = theme.primary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("LAUNCH AGSL SHADER STUDIO", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }

                HorizontalDivider(color = theme.borderGlow.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 12.dp))

                SettingsToggleRow(
                    label = "Reduce Motion",
                    checked = prefs.reduceMotion,
                    onCheckedChange = { viewModel.toggleReduceMotion(it) }
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Section 6: PERFORMANCE
        SettingsSectionHeader("PERFORMANCE PROFILE")

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("BALANCED", "PERFORMANCE", "QUALITY").forEach { mode ->
                val isSelected = prefs.performanceMode == mode
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.selectPerformanceMode(mode) },
                    label = { Text(mode) },
                    modifier = Modifier.weight(1f),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = theme.primary.copy(alpha = 0.2f),
                        selectedLabelColor = theme.primary
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = theme.borderGlow.copy(alpha = 0.2f),
                        selectedBorderColor = theme.primary
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Section 7: ABOUT
        SettingsSectionHeader("ABOUT ENGINE")

        Card(
            colors = CardDefaults.cardColors(containerColor = theme.background),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, theme.borderGlow.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("AURA v1.0.0", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold, color = theme.textPrimary))
                Spacer(modifier = Modifier.height(4.dp))
                Text("High-performance OLED wallpaper rotation engine built with Jetpack Compose, Room & WorkManager.", style = MaterialTheme.typography.bodySmall.copy(color = theme.textSecondary))
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }

    // Export Dialog
    if (showBackupDialog) {
        AlertDialog(
            onDismissRequest = { showBackupDialog = false },
            title = { Text("Backup JSON Export", color = theme.textPrimary) },
            text = {
                Column {
                    Text("Successfully generated JSON backup payload (${backupJsonContent.length} bytes).", style = MaterialTheme.typography.bodySmall.copy(color = theme.textSecondary))
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = backupJsonContent.take(500) + if (backupJsonContent.length > 500) "\n...[truncated]" else "",
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth().height(150.dp)
                    )
                }
            },
            confirmButton = {
                Button(onClick = { showBackupDialog = false }) {
                    Text("CLOSE")
                }
            }
        )
    }

    // Restore Dialog
    if (showRestoreDialog) {
        AlertDialog(
            onDismissRequest = { showRestoreDialog = false },
            title = { Text("Restore JSON Backup", color = theme.textPrimary) },
            text = {
                Column {
                    Text("Paste your backup JSON payload below to import wallpapers, collections, and schedules.", style = MaterialTheme.typography.bodySmall.copy(color = theme.textSecondary))
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = restoreInputJson,
                        onValueChange = { restoreInputJson = it },
                        placeholder = { Text("{\n  \"wallpapers\": [...]\n}") },
                        modifier = Modifier.fillMaxWidth().height(150.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (restoreInputJson.isNotBlank()) {
                            viewModel.restoreBackup(restoreInputJson) { summary ->
                                if (summary != null) {
                                    statusMessage = "Restored ${summary.wallpaperCount} wallpapers, ${summary.collectionCount} collections"
                                } else {
                                    statusMessage = "Failed to restore backup: Invalid JSON"
                                }
                                showRestoreDialog = false
                            }
                        }
                    }
                ) {
                    Text("RESTORE")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreDialog = false }) {
                    Text("CANCEL")
                }
            }
        )
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    val theme = LocalThemeColors.current
    Text(
        text = title,
        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = theme.textSecondary, letterSpacing = 1.sp),
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
