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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.akshar.wallpaperengine.data.local.entity.CollectionEntity
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
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (selected != null) selected.name else "Collections",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold, color = theme.textPrimary)
            )

            if (selected != null) {
                IconButton(onClick = { viewModel.selectCollection(null) }) {
                    Icon(Icons.Filled.Close, contentDescription = "Close Collection", tint = theme.textSecondary)
                }
            } else {
                FloatingActionButton(
                    onClick = { showCreateDialog = true },
                    containerColor = theme.primary,
                    contentColor = theme.onPrimary,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "New Collection")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (selected == null) {
            // List of Collections
            if (uiState.collections.isNotEmpty()) {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(uiState.collections, key = { it.id }) { col ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
                                .clickable { viewModel.selectCollection(col) },
                            colors = CardDefaults.cardColors(containerColor = theme.surfaceVariant),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(52.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(theme.surface)
                                            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Filled.FolderSpecial,
                                            contentDescription = null,
                                            tint = theme.primary,
                                            modifier = Modifier.size(26.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column {
                                        Text(
                                            col.name,
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = theme.textPrimary)
                                        )
                                        if (col.description.isNotBlank()) {
                                            Text(
                                                col.description,
                                                style = MaterialTheme.typography.bodySmall.copy(color = theme.textSecondary)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            "${col.wallpaperCount} wallpapers",
                                            style = MaterialTheme.typography.labelSmall.copy(color = theme.primary, fontWeight = FontWeight.Bold)
                                        )
                                    }
                                }

                                IconButton(onClick = { viewModel.deleteCollection(col) }) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = theme.textSecondary)
                                }
                            }
                        }
                    }
                }
            } else {
                EmptyStateView(
                    title = "No Collections Yet",
                    description = "Group wallpapers into curated collections like Cyberpunk Cities or Dark AMOLED.",
                    icon = Icons.Filled.FolderSpecial,
                    buttonText = "Create Collection",
                    onButtonClick = { showCreateDialog = true },
                    modifier = Modifier.fillMaxSize()
                )
            }
        } else {
            // Selected Collection Wallpapers Grid
            if (uiState.collectionWallpapers.isNotEmpty()) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
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
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = newDesc,
                        onValueChange = { newDesc = it },
                        placeholder = { Text("Description (Optional)") }
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
                    colors = ButtonDefaults.buttonColors(containerColor = theme.primary)
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Cancel")
                }
            },
            containerColor = theme.surface
        )
    }
}
