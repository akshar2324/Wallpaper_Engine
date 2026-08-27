package com.akshar.wallpaperengine.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.akshar.wallpaperengine.theme.LocalThemeColors
import com.akshar.wallpaperengine.ui.components.ProceduralWallpaperPreview
import com.akshar.wallpaperengine.ui.viewmodel.HomeViewModel

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
            .padding(horizontal = 20.dp)
    ) {
        AuraHeader()
        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Good evening, Akshar",
            style = MaterialTheme.typography.headlineSmall.copy(
                color = theme.textPrimary,
                fontWeight = FontWeight.Medium
            )
        )
        Text(
            text = "Your space, on rotation",
            style = MaterialTheme.typography.bodySmall.copy(color = theme.textSecondary)
        )
        Spacer(modifier = Modifier.height(20.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(16.dp))
                .clickable(enabled = currentWallpaper != null) {
                    currentWallpaper?.let { onNavigateToDetail(it.id) }
                }
        ) {
            if (currentWallpaper != null) {
                if (currentWallpaper.isSample || currentWallpaper.uri.startsWith("sample_")) {
                    ProceduralWallpaperPreview(
                        sampleKey = currentWallpaper.uri,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(currentWallpaper.uri)
                            .size(1080)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Current wallpaper: ${currentWallpaper.title}",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Black.copy(alpha = 0.24f), Color.Transparent, Color.Black.copy(alpha = 0.74f))
                            )
                        )
                )

                Text(
                    text = "Active",
                    style = MaterialTheme.typography.labelSmall.copy(color = Color.White),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp)
                        .background(Color.Black.copy(alpha = 0.54f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 5.dp)
                )

                IconButton(
                    onClick = { viewModel.toggleFavorite(currentWallpaper) },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .background(Color.Black.copy(alpha = 0.42f), CircleShape)
                ) {
                    Icon(
                        imageVector = if (currentWallpaper.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = "Toggle favorite",
                        tint = if (currentWallpaper.isFavorite) theme.primary else Color.White
                    )
                }

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                ) {
                    Text(
                        text = currentWallpaper.title,
                        style = MaterialTheme.typography.titleLarge.copy(color = Color.White, fontWeight = FontWeight.Medium)
                    )
                    Text(
                        text = "${currentWallpaper.style ?: "Wallpaper"} · OLED · 4K",
                        style = MaterialTheme.typography.labelMedium.copy(color = Color.White.copy(alpha = 0.72f))
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(theme.surface)
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Your canvas is ready", style = MaterialTheme.typography.titleMedium.copy(color = theme.textPrimary))
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Choose a wallpaper to make it yours.", style = MaterialTheme.typography.bodySmall.copy(color = theme.textSecondary))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onNavigateToLibrary,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = theme.primary, contentColor = theme.onPrimary)
        ) {
            Icon(Icons.Filled.Shuffle, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.size(8.dp))
            Text("Change wallpaper", fontWeight = FontWeight.SemiBold)
        }
        Spacer(modifier = Modifier.height(14.dp))

        val scheduleText = uiState.nextUpcomingSchedule?.let {
            "Next change ${String.format("%02d:%02d", it.timeHour, it.timeMinute)}"
        } ?: "Rotation ready"
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
                .clickable(onClick = onNavigateToSchedules),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(scheduleText, style = MaterialTheme.typography.labelMedium.copy(color = theme.textSecondary))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Rotation on", style = MaterialTheme.typography.labelMedium.copy(color = theme.textSecondary))
                Spacer(modifier = Modifier.size(6.dp))
                Canvas(modifier = Modifier.size(7.dp)) {
                    drawCircle(theme.primary, radius = size.minDimension / 2f, center = Offset(size.width / 2f, size.height / 2f))
                }
            }
        }
    }
}

@Composable
private fun AuraHeader() {
    val theme = LocalThemeColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Canvas(modifier = Modifier.size(22.dp)) {
                drawArc(
                    color = theme.primary,
                    startAngle = 34f,
                    sweepAngle = 274f,
                    useCenter = false,
                    style = Stroke(width = 2.dp.toPx())
                )
                drawCircle(theme.primary, radius = 2.2.dp.toPx(), center = Offset(size.width * 0.83f, size.height * 0.16f))
            }
            Spacer(modifier = Modifier.size(8.dp))
            Text(
                text = "AURA",
                style = MaterialTheme.typography.titleSmall.copy(color = theme.textPrimary, fontWeight = FontWeight.Medium)
            )
        }
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(theme.surfaceVariant, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text("A", style = MaterialTheme.typography.labelSmall.copy(color = theme.textPrimary, fontWeight = FontWeight.Bold))
        }
    }
}
