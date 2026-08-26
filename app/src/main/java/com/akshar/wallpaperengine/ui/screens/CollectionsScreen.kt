package com.akshar.wallpaperengine.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.akshar.wallpaperengine.theme.LocalThemeColors
import com.akshar.wallpaperengine.ui.components.EmptyStateView
import com.akshar.wallpaperengine.ui.components.WallpaperCard
import com.akshar.wallpaperengine.ui.viewmodel.CollectionsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionsScreen(
    viewModel: CollectionsViewModel,
    onNavigateToDetail: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = LocalThemeColors.current
    val uiState by viewModel.uiState.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }
    var newDesc by remember { mutableStateOf("") }

    val selected = uiState.selectedCollection

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (selected != null) selected.name.uppercase() else "COLLECTIONS",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = theme.textPrimary, letterSpacing = 1.sp)
            )

            if (selected != null) {
                IconButton(onClick = { viewModel.selectCollection(null) }) {
                    Icon(Icons.Filled.Close, contentDescription = "Close Collection", tint = theme.textSecondary)
                }
            } else {
                IconButton(
                    onClick = { showCreateDialog = true }
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "New Collection", tint = theme.primary)
                }
            }
        }

        if (selected == null) {
            // List of Collections
            if (uiState.collections.isNotEmpty()) {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(uiState.collections, key = { it.id }) { col ->
                        val shape = RoundedCornerShape(12.dp)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(shape)
                                .border(1.dp, theme.borderGlow.copy(alpha = 0.2f), shape)
                                .clickable { viewModel.selectCollection(col) }
                        ) {
                            // Immersive Background Cover
                            if (col.coverUri != null) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(col.coverUri)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Box(modifier = Modifier.fillMaxSize().background(theme.surfaceVariant)) {
                                    Icon(
                                        Icons.Filled.FolderSpecial,
                                        contentDescription = null,
                                        tint = theme.textSecondary.copy(alpha = 0.2f),
                                        modifier = Modifier.size(64.dp).align(Alignment.Center)
                                    )
                                }
                            }

                            // Dark Gradient
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.9f))
                                        )
                                    )
                            )

                            // Content
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = col.name,
                                        style = MaterialTheme.typography.titleLarge.copy(color = Color.White, fontWeight = FontWeight.Bold)
                                    )
                                    if (col.description.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = col.description,
                                            style = MaterialTheme.typography.bodySmall.copy(color = theme.textSecondary)
                                        )
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    IconButton(
                                        onClick = { viewModel.deleteCollection(col) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = Color.White.copy(alpha = 0.6f), modifier = Modifier.size(20.dp))
                                    }
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "${col.wallpaperCount} WALLPAPERS",
                                        style = MaterialTheme.typography.labelSmall.copy(color = theme.primary, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                EmptyStateView(
                    title = "No Collections",
                    description = "Group wallpapers into themed collections like Cyberpunk or Dark AMOLED.",
                    icon = Icons.Filled.FolderSpecial,
                    buttonText = "CREATE COLLECTION",
                    onButtonClick = { showCreateDialog = true },
                    modifier = Modifier.fillMaxSize()
                )
            }
        } else {
            // Selected Collection Wallpapers Grid
            if (uiState.collectionWallpapers.isNotEmpty()) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(uiState.collectionWallpapers, key = { it.id }) { item ->
                        WallpaperCard(
                            wallpaper = item,
                            onClick = { onNavigateToDetail(item.id) },
                            onFavoriteToggle = { }
                        )
                    }
                }
            } else {
                EmptyStateView(
                    title = "Empty Collection",
                    description = "Add wallpapers from the Library tab to populate this collection.",
                    icon = Icons.Filled.PhotoLibrary,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }

    // Create Collection Dialog
    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Create Collection", color = theme.textPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        placeholder = { Text("Collection Name") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = theme.primary,
                            unfocusedBorderColor = theme.borderGlow.copy(alpha = 0.3f),
                            focusedTextColor = theme.textPrimary,
                            unfocusedTextColor = theme.textPrimary
                        )
                    )
                    OutlinedTextField(
                        value = newDesc,
                        onValueChange = { newDesc = it },
                        placeholder = { Text("Description (Optional)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = theme.primary,
                            unfocusedBorderColor = theme.borderGlow.copy(alpha = 0.3f),
                            focusedTextColor = theme.textPrimary,
                            unfocusedTextColor = theme.textPrimary
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.createCollection(newName, newDesc)
                        newName = ""
                        newDesc = ""
                        showCreateDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = theme.primary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("CREATE")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("CANCEL", color = theme.textSecondary)
                }
            },
            containerColor = theme.surface,
            shape = RoundedCornerShape(12.dp)
        )
    }
}
