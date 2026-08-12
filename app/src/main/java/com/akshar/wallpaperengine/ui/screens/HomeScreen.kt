package com.akshar.wallpaperengine.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.akshar.wallpaperengine.data.local.entity.ScheduleEntity
import com.akshar.wallpaperengine.data.local.entity.WallpaperEntity
import com.akshar.wallpaperengine.theme.LocalThemeColors
import com.akshar.wallpaperengine.ui.components.ProceduralWallpaperPreview
import com.akshar.wallpaperengine.ui.components.WallpaperCard
import com.akshar.wallpaperengine.ui.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
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

    val currentWallpaper = uiState.currentWallpaper
    var showApplyQuickMenu by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
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
                    text = "WALLPAPER ENGINE",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = theme.primary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Dashboard",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        color = theme.textPrimary,
                        fontWeight = FontWeight.ExtraBold
                    )
                )
            }

            // Engine status badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(theme.surfaceVariant)
                    .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF22C55E))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ONLINE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = theme.textSecondary,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Hero Active Wallpaper Display
        Text(
            text = "Active Surface",
            style = MaterialTheme.typography.titleSmall.copy(
                color = theme.textSecondary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        )
        Spacer(modifier = Modifier.height(8.dp))

        val cardShape = RoundedCornerShape(16.dp)

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .clip(cardShape)
                .border(1.dp, Color.White.copy(alpha = 0.1f), cardShape)
                .clickable {
                    if (currentWallpaper != null) onNavigateToDetail(currentWallpaper.id) else onNavigateToLibrary()
                }
                .testTag("current_wallpaper_card"),
            colors = CardDefaults.cardColors(containerColor = theme.surface),
            shape = cardShape
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
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
                                .size(800) // downsample for performance
                                .crossfade(true)
                                .build(),
                            contentDescription = currentWallpaper.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // Gradient overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f)),
                                    startY = 100f
                                )
                            )
                    )

                    // Overlay info
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp)
                    ) {
                        Text(
                            text = currentWallpaper.title,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${currentWallpaper.width}x${currentWallpaper.height} • ${currentWallpaper.mimeType.substringAfter('/')}",
                            style = MaterialTheme.typography.bodySmall.copy(color = theme.textSecondary)
                        )
                    }

                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(12.dp)
                    ) {
                        Button(
                            onClick = { showApplyQuickMenu = true },
                            colors = ButtonDefaults.buttonColors(containerColor = theme.primary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("quick_apply_button")
                        ) {
                            Icon(Icons.Filled.FlashOn, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Apply")
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(theme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Filled.Wallpaper, contentDescription = null, tint = theme.primary, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No Wallpaper Set", style = MaterialTheme.typography.titleMedium.copy(color = theme.textPrimary))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Explore library to pick one", style = MaterialTheme.typography.bodySmall.copy(color = theme.textSecondary))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Quick Stats Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                count = uiState.totalWallpapers.toString(),
                label = "Wallpapers",
                icon = Icons.Filled.PhotoLibrary,
                onClick = onNavigateToLibrary,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                count = uiState.totalFavorites.toString(),
                label = "Favorites",
                icon = Icons.Filled.Favorite,
                onClick = onNavigateToLibrary,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                count = uiState.totalSchedules.toString(),
                label = "Active Rotations",
                icon = Icons.Outlined.Schedule,
                onClick = onNavigateToSchedules,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Upcoming Schedule Banner
        val upcoming = uiState.nextUpcomingSchedule
        if (upcoming != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onNavigateToSchedules),
                colors = CardDefaults.cardColors(containerColor = theme.surfaceVariant),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(theme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Autorenew, contentDescription = null, tint = theme.primary)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Next Scheduled Rotation",
                            style = MaterialTheme.typography.labelMedium.copy(color = theme.textSecondary)
                        )
                        Text(
                            text = "${upcoming.name} (${String.format("%02d:%02d", upcoming.timeHour, upcoming.timeMinute)})",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = theme.textPrimary)
                        )
                    }
                    Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = theme.textSecondary)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Recently Used Horizontal Carousel
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Recently Applied",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = theme.textPrimary
                )
            )
            TextTextButton(text = "View All", onClick = onNavigateToLibrary)
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (uiState.recentlyUsed.isNotEmpty()) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.recentlyUsed, key = { it.id }) { item ->
                    WallpaperCard(
                        wallpaper = item,
                        onClick = { onNavigateToDetail(item.id) },
                        onFavoriteToggle = { viewModel.toggleFavorite(item) },
                        modifier = Modifier.width(130.dp)
                    )
                }
            }
        } else {
            Text(
                text = "No recently applied wallpapers yet",
                style = MaterialTheme.typography.bodySmall.copy(color = theme.textSecondary),
                modifier = Modifier.padding(vertical = 12.dp)
            )
        }
    }

    // Quick Apply Modal
    if (showApplyQuickMenu && currentWallpaper != null) {
        AlertDialog(
            onDismissRequest = { showApplyQuickMenu = false },
            title = { Text("Set Active Wallpaper", color = theme.textPrimary) },
            text = { Text("Choose destination screen:", color = theme.textSecondary) },
            confirmButton = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            viewModel.applyWallpaperQuick(currentWallpaper, "HOME")
                            showApplyQuickMenu = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = theme.primary)
                    ) {
                        Text("Home Screen")
                    }
                    Button(
                        onClick = {
                            viewModel.applyWallpaperQuick(currentWallpaper, "LOCK")
                            showApplyQuickMenu = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = theme.primaryContainer, contentColor = theme.onPrimaryContainer)
                    ) {
                        Text("Lock Screen")
                    }
                    Button(
                        onClick = {
                            viewModel.applyWallpaperQuick(currentWallpaper, "HOME_AND_LOCK")
                            showApplyQuickMenu = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = theme.secondary, contentColor = Color.White)
                    ) {
                        Text("Home & Lock Screen")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showApplyQuickMenu = false }) {
                    Text("Cancel", color = theme.textSecondary)
                }
            },
            containerColor = theme.surface
        )
    }
}

@Composable
private fun StatCard(
    count: String,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = LocalThemeColors.current

    Card(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = theme.surfaceVariant),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = theme.primary, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = count, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = theme.textPrimary))
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = label, style = MaterialTheme.typography.labelSmall.copy(color = theme.textSecondary))
        }
    }
}

@Composable
private fun TextTextButton(text: String, onClick: () -> Unit) {
    val theme = LocalThemeColors.current
    TextButton(onClick = onClick) {
        Text(text = text, style = MaterialTheme.typography.labelMedium.copy(color = theme.primary, fontWeight = FontWeight.Bold))
    }
}
