package com.akshar.wallpaperengine.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.akshar.wallpaperengine.data.local.entity.ScheduleEntity
import com.akshar.wallpaperengine.theme.LocalThemeColors
import com.akshar.wallpaperengine.ui.components.EmptyStateView
import com.akshar.wallpaperengine.ui.viewmodel.SchedulesViewModel

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

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Automated Rotations",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold, color = theme.textPrimary)
            )

            FloatingActionButton(
                onClick = {
                    selectedScheduleForEdit = ScheduleEntity(name = "Daily Rotation", timeHour = 8, timeMinute = 0)
                    showEditDialog = true
                },
                containerColor = theme.primary,
                contentColor = theme.onPrimary,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("add_schedule_button")
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add Schedule")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Schedule Conflict Warning Banner
        if (uiState.conflictingSchedules.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF7F1D1D)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Warning, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Schedule Conflict Detected: Multiple active rotations run at the exact same time.",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.White)
                    )
                }
            }
        }

        if (uiState.schedules.isNotEmpty()) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(uiState.schedules, key = { it.id }) { schedule ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp)),
                        colors = CardDefaults.cardColors(containerColor = theme.surfaceVariant),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (schedule.isEnabled) theme.primary.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.05f))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = String.format("%02d:%02d", schedule.timeHour, schedule.timeMinute),
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = if (schedule.isEnabled) theme.primary else theme.textSecondary
                                            )
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = schedule.name,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = theme.textPrimary)
                                    )
                                }

                                Switch(
                                    checked = schedule.isEnabled,
                                    onCheckedChange = { viewModel.toggleSchedule(schedule) }
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Surface(
                                    color = theme.surface,
                                    shape = RoundedCornerShape(6.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.06f))
                                ) {
                                    Text(
                                        text = "POOL: ${schedule.sourceType}",
                                        style = MaterialTheme.typography.labelSmall.copy(color = theme.textSecondary, fontSize = 10.sp),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }

                                Surface(
                                    color = theme.surface,
                                    shape = RoundedCornerShape(6.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.06f))
                                ) {
                                    Text(
                                        text = "MODE: ${schedule.selectionMode}",
                                        style = MaterialTheme.typography.labelSmall.copy(color = theme.textSecondary, fontSize = 10.sp),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }

                                Surface(
                                    color = theme.surface,
                                    shape = RoundedCornerShape(6.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.06f))
                                ) {
                                    Text(
                                        text = "TARGET: ${schedule.targetScreen}",
                                        style = MaterialTheme.typography.labelSmall.copy(color = theme.textSecondary, fontSize = 10.sp),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(onClick = { viewModel.triggerNow(schedule) }) {
                                    Text("Run Now", color = theme.secondary, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                                }
                                TextButton(onClick = {
                                    selectedScheduleForEdit = schedule
                                    showEditDialog = true
                                }) {
                                    Text("Edit", color = theme.primary, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                                }
                                IconButton(onClick = { viewModel.deleteSchedule(schedule) }) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = theme.textSecondary)
                                }
                            }
                        }
                    }
                }
            }
        } else {
            EmptyStateView(
                title = "No Rotations Scheduled",
                description = "Set up automated daily or hourly wallpaper rotations using your favorites or specific collections.",
                icon = Icons.Filled.Schedule,
                buttonText = "Create Schedule",
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
            title = { Text(if (sched.id == 0L) "New Rotation Schedule" else "Edit Rotation Schedule", color = theme.textPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = sched.name,
                        onValueChange = { sched = sched.copy(name = it) },
                        label = { Text("Schedule Name") },
                        singleLine = true
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = sched.timeHour.toString(),
                            onValueChange = { sched = sched.copy(timeHour = it.toIntOrNull()?.coerceIn(0, 23) ?: 0) },
                            label = { Text("Hour (0-23)") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = sched.timeMinute.toString(),
                            onValueChange = { sched = sched.copy(timeMinute = it.toIntOrNull()?.coerceIn(0, 59) ?: 0) },
                            label = { Text("Minute (0-59)") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Text("Source Wallpaper Pool", style = MaterialTheme.typography.labelMedium.copy(color = theme.textSecondary))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("FAVORITES", "ALL", "COLLECTION").forEach { pool ->
                            FilterChip(
                                selected = sched.sourceType == pool,
                                onClick = { sched = sched.copy(sourceType = pool) },
                                label = { Text(pool) }
                            )
                        }
                    }

                    Text("Selection Mode", style = MaterialTheme.typography.labelMedium.copy(color = theme.textSecondary))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("RANDOM", "SEQUENTIAL", "LRU").forEach { mode ->
                            FilterChip(
                                selected = sched.selectionMode == mode,
                                onClick = { sched = sched.copy(selectionMode = mode) },
                                label = { Text(mode) }
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
                    colors = ButtonDefaults.buttonColors(containerColor = theme.primary)
                ) {
                    Text("Save Schedule")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Cancel")
                }
            },
            containerColor = theme.surface
        )
    }
}
