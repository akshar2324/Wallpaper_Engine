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
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.akshar.wallpaperengine.theme.LocalThemeColors
import com.akshar.wallpaperengine.ui.components.ProceduralWallpaperPreview
import com.akshar.wallpaperengine.ui.viewmodel.WallpaperDetailViewModel

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
    ) {
        // Immersive Hero Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f) // Takes up majority of screen
                .padding(16.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, theme.borderGlow.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
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
                    modifier = Modifier.background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                ) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = { viewModel.toggleFavorite() },
                        modifier = Modifier.background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                    ) {
                        Icon(
                            imageVector = if (wallpaper.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (wallpaper.isFavorite) theme.primary else Color.White
                        )
                    }
                    IconButton(
                        onClick = { showDeleteConfirm = true },
                        modifier = Modifier.background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
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
                .padding(bottom = 24.dp)
        ) {
            Text(
                text = wallpaper.title,
                style = MaterialTheme.typography.headlineMedium.copy(color = theme.textPrimary, fontWeight = FontWeight.Bold)
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
                    text = String.format("%.1f MB", wallpaper.fileSize / (1024f * 1024f)),
                    style = MaterialTheme.typography.labelSmall.copy(color = theme.textSecondary)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

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
                            border = InputChipDefaults.inputChipBorder(borderColor = theme.borderGlow.copy(alpha = 0.2f))
                        )
                    }
                    InputChip(
                        selected = false,
                        onClick = { showTagDialog = true },
                        label = { Text("ADD TAG", style = MaterialTheme.typography.labelMedium.copy(color = theme.primary)) },
                        colors = InputChipDefaults.inputChipColors(containerColor = Color.Transparent),
                        border = InputChipDefaults.inputChipBorder(borderColor = Color.Transparent)
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

            // Main Action Button
            Button(
                onClick = { showApplyModal = true },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = theme.primary),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("APPLY WALLPAPER", style = MaterialTheme.typography.labelLarge.copy(color = theme.onPrimary, fontWeight = FontWeight.Bold, letterSpacing = 1.dp))
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
