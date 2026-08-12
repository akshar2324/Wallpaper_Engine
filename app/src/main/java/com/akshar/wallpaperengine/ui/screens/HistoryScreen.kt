package com.akshar.wallpaperengine.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
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
import com.akshar.wallpaperengine.theme.LocalThemeColors
import com.akshar.wallpaperengine.ui.components.EmptyStateView
import com.akshar.wallpaperengine.ui.viewmodel.HistoryViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel,
    modifier: Modifier = Modifier
) {
    val theme = LocalThemeColors.current
    val uiState by viewModel.uiState.collectAsState()
    val dateFormat = remember { SimpleDateFormat("MMM dd • HH:mm", Locale.getDefault()) }

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
            Column {
                Text(
                    text = "Application Timeline",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold, color = theme.textPrimary)
                )
                Text(
                    text = "Applied wallpapers & rotation history log",
                    style = MaterialTheme.typography.bodySmall.copy(color = theme.textSecondary)
                )
            }

            if (uiState.historyRecords.isNotEmpty()) {
                IconButton(onClick = { viewModel.clearHistory() }) {
                    Icon(Icons.Filled.DeleteSweep, contentDescription = "Clear History", tint = theme.textSecondary)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (uiState.historyRecords.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                itemsIndexed(uiState.historyRecords, key = { _, item -> item.id }) { index, record ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        // Timeline Left Node & Connecting Line
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .width(32.dp)
                                .padding(top = 16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(if (record.source == "SCHEDULE") theme.primary else theme.secondary)
                            )
                            if (index < uiState.historyRecords.size - 1) {
                                Box(
                                    modifier = Modifier
                                        .width(2.dp)
                                        .height(72.dp)
                                        .background(Color.White.copy(alpha = 0.08f))
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Timeline Card Content
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .padding(bottom = 12.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(14.dp)),
                            colors = CardDefaults.cardColors(containerColor = theme.surfaceVariant),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = record.wallpaperTitle,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = theme.textPrimary)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Surface(
                                            color = theme.surface,
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = record.source,
                                                style = MaterialTheme.typography.labelSmall.copy(color = theme.primary, fontSize = 9.sp, fontWeight = FontWeight.Bold),
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                        Text(
                                            text = "${dateFormat.format(Date(record.appliedAt))} • Target: ${record.targetScreen}",
                                            style = MaterialTheme.typography.bodySmall.copy(color = theme.textSecondary, fontSize = 11.sp)
                                        )
                                    }
                                }

                                Row {
                                    IconButton(onClick = { viewModel.reapplyHistoryWallpaper(record) }) {
                                        Icon(Icons.Filled.Replay, contentDescription = "Reapply", tint = theme.primary)
                                    }
                                    IconButton(onClick = { viewModel.deleteHistoryRecord(record) }) {
                                        Icon(Icons.Filled.Close, contentDescription = "Delete", tint = theme.textSecondary)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            EmptyStateView(
                title = "No History Records",
                description = "All applied wallpapers and scheduled rotation events will appear in this timeline.",
                icon = Icons.Filled.History,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
