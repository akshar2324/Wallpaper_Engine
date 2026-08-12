package com.akshar.wallpaperengine.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.akshar.wallpaperengine.data.local.entity.WallpaperEntity
import com.akshar.wallpaperengine.theme.LocalThemeColors
import com.akshar.wallpaperengine.ui.components.ProceduralWallpaperPreview
import com.akshar.wallpaperengine.ui.components.WallpaperCard
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
    val scrollState = rememberScrollState()
    var showApplyQuickMenu by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(bottom = 24.dp)
    ) {
        // Subtle Greeting
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Wallpaper Engine",
                style = MaterialTheme.typography.titleMedium.copy(
                    color = theme.textPrimary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.dp
                )
            )
        }

        // Hero Image - Current Wallpaper
        val currentWallpaper = uiState.currentWallpaper
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .aspectRatio(9f / 16f) // Maintain phone aspect ratio for hero
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, theme.borderGlow.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                .clickable {
                    if (currentWallpaper != null) {
                        onNavigateToDetail(currentWallpaper.id)
                    }
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
                            .crossfade(true)
                            .build(),
                        contentDescription = "Current Wallpaper",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Overlay gradient for text readability
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            androidx.compose.ui.graphics.Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Transparent, Color.Black.copy(alpha = 0.8f))
                            )
                        )
                )

                // Hero Content Overlay
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(20.dp)
                ) {
                    Text(
                        text = "ACTIVE",
                        style = MaterialTheme.typography.labelSmall.copy(color = theme.primary, letterSpacing = 2.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = currentWallpaper.title,
                        style = MaterialTheme.typography.headlineMedium.copy(color = Color.White)
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(
                            onClick = { showApplyQuickMenu = true },
                            colors = ButtonDefaults.buttonColors(containerColor = theme.surface),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("APPLY", style = MaterialTheme.typography.labelMedium.copy(color = theme.textPrimary))
                        }

                        IconButton(
                            onClick = { viewModel.toggleFavorite(currentWallpaper) },
                            modifier = Modifier
                                .background(theme.surface.copy(alpha = 0.5f), CircleShape)
                        ) {
                            Icon(
                                imageVector = if (currentWallpaper.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                contentDescription = "Favorite",
                                tint = if (currentWallpaper.isFavorite) theme.primary else theme.textPrimary
                            )
                        }
                    }
                }
            } else {
                // Empty state for Hero
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(theme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Filled.Wallpaper, contentDescription = null, tint = theme.textSecondary, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No Wallpaper Active", style = MaterialTheme.typography.titleMedium.copy(color = theme.textSecondary))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Upcoming Schedule Strip
        val upcoming = uiState.nextUpcomingSchedule
        if (upcoming != null) {
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                Text(
                    text = "UPCOMING ROTATION",
                    style = MaterialTheme.typography.labelSmall.copy(color = theme.textSecondary, letterSpacing = 1.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onNavigateToSchedules() }
                ) {
                    Icon(Icons.Outlined.Schedule, contentDescription = null, tint = theme.primary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${upcoming.name} at ${String.format("%02d:%02d", upcoming.timeHour, upcoming.timeMinute)}",
                        style = MaterialTheme.typography.bodyMedium.copy(color = theme.textPrimary)
                    )
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }

        // Recently Used Horizontal Carousel
        if (uiState.recentlyUsed.isNotEmpty()) {
            Column(modifier = Modifier.fillMaxWidth()) {
                PaddingValues(horizontal = 24.dp).let { padding ->
                    Text(
                        text = "RECENTLY APPLIED",
                        style = MaterialTheme.typography.labelSmall.copy(color = theme.textSecondary, letterSpacing = 1.dp),
                        modifier = Modifier.padding(padding)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 24.dp)
                ) {
                    items(uiState.recentlyUsed, key = { it.id }) { item ->
                        WallpaperCard(
                            wallpaper = item,
                            onClick = { onNavigateToDetail(item.id) },
                            onFavoriteToggle = { viewModel.toggleFavorite(item) },
                            modifier = Modifier.width(110.dp) // Smaller width for recently used strip
                        )
                    }
                }
            }
        }
    }

    // Quick Apply Modal
    if (showApplyQuickMenu && uiState.currentWallpaper != null) {
        AlertDialog(
            onDismissRequest = { showApplyQuickMenu = false },
            title = { Text("Set Destination", style = MaterialTheme.typography.titleLarge.copy(color = theme.textPrimary)) },
            text = { Text("Where would you like to apply this wallpaper?", style = MaterialTheme.typography.bodyMedium.copy(color = theme.textSecondary)) },
            confirmButton = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            viewModel.applyWallpaperQuick(uiState.currentWallpaper!!, "HOME")
                            showApplyQuickMenu = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = theme.surfaceVariant),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("HOME SCREEN", color = theme.textPrimary)
                    }
                    Button(
                        onClick = {
                            viewModel.applyWallpaperQuick(uiState.currentWallpaper!!, "LOCK")
                            showApplyQuickMenu = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = theme.surfaceVariant),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("LOCK SCREEN", color = theme.textPrimary)
                    }
                    Button(
                        onClick = {
                            viewModel.applyWallpaperQuick(uiState.currentWallpaper!!, "HOME_AND_LOCK")
                            showApplyQuickMenu = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = theme.primary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("HOME & LOCK SCREEN", color = theme.onPrimary)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showApplyQuickMenu = false }) {
                    Text("CANCEL", color = theme.textSecondary)
                }
            },
            containerColor = theme.surface,
            shape = RoundedCornerShape(12.dp)
        )
    }
}
