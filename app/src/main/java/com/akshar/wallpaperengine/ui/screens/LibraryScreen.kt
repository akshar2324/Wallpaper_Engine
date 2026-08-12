package com.akshar.wallpaperengine.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.outlined.FilterList
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
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    var showFilterSheet by remember { mutableStateOf(false) }
    var showAddToCollectionDialog by remember { mutableStateOf(false) }

    // System SAF Photo Picker launcher
    val pickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            viewModel.importImagesFromUris(context, uris)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Search & Filter Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = uiState.filterOptions.searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                placeholder = { Text("Search wallpapers, tags...", color = theme.textSecondary) },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = theme.primary) },
                trailingIcon = {
                    if (uiState.filterOptions.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                            Icon(Icons.Filled.Close, contentDescription = "Clear", tint = theme.textSecondary)
                        }
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("search_text_field"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = theme.primary,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.08f),
                    focusedContainerColor = theme.surfaceVariant,
                    unfocusedContainerColor = theme.surfaceVariant,
                    focusedTextColor = theme.textPrimary,
                    unfocusedTextColor = theme.textPrimary
                ),
                singleLine = true
            )

            IconButton(
                onClick = { showFilterSheet = true },
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(theme.surfaceVariant)
            ) {
                Icon(Icons.Outlined.FilterList, contentDescription = "Filter", tint = theme.primary)
            }

            IconButton(
                onClick = { pickerLauncher.launch("image/*") },
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(theme.primary)
                    .testTag("import_wallpaper_button")
            ) {
                Icon(Icons.Filled.AddPhotoAlternate, contentDescription = "Import", tint = theme.onPrimary)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Quick Tags Filter Bar
        if (uiState.tags.isNotEmpty()) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    FilterChip(
                        selected = uiState.filterOptions.tagId == null,
                        onClick = { viewModel.selectTag(null) },
                        label = { Text("All Tags") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = theme.primary,
                            selectedLabelColor = theme.onPrimary
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
                        label = { Text("#${tag.name}") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = theme.primary,
                            selectedLabelColor = theme.onPrimary
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Batch Action Toolbar
        AnimatedVisibility(visible = uiState.isMultiSelectMode) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = theme.surfaceVariant),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${uiState.selectedWallpaperIds.size} Selected",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = theme.textPrimary)
                    )

                    Row {
                        IconButton(onClick = { showAddToCollectionDialog = true }) {
                            Icon(Icons.Filled.FolderSpecial, contentDescription = "Add to Collection", tint = theme.primary)
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
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Grid Content
        if (uiState.wallpapers.isNotEmpty()) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(uiState.gridDensity),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
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
                title = "No Wallpapers Found",
                description = "Build your personal library by importing high quality anime wallpapers.",
                icon = Icons.Filled.Wallpaper,
                buttonText = "Import Wallpapers",
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
                    .padding(20.dp)
            ) {
                Text(
                    text = "Filter & Sort Library",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = theme.textPrimary)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text("Sorting Order", style = MaterialTheme.typography.labelLarge.copy(color = theme.textSecondary))
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SortOrder.values().take(3).forEach { order ->
                        FilterChip(
                            selected = uiState.filterOptions.sortOrder == order,
                            onClick = { viewModel.updateSortOrder(order) },
                            label = { Text(order.name.replace('_', ' ')) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = theme.primary)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Orientation", style = MaterialTheme.typography.labelLarge.copy(color = theme.textSecondary))
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OrientationFilter.values().forEach { orient ->
                        FilterChip(
                            selected = uiState.filterOptions.orientation == orient,
                            onClick = { viewModel.updateOrientation(orient) },
                            label = { Text(orient.name) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = theme.primary)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Favorites Only", style = MaterialTheme.typography.titleMedium.copy(color = theme.textPrimary))
                    Switch(
                        checked = uiState.filterOptions.favoritesOnly,
                        onCheckedChange = { viewModel.toggleFavoritesOnly() }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { showFilterSheet = false },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = theme.primary)
                ) {
                    Text("Apply Filters")
                }
            }
        }
    }

    // Add to Collection Batch Modal
    if (showAddToCollectionDialog) {
        AlertDialog(
            onDismissRequest = { showAddToCollectionDialog = false },
            title = { Text("Add to Collection", color = theme.textPrimary) },
            text = {
                Column {
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
                    Text("Cancel", color = theme.textSecondary)
                }
            },
            containerColor = theme.surface
        )
    }
}
