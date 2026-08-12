package com.akshar.wallpaperengine.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.akshar.wallpaperengine.data.repository.OrientationFilter
import com.akshar.wallpaperengine.data.repository.SortOrder
import com.akshar.wallpaperengine.theme.LocalThemeColors
import com.akshar.wallpaperengine.ui.components.EmptyStateView
import com.akshar.wallpaperengine.ui.components.WallpaperCard
import com.akshar.wallpaperengine.ui.viewmodel.LibraryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel,
    onNavigateToDetail: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = LocalThemeColors.current
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var showFilterSheet by remember { mutableStateOf(false) }
    var showAddToCollectionDialog by remember { mutableStateOf(false) }
    var showAddToPlaylistDialog by remember { mutableStateOf(false) }
    var showAddTagDialog by remember { mutableStateOf(false) }
    var newTagName by remember { mutableStateOf("") }

    val pickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents(),
        onResult = { uris: List<Uri> ->
            if (uris.isNotEmpty()) {
                viewModel.importImagesFromUris(context, uris)
            }
        }
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "LIBRARY",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = theme.textPrimary, letterSpacing = 1.dp)
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = { showFilterSheet = true }) {
                    Icon(Icons.Filled.FilterList, contentDescription = "Filter", tint = theme.textSecondary)
                }

                IconButton(
                    onClick = { pickerLauncher.launch("image/*") },
                    modifier = Modifier.testTag("import_wallpaper_button")
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Import", tint = theme.primary)
                }
            }
        }

        // Tags Bar
        if (uiState.tags.isNotEmpty()) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
            ) {
                item {
                    FilterChip(
                        selected = uiState.filterOptions.tagId == null,
                        onClick = { viewModel.selectTag(null) },
                        label = { Text("ALL") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = theme.primary.copy(alpha = 0.2f),
                            selectedLabelColor = theme.primary,
                            containerColor = Color.Transparent,
                            labelColor = theme.textSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = theme.borderGlow.copy(alpha = 0.2f),
                            selectedBorderColor = theme.primary
                        )
                    )
                }
                items(uiState.tags, key = { it.id }) { tag ->
                    FilterChip(
                        selected = uiState.filterOptions.tagId == tag.id,
                        onClick = {
                            if (uiState.filterOptions.tagId == tag.id) viewModel.selectTag(null)
                            else viewModel.selectTag(tag.id)
                        },
                        label = { Text(tag.name.uppercase()) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = theme.primary.copy(alpha = 0.2f),
                            selectedLabelColor = theme.primary,
                            containerColor = Color.Transparent,
                            labelColor = theme.textSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = theme.borderGlow.copy(alpha = 0.2f),
                            selectedBorderColor = theme.primary
                        )
                    )
                }
            }
        }

        // Batch Action Toolbar
        AnimatedVisibility(visible = uiState.isMultiSelectMode) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .background(theme.surfaceVariant, RoundedCornerShape(8.dp))
                    .border(1.dp, theme.borderGlow.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${uiState.selectedWallpaperIds.size} SELECTED",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = theme.textPrimary)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = { showAddTagDialog = true }) {
                        Icon(Icons.Filled.Label, contentDescription = "Add Tag", tint = theme.textPrimary)
                    }
                    IconButton(onClick = { showAddToPlaylistDialog = true }) {
                        Icon(Icons.Filled.QueueMusic, contentDescription = "Add to Playlist", tint = theme.textPrimary)
                    }
                    IconButton(onClick = { showAddToCollectionDialog = true }) {
                        Icon(Icons.Filled.FolderSpecial, contentDescription = "Add to Collection", tint = theme.textPrimary)
                    }
                    IconButton(onClick = { viewModel.deleteSelectedWallpapers() }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete Selected", tint = Color(0xFFEF4444))
                    }
                    IconButton(onClick = { viewModel.clearSelection() }) {
                        Icon(Icons.Filled.Close, contentDescription = "Cancel", tint = theme.textSecondary)
                    }
                }
            }
        }

        // Grid Content
        if (uiState.wallpapers.isNotEmpty()) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(uiState.gridDensity),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(uiState.wallpapers, key = { it.id }) { wallpaper ->
                    val isSelected = uiState.selectedWallpaperIds.contains(wallpaper.id)
                    WallpaperCard(
                        wallpaper = wallpaper,
                        isSelected = isSelected,
                        isMultiSelectMode = uiState.isMultiSelectMode,
                        onClick = {
                            if (uiState.isMultiSelectMode) {
                                viewModel.toggleSelection(wallpaper.id)
                            } else {
                                onNavigateToDetail(wallpaper.id)
                            }
                        },
                        onLongClick = { viewModel.toggleSelection(wallpaper.id) },
                        onFavoriteToggle = { viewModel.toggleFavorite(wallpaper) }
                    )
                }
            }
        } else {
            EmptyStateView(
                title = "Library is Empty",
                description = "Import wallpapers to build your collection.",
                icon = Icons.Filled.PhotoLibrary,
                buttonText = "IMPORT IMAGES",
                onButtonClick = { pickerLauncher.launch("image/*") },
                modifier = Modifier.fillMaxSize()
            )
        }
    }

    // Filter Bottom Sheet / Modal
    if (showFilterSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFilterSheet = false },
            containerColor = theme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = "FILTER & SORT",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = theme.textPrimary, letterSpacing = 1.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text("SORT BY", style = MaterialTheme.typography.labelSmall.copy(color = theme.textSecondary, letterSpacing = 1.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SortOrder.values().take(3).forEach { order ->
                        FilterChip(
                            selected = uiState.filterOptions.sortOrder == order,
                            onClick = { viewModel.updateSortOrder(order) },
                            label = { Text(order.name.replace('_', ' ').uppercase()) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = theme.primary.copy(alpha = 0.2f),
                                selectedLabelColor = theme.primary
                            ),
                            border = FilterChipDefaults.filterChipBorder(borderColor = theme.borderGlow.copy(alpha = 0.2f))
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text("ORIENTATION", style = MaterialTheme.typography.labelSmall.copy(color = theme.textSecondary, letterSpacing = 1.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OrientationFilter.values().forEach { orient ->
                        FilterChip(
                            selected = uiState.filterOptions.orientation == orient,
                            onClick = { viewModel.updateOrientation(orient) },
                            label = { Text(orient.name) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = theme.primary.copy(alpha = 0.2f),
                                selectedLabelColor = theme.primary
                            ),
                            border = FilterChipDefaults.filterChipBorder(borderColor = theme.borderGlow.copy(alpha = 0.2f))
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("FAVORITES ONLY", style = MaterialTheme.typography.labelMedium.copy(color = theme.textPrimary, fontWeight = FontWeight.Bold))
                    Switch(
                        checked = uiState.filterOptions.favoritesOnly,
                        onCheckedChange = { viewModel.toggleFavoritesOnly() }
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = { showFilterSheet = false },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = theme.primary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("APPLY FILTERS", style = MaterialTheme.typography.labelLarge.copy(color = theme.onPrimary))
                }
            }
        }
    }

    // Add to Collection Batch Modal
    if (showAddToCollectionDialog) {
        AlertDialog(
            onDismissRequest = { showAddToCollectionDialog = false },
            title = { Text("Add to Collection", style = MaterialTheme.typography.titleLarge.copy(color = theme.textPrimary)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    uiState.collections.forEach { col ->
                        TextButton(
                            onClick = {
                                viewModel.addSelectedToCollection(col.id)
                                showAddToCollectionDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(col.name, color = theme.textPrimary, modifier = Modifier.fillMaxWidth())
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showAddToCollectionDialog = false }) {
                    Text("CANCEL", color = theme.textSecondary)
                }
            },
            containerColor = theme.surface,
            shape = RoundedCornerShape(12.dp)
        )
    }

    // Add to Playlist Batch Modal
    if (showAddToPlaylistDialog) {
        AlertDialog(
            onDismissRequest = { showAddToPlaylistDialog = false },
            title = { Text("Add to Playlist", style = MaterialTheme.typography.titleLarge.copy(color = theme.textPrimary)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    uiState.playlists.forEach { playlist ->
                        TextButton(
                            onClick = {
                                viewModel.addSelectedToPlaylist(playlist.id)
                                showAddToPlaylistDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(playlist.name, color = theme.textPrimary, modifier = Modifier.fillMaxWidth())
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showAddToPlaylistDialog = false }) {
                    Text("CANCEL", color = theme.textSecondary)
                }
            },
            containerColor = theme.surface,
            shape = RoundedCornerShape(12.dp)
        )
    }

    // Add Tag Batch Dialog
    if (showAddTagDialog) {
        AlertDialog(
            onDismissRequest = { showAddTagDialog = false },
            title = { Text("Add Tag to Selected", color = theme.textPrimary) },
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
                        viewModel.addTagToSelected(newTagName)
                        newTagName = ""
                        showAddTagDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = theme.primary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("ADD")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddTagDialog = false }) {
                    Text("CANCEL", color = theme.textSecondary)
                }
            },
            containerColor = theme.surface,
            shape = RoundedCornerShape(12.dp)
        )
    }
}
