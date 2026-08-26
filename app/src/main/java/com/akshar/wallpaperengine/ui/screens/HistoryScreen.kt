package com.akshar.wallpaperengine.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = LocalThemeColors.current
    val uiState by viewModel.uiState.collectAsState()
    val dateFormat = remember { SimpleDateFormat("MMM dd • HH:mm", Locale.getDefault()) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = theme.textPrimary)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "HISTORY",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = theme.textPrimary, letterSpacing = 1.sp)
                )
            }

            if (uiState.historyRecords.isNotEmpty()) {
                IconButton(onClick = { viewModel.clearHistory() }) {
                    Icon(Icons.Filled.DeleteSweep, contentDescription = "Clear History", tint = theme.textSecondary)
                }
            }
        }

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
                                .padding(top = 24.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (record.source == "SCHEDULE") theme.primary else theme.textSecondary)
                            )
                            if (index < uiState.historyRecords.size - 1) {
                                Box(
                                    modifier = Modifier
                                        .width(1.dp)
                                        .height(72.dp)
                                        .background(theme.borderGlow.copy(alpha = 0.2f))
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Timeline Content
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(vertical = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = record.wallpaperTitle,
                                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold, color = theme.textPrimary)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = record.source.uppercase(),
                                            style = MaterialTheme.typography.labelSmall.copy(color = if (record.source == "SCHEDULE") theme.primary else theme.textSecondary, letterSpacing = 1.sp)
                                        )
                                        if (!record.selectionReason.isNullOrBlank()) {
                                            Text("•", color = theme.textSecondary)
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = theme.primary.copy(alpha = 0.15f),
                                                border = androidx.compose.foundation.BorderStroke(1.dp, theme.primary.copy(alpha = 0.4f))
                                            ) {
                                                Text(
                                                    text = record.selectionReason,
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        color = theme.primary,
                                                        fontWeight = FontWeight.SemiBold,
                                                        fontSize = 10.sp
                                                    ),
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                        Text("•", color = theme.textSecondary)
                                        Text(
                                            text = dateFormat.format(Date(record.appliedAt)),
                                            style = MaterialTheme.typography.labelSmall.copy(color = theme.textSecondary)
                                        )
                                    }
                                }

                                Row {
                                    IconButton(onClick = { viewModel.reapplyHistoryWallpaper(record) }) {
                                        Icon(Icons.Filled.Replay, contentDescription = "Reapply", tint = theme.textPrimary)
                                    }
                                }
                            }

                            if (index < uiState.historyRecords.size - 1) {
                                Divider(color = theme.borderGlow.copy(alpha = 0.1f), modifier = Modifier.padding(top = 12.dp))
                            }
                        }
                    }
                }
            }
        } else {
            EmptyStateView(
                title = "No History",
                description = "All applied wallpapers and rotation events will appear here.",
                icon = Icons.Filled.History,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
