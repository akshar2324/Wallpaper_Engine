package com.akshar.wallpaperengine.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.akshar.wallpaperengine.data.local.entity.WallpaperEntity
import com.akshar.wallpaperengine.theme.LocalThemeColors
import com.akshar.wallpaperengine.ui.components.ProceduralWallpaperPreview
import com.akshar.wallpaperengine.ui.viewmodel.WallpaperDetailViewModel

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.text.font.FontFamily
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun WallpaperDetailScreen(
    viewModel: WallpaperDetailViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = LocalThemeColors.current
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    var showApplyModal by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showTagDialog by remember { mutableStateOf(false) }
    var newTagName by remember { mutableStateOf("") }

    val wallpaper = uiState.wallpaper

    if (wallpaper == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = theme.primary)
        }
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(theme.background)
            .verticalScroll(scrollState)
    ) {
        // Immersive Hero Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(440.dp)
                .clip(RoundedCornerShape(20.dp))
        ) {
            if (wallpaper.isSample || wallpaper.uri.startsWith("sample_")) {
                ProceduralWallpaperPreview(
                    sampleKey = wallpaper.uri,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(wallpaper.uri)
                        .crossfade(true)
                        .build(),
                    contentDescription = wallpaper.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Top action bar overlay
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.background(Color.Black.copy(alpha = 0.42f), CircleShape)
                ) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = { viewModel.toggleFavorite() },
                        modifier = Modifier.background(Color.Black.copy(alpha = 0.42f), CircleShape)
                    ) {
                        Icon(
                            imageVector = if (wallpaper.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (wallpaper.isFavorite) theme.primary else Color.White
                        )
                    }
                    IconButton(
                        onClick = { showDeleteConfirm = true },
                        modifier = Modifier.background(Color.Black.copy(alpha = 0.42f), CircleShape)
                    ) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = Color.White)
                    }
                }
            }
        }

        // Details Section Bottom Panel
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = wallpaper.title,
                style = MaterialTheme.typography.headlineMedium.copy(color = theme.textPrimary, fontWeight = FontWeight.Medium)
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "${wallpaper.width} × ${wallpaper.height}",
                    style = MaterialTheme.typography.labelSmall.copy(color = theme.textSecondary)
                )
                Text(
                    text = wallpaper.mimeType.substringAfter('/').uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(color = theme.textSecondary)
                )
                Text(
                    text = String.format(Locale.getDefault(), "%.1f MB", wallpaper.fileSize / (1024f * 1024f)),
                    style = MaterialTheme.typography.labelSmall.copy(color = theme.textSecondary)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Wallpaper DNA Card Section
            WallpaperDnaCard(
                wallpaper = wallpaper,
                onRate = { viewModel.updateRating(it) },
                onLike = { viewModel.recordLike() },
                onSkip = { viewModel.recordSkip() }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Tags
            if (uiState.tags.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    uiState.tags.forEach { tag ->
                        InputChip(
                            selected = false,
                            onClick = { viewModel.removeTag(tag) },
                            label = { Text("#${tag.name}", style = MaterialTheme.typography.labelMedium) },
                            trailingIcon = { Icon(Icons.Filled.Close, contentDescription = "Remove", modifier = Modifier.size(14.dp)) },
                            colors = InputChipDefaults.inputChipColors(
                                containerColor = theme.surface,
                                labelColor = theme.textSecondary
                            ),
                            border = InputChipDefaults.inputChipBorder(
                                enabled = true,
                                selected = false,
                                borderColor = theme.borderGlow.copy(alpha = 0.2f)
                            )
                        )
                    }
                    InputChip(
                        selected = false,
                        onClick = { showTagDialog = true },
                        label = { Text("ADD TAG", style = MaterialTheme.typography.labelMedium.copy(color = theme.primary)) },
                        colors = InputChipDefaults.inputChipColors(containerColor = Color.Transparent),
                        border = InputChipDefaults.inputChipBorder(
                            enabled = true,
                            selected = false,
                            borderColor = Color.Transparent
                        )
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            } else {
                TextButton(
                    onClick = { showTagDialog = true },
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("+ ADD TAG", style = MaterialTheme.typography.labelMedium.copy(color = theme.primary))
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Visual Similarity / Near Duplicates Section (Phase 5)
            if (uiState.similarWallpapers.isNotEmpty()) {
                Text(
                    text = "VISUALLY SIMILAR (${uiState.similarWallpapers.size})",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = theme.textSecondary,
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                androidx.compose.foundation.lazy.LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(uiState.similarWallpapers.size) { idx ->
                        val item = uiState.similarWallpapers[idx]
                        Box(
                            modifier = Modifier
                                .width(70.dp)
                                .height(110.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, theme.borderGlow.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        ) {
                            if (item.isSample || item.uri.startsWith("sample_")) {
                                ProceduralWallpaperPreview(sampleKey = item.uri, modifier = Modifier.fillMaxSize())
                            } else {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(item.uri)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = item.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Main Action Button
            Button(
                onClick = { showApplyModal = true },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = theme.primary),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("APPLY WALLPAPER", style = MaterialTheme.typography.labelLarge.copy(color = theme.onPrimary, fontWeight = FontWeight.Bold, letterSpacing = 1.sp))
            }
        }
    }

    // Apply Destination Modal Dialog
    if (showApplyModal) {
        AlertDialog(
            onDismissRequest = { showApplyModal = false },
            title = { Text("Apply Wallpaper", style = MaterialTheme.typography.titleLarge.copy(color = theme.textPrimary)) },
            text = { Text("Select target destination:", style = MaterialTheme.typography.bodyMedium.copy(color = theme.textSecondary)) },
            confirmButton = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            viewModel.applyWallpaper("HOME")
                            showApplyModal = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = theme.surfaceVariant),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("HOME SCREEN", color = theme.textPrimary)
                    }
                    Button(
                        onClick = {
                            viewModel.applyWallpaper("LOCK")
                            showApplyModal = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = theme.surfaceVariant),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("LOCK SCREEN", color = theme.textPrimary)
                    }
                    Button(
                        onClick = {
                            viewModel.applyWallpaper("HOME_AND_LOCK")
                            showApplyModal = false
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
                TextButton(onClick = { showApplyModal = false }) {
                    Text("CANCEL", color = theme.textSecondary)
                }
            },
            containerColor = theme.surface,
            shape = RoundedCornerShape(12.dp)
        )
    }

    // Add Tag Dialog
    if (showTagDialog) {
        AlertDialog(
            onDismissRequest = { showTagDialog = false },
            title = { Text("Add Tag", color = theme.textPrimary) },
            text = {
                OutlinedTextField(
                    value = newTagName,
                    onValueChange = { newTagName = it },
                    placeholder = { Text("e.g. cyberpunk") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = theme.primary,
                        unfocusedBorderColor = theme.borderGlow.copy(alpha = 0.3f),
                        focusedTextColor = theme.textPrimary,
                        unfocusedTextColor = theme.textPrimary
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.addTag(newTagName)
                        newTagName = ""
                        showTagDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = theme.primary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("ADD")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTagDialog = false }) {
                    Text("CANCEL", color = theme.textSecondary)
                }
            },
            containerColor = theme.surface,
            shape = RoundedCornerShape(12.dp)
        )
    }

    // Confirm Delete Dialog
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Wallpaper", color = theme.textPrimary) },
            text = { Text("Are you sure you want to remove this wallpaper?", color = theme.textSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteWallpaper {
                            showDeleteConfirm = false
                            onBack()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("DELETE")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("CANCEL", color = theme.textSecondary)
                }
            },
            containerColor = theme.surface,
            shape = RoundedCornerShape(12.dp)
        )
    }
}

@Composable
fun WallpaperDnaCard(
    wallpaper: WallpaperEntity,
    onRate: (Float) -> Unit,
    onLike: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = LocalThemeColors.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(theme.surface, RoundedCornerShape(16.dp))
            .border(1.dp, theme.borderGlow.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        // Card Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.AutoAwesome,
                    contentDescription = null,
                    tint = theme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "WALLPAPER DNA",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = theme.textPrimary,
                        letterSpacing = 1.2.sp
                    )
                )
            }

            if (wallpaper.isDark) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color.Black.copy(alpha = 0.6f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, theme.primary.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.DarkMode,
                            contentDescription = null,
                            tint = theme.primary,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "DARK / OLED",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = theme.primary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                letterSpacing = 0.5.sp
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 1. 5-Star Rating & Quick Feedback (Likes/Skips) Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Interactive 5-Star Rating Bar
            Column {
                Text(
                    text = "RATING",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = theme.textSecondary,
                        letterSpacing = 1.sp
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    for (i in 1..5) {
                        val isFilled = i <= wallpaper.rating.toInt()
                        IconButton(
                            onClick = { onRate(i.toFloat()) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = if (isFilled) Icons.Filled.Star else Icons.Filled.StarBorder,
                                contentDescription = "Rate $i stars",
                                tint = if (isFilled) Color(0xFFFFB300) else theme.textSecondary.copy(alpha = 0.4f),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (wallpaper.rating > 0f) String.format(Locale.getDefault(), "%.1f", wallpaper.rating) else "Unrated",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = if (wallpaper.rating > 0f) Color(0xFFFFB300) else theme.textSecondary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            // Quick Feedback Buttons: Like & Skip
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                // Like Button
                Surface(
                    onClick = onLike,
                    shape = RoundedCornerShape(8.dp),
                    color = theme.surfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(1.dp, theme.borderGlow.copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ThumbUp,
                            contentDescription = "Like",
                            tint = theme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${wallpaper.likeCount}",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = theme.textPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }

                // Skip Button
                Surface(
                    onClick = onSkip,
                    shape = RoundedCornerShape(8.dp),
                    color = theme.surfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(1.dp, theme.borderGlow.copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ThumbDown,
                            contentDescription = "Skip",
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${wallpaper.skipCount}",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = theme.textPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Divider(color = theme.borderGlow.copy(alpha = 0.15f))
        Spacer(modifier = Modifier.height(14.dp))

        // 2. Color Palette Swatches & Luminance Metric
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Dominant & Secondary Swatches
            Column {
                Text(
                    text = "PALETTE",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = theme.textSecondary,
                        letterSpacing = 1.sp
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Dominant Color Swatch
                    wallpaper.dominantColor?.let { domColor ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .background(Color(domColor))
                                    .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = String.format(Locale.getDefault(), "#%06X", domColor and 0xFFFFFF),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = theme.textPrimary,
                                    fontFamily = FontFamily.Monospace
                                )
                            )
                        }
                    }

                    // Secondary Color Swatch
                    wallpaper.secondaryColor?.let { secColor ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .background(Color(secColor))
                                    .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = String.format(Locale.getDefault(), "#%06X", secColor and 0xFFFFFF),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = theme.textSecondary,
                                    fontFamily = FontFamily.Monospace
                                )
                            )
                        }
                    }

                    if (wallpaper.dominantColor == null && wallpaper.secondaryColor == null) {
                        Text("No palette data", style = MaterialTheme.typography.bodySmall.copy(color = theme.textSecondary))
                    }
                }
            }

            // Luminance / Brightness Metric
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "LUMINANCE",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = theme.textSecondary,
                        letterSpacing = 1.sp
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.LightMode,
                        contentDescription = null,
                        tint = theme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${(wallpaper.brightness * 100).toInt()}%",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = theme.textPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }

        // 3. Style & Mood Chips (if present)
        if (!wallpaper.style.isNullOrBlank() || !wallpaper.mood.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(14.dp))
            Divider(color = theme.borderGlow.copy(alpha = 0.15f))
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                wallpaper.style?.let { style ->
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = theme.surfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(1.dp, theme.borderGlow.copy(alpha = 0.2f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "STYLE: ",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = theme.textSecondary,
                                    fontSize = 10.sp,
                                    letterSpacing = 0.5.sp
                                )
                            )
                            Text(
                                text = style,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = theme.primary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }
                }

                wallpaper.mood?.let { mood ->
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = theme.surfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(1.dp, theme.borderGlow.copy(alpha = 0.2f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "MOOD: ",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = theme.textSecondary,
                                    fontSize = 10.sp,
                                    letterSpacing = 0.5.sp
                                )
                            )
                            Text(
                                text = mood,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = theme.primary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
