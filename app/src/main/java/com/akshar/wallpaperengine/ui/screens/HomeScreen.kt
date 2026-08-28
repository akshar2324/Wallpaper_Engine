package com.akshar.wallpaperengine.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.akshar.wallpaperengine.ui.viewmodel.HomeViewModel
import com.akshar.wallpaperengine.theme.LocalThemeColors
import com.akshar.wallpaperengine.ui.components.AuraHeader
import com.akshar.wallpaperengine.ui.components.AuraWallpaperItem
import com.akshar.wallpaperengine.ui.components.AuraButton

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToLibrary: () -> Unit,
    onNavigateToDetail: (Long) -> Unit,
    onNavigateToSchedules: () -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = LocalThemeColors.current
    val uiState by viewModel.uiState.collectAsState()
    val currentWallpaper = uiState.currentWallpaper

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(theme.background)
            .padding(horizontal = 24.dp)
    ) {
        AuraTopBar()

        Spacer(modifier = Modifier.height(8.dp))

        AuraHeader(
            title = "Your space,\non rotation.",
            subtitle = "Good evening, Akshar."
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (currentWallpaper != null) {
            AuraWallpaperItem(
                wallpaper = currentWallpaper,
                onClick = { onNavigateToDetail(currentWallpaper.id) },
                onToggleFavorite = { viewModel.toggleFavorite(currentWallpaper) },
                isActive = true,
                aspectRatio = 0.85f,
                modifier = Modifier.weight(1f)
            )
        } else {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(theme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No wallpaper active",
                    style = MaterialTheme.typography.bodyLarge.copy(color = theme.textSecondary)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        AuraButton(
            text = "Shuffle Wallpaper",
            icon = Icons.Filled.Shuffle,
            onClick = onNavigateToLibrary,
            isPrimary = true
        )

        Spacer(modifier = Modifier.height(24.dp))

        val scheduleText = uiState.nextUpcomingSchedule?.let {
            "Next change ${String.format("%02d:%02d", it.timeHour, it.timeMinute)}"
        } ?: "Rotation ready"

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
                .clickable(onClick = onNavigateToSchedules),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(scheduleText, style = MaterialTheme.typography.labelMedium.copy(color = theme.textSecondary))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Rotation on", style = MaterialTheme.typography.labelMedium.copy(color = theme.textSecondary))
                Spacer(modifier = Modifier.size(8.dp))
                Canvas(modifier = Modifier.size(8.dp)) {
                    drawCircle(theme.primary, radius = size.minDimension / 2f, center = Offset(size.width / 2f, size.height / 2f))
                }
            }
        }
    }
}

@Composable
private fun AuraTopBar() {
    val theme = LocalThemeColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Canvas(modifier = Modifier.size(24.dp)) {
                drawArc(
                    color = theme.primary,
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = false,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )
                drawCircle(theme.primary, radius = 3.dp.toPx(), center = Offset(size.width * 0.5f, size.height * 0.8f))
            }
            Spacer(modifier = Modifier.size(12.dp))
            Text(
                text = "AURA",
                style = MaterialTheme.typography.titleMedium.copy(color = theme.textPrimary, fontWeight = FontWeight.Bold)
            )
        }
    }
}
