package com.akshar.wallpaperengine.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.akshar.wallpaperengine.data.local.entity.ScheduleEntity
import com.akshar.wallpaperengine.theme.LocalThemeColors
import com.akshar.wallpaperengine.ui.components.EmptyStateView
import com.akshar.wallpaperengine.ui.viewmodel.SchedulesViewModel

@Composable
private fun RotationPreview(label: String, time: String, color: Color) {
    val theme = LocalThemeColors.current
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(width = 88.dp, height = 112.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(theme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        androidx.compose.ui.graphics.Brush.radialGradient(
                            colors = listOf(color.copy(alpha = 0.7f), Color.Transparent)
                        )
                    )
            )
        }
        Spacer(modifier = Modifier.height(7.dp))
        Text(label, style = MaterialTheme.typography.labelMedium.copy(color = theme.textPrimary))
        Text(time, style = MaterialTheme.typography.labelSmall.copy(color = theme.textSecondary))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SchedulesScreen(
    viewModel: SchedulesViewModel,
    modifier: Modifier = Modifier
) {
    val theme = LocalThemeColors.current
    val uiState by viewModel.uiState.collectAsState()
    var showEditDialog by remember { mutableStateOf(false) }
    var selectedScheduleForEdit by remember { mutableStateOf<ScheduleEntity?>(null) }
    var rotationMode by remember { mutableStateOf("Smart") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Rotation",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Medium, color = theme.textPrimary)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(modifier = Modifier.size(7.dp).background(theme.primary, RoundedCornerShape(50)))
            }

            IconButton(onClick = {
                selectedScheduleForEdit = ScheduleEntity(name = "Daily Rotation", timeHour = 8, timeMinute = 0)
                showEditDialog = true
            }) {
                Icon(Icons.Filled.Add, contentDescription = "New Schedule", tint = theme.primary)
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(theme.surface, RoundedCornerShape(12.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            listOf("Smart", "Shuffle", "Schedule").forEach { mode ->
                val selected = rotationMode == mode
                Text(
                    text = mode,
                    modifier = Modifier
                        .weight(1f)
                        .background(if (selected) theme.surfaceVariant else Color.Transparent, RoundedCornerShape(9.dp))
                        .clickable { rotationMode = mode }
                        .padding(vertical = 10.dp),
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = if (selected) theme.textPrimary else theme.textSecondary,
                        fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal
                    ),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            RotationPreview("Current", "Now", theme.primary)
            Text("42 min", style = MaterialTheme.typography.labelMedium.copy(color = theme.textSecondary))
            RotationPreview("Next", "22:30", theme.tertiary)
        }

        Spacer(modifier = Modifier.height(22.dp))
        Text("Your day", style = MaterialTheme.typography.titleSmall.copy(color = theme.textPrimary, fontWeight = FontWeight.Medium))
        Spacer(modifier = Modifier.height(10.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            listOf("Morning" to "07:00", "Focus" to "09:00", "Evening" to "18:00", "Night" to "22:30").forEach { (label, time) ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(modifier = Modifier.size(10.dp).background(if (label == "Night") theme.primary else theme.surfaceVariant, RoundedCornerShape(50)))
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(label, style = MaterialTheme.typography.labelSmall.copy(color = theme.textSecondary))
                    Text(time, style = MaterialTheme.typography.labelSmall.copy(color = theme.textSecondary.copy(alpha = 0.65f)))
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (uiState.schedules.isNotEmpty()) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(uiState.schedules, key = { it.id }) { schedule ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                        .border(1.dp, theme.borderGlow.copy(alpha = 0.12f), RoundedCornerShape(12.dp)),
                        colors = CardDefaults.cardColors(containerColor = theme.surface),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = String.format("%02d:%02d", schedule.timeHour, schedule.timeMinute),
                                        style = MaterialTheme.typography.headlineMedium.copy(
                                            fontWeight = FontWeight.Light,
                                            color = if (schedule.isEnabled) theme.textPrimary else theme.textSecondary.copy(alpha = 0.5f)
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = schedule.name.uppercase(),
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = theme.primary, letterSpacing = 1.sp)
                                    )
                                }

                                Switch(
                                    checked = schedule.isEnabled,
                                    onCheckedChange = { viewModel.toggleSchedule(schedule) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = theme.onPrimary,
                                        checkedTrackColor = theme.primary,
                                        uncheckedThumbColor = theme.textSecondary,
                                        uncheckedTrackColor = theme.surface
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text(
                                    text = "POOL: ${schedule.sourceType}",
                                    style = MaterialTheme.typography.labelSmall.copy(color = theme.textSecondary)
                                )
                                Text(
                                    text = "MODE: ${schedule.selectionMode}",
                                    style = MaterialTheme.typography.labelSmall.copy(color = theme.textSecondary)
                                )
                                Text(
                                    text = "TARGET: ${schedule.targetScreen.replace("_", " ")}",
                                    style = MaterialTheme.typography.labelSmall.copy(color = theme.textSecondary)
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            Divider(color = theme.borderGlow.copy(alpha = 0.1f))

                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(onClick = { viewModel.triggerNow(schedule) }) {
                                    Text("RUN NOW", color = theme.textPrimary, style = MaterialTheme.typography.labelMedium)
                                }
                                TextButton(onClick = {
                                    selectedScheduleForEdit = schedule
                                    showEditDialog = true
                                }) {
                                    Text("EDIT", color = theme.primary, style = MaterialTheme.typography.labelMedium)
                                }
                                IconButton(onClick = { viewModel.deleteSchedule(schedule) }) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444).copy(alpha = 0.7f))
                                }
                            }
                        }
                    }
                }
            }
        } else {
            EmptyStateView(
                title = "No Rotations Scheduled",
                description = "Set up automated daily or hourly wallpaper rotations using your favorites or collections.",
                icon = Icons.Filled.Schedule,
                buttonText = "CREATE SCHEDULE",
                onButtonClick = {
                    selectedScheduleForEdit = ScheduleEntity(name = "Daily Rotation", timeHour = 8, timeMinute = 0)
                    showEditDialog = true
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }

    // Schedule Edit Dialog
    if (showEditDialog && selectedScheduleForEdit != null) {
        var sched by remember { mutableStateOf(selectedScheduleForEdit!!) }

        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text(if (sched.id == 0L) "New Rotation" else "Edit Rotation", color = theme.textPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedTextField(
                        value = sched.name,
                        onValueChange = { sched = sched.copy(name = it) },
                        label = { Text("Schedule Name") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = theme.primary,
                            unfocusedBorderColor = theme.borderGlow.copy(alpha = 0.3f),
                            focusedTextColor = theme.textPrimary,
                            unfocusedTextColor = theme.textPrimary
                        )
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = sched.timeHour.toString(),
                            onValueChange = { sched = sched.copy(timeHour = it.toIntOrNull()?.coerceIn(0, 23) ?: 0) },
                            label = { Text("Hour (0-23)") },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = theme.primary,
                                unfocusedBorderColor = theme.borderGlow.copy(alpha = 0.3f),
                                focusedTextColor = theme.textPrimary,
                                unfocusedTextColor = theme.textPrimary
                            )
                        )
                        OutlinedTextField(
                            value = sched.timeMinute.toString(),
                            onValueChange = { sched = sched.copy(timeMinute = it.toIntOrNull()?.coerceIn(0, 59) ?: 0) },
                            label = { Text("Minute (0-59)") },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = theme.primary,
                                unfocusedBorderColor = theme.borderGlow.copy(alpha = 0.3f),
                                focusedTextColor = theme.textPrimary,
                                unfocusedTextColor = theme.textPrimary
                            )
                        )
                    }

                    Text("SOURCE POOL", style = MaterialTheme.typography.labelSmall.copy(color = theme.textSecondary, letterSpacing = 1.sp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("FAVORITES", "ALL", "COLLECTION").forEach { pool ->
                            FilterChip(
                                selected = sched.sourceType == pool,
                                onClick = { sched = sched.copy(sourceType = pool) },
                                label = { Text(pool) },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = theme.primary.copy(alpha = 0.2f), selectedLabelColor = theme.primary)
                            )
                        }
                    }

                    Text("SELECTION MODE", style = MaterialTheme.typography.labelSmall.copy(color = theme.textSecondary, letterSpacing = 1.sp))
                    androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(listOf("TIME_OF_DAY", "SMART_SHUFFLE", "WEIGHTED_FAVORITES", "NEVER_REPEAT", "VARIETY", "RANDOM", "LRU", "SEQUENTIAL").size) { idx ->
                            val mode = listOf("TIME_OF_DAY", "SMART_SHUFFLE", "WEIGHTED_FAVORITES", "NEVER_REPEAT", "VARIETY", "RANDOM", "LRU", "SEQUENTIAL")[idx]
                            FilterChip(
                                selected = sched.selectionMode == mode,
                                onClick = { sched = sched.copy(selectionMode = mode) },
                                label = { Text(mode.replace("_", " ")) },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = theme.primary.copy(alpha = 0.2f), selectedLabelColor = theme.primary)
                            )
                        }
                    }

                    Text("TRIGGER TYPE", style = MaterialTheme.typography.labelSmall.copy(color = theme.textSecondary, letterSpacing = 1.sp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("TIME", "BATTERY_SAVER", "CHARGING", "DND").forEach { trigger ->
                            FilterChip(
                                selected = sched.triggerType == trigger,
                                onClick = { sched = sched.copy(triggerType = trigger) },
                                label = { Text(trigger.replace("_", " ")) },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = theme.primary.copy(alpha = 0.2f), selectedLabelColor = theme.primary)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.saveSchedule(sched)
                        showEditDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = theme.primary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("SAVE")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("CANCEL", color = theme.textSecondary)
                }
            },
            containerColor = theme.surface,
            shape = RoundedCornerShape(12.dp)
        )
    }
}
