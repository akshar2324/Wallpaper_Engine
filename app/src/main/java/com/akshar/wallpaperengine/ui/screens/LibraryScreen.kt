package com.akshar.wallpaperengine.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.akshar.wallpaperengine.data.repository.OrientationFilter
import com.akshar.wallpaperengine.data.repository.SortOrder
import com.akshar.wallpaperengine.theme.LocalThemeColors
import com.akshar.wallpaperengine.ui.components.EmptyStateView
import com.akshar.wallpaperengine.ui.components.WallpaperCard
import com.akshar.wallpaperengine.ui.viewmodel.LibraryViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
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
    var showImportSheet by remember { mutableStateOf(false) }

    val pickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents(),
        onResult = { uris: List<Uri> ->
            if (uris.isNotEmpty()) {
                viewModel.importImagesFromUris(context, uris)
            }
        }
    )

    val folderLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree(),
        onResult = { treeUri ->
            if (treeUri != null) {
                try {
                    context.contentResolver.takePersistableUriPermission(
                        treeUri,
                        android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION or android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    )
                } catch (_: SecurityException) {
                    // The import falls back to the transient grant when a provider does not support persistence.
                }
                viewModel.importFolderTree(context, treeUri)
            }
        }
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(theme.background)
            .padding(horizontal = 24.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Library",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold, color = theme.textPrimary)
                )
                Text(
                    text = "${uiState.wallpapers.size} wallpapers",
                    style = MaterialTheme.typography.labelMedium.copy(color = theme.textSecondary)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                LibraryActionButton(onClick = { showFilterSheet = true }) {
                    Icon(Icons.Filled.FilterList, contentDescription = "Filter", tint = theme.textSecondary)
                }

                LibraryActionButton(
                    onClick = { showImportSheet = true },
                    modifier = Modifier.testTag("import_wallpaper_button")
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Import", tint = theme.primary)
                }
            }
        }

        // Search Bar
        OutlinedTextField(
            value = uiState.filterOptions.searchQuery,
            onValueChange = { viewModel.updateSearchQuery(it) },
            placeholder = { Text("Search wallpapers, styles, moods...", color = theme.textSecondary, style = MaterialTheme.typography.bodyMedium) },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search", tint = theme.textSecondary) },
            trailingIcon = {
                if (uiState.filterOptions.searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                        Icon(Icons.Filled.Close, contentDescription = "Clear", tint = theme.textSecondary)
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = theme.primary,
                unfocusedBorderColor = Color.White.copy(alpha = 0.10f),
                focusedTextColor = theme.textPrimary,
                unfocusedTextColor = theme.textPrimary,
                focusedContainerColor = theme.surface,
                unfocusedContainerColor = theme.surface
            ),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 52.dp)
                .padding(bottom = 16.dp)
        )

        // Daily discovery controls stay in one rail; detailed sorting lives in the filter sheet.
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp)
        ) {
            item {
                val isAll = !uiState.filterOptions.favoritesOnly &&
                        !uiState.filterOptions.darkOnly &&
                        uiState.filterOptions.minRating == 0f &&
                        uiState.filterOptions.orientation == OrientationFilter.ALL
                FilterChip(
                    selected = isAll,
                    onClick = { viewModel.clearAllFilters() },
                    label = { Text("All") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = theme.primary,
                        selectedLabelColor = theme.onPrimary,
                        containerColor = theme.surface,
                        labelColor = theme.textSecondary
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isAll,
                        borderColor = Color.White.copy(alpha = 0.10f),
                        selectedBorderColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
            }
            item {
                val isFav = uiState.filterOptions.favoritesOnly
                FilterChip(
                    selected = isFav,
                    onClick = { viewModel.toggleFavoritesOnly() },
                    label = { Text("Favorites") },
                    leadingIcon = {
                        Icon(
                            imageVector = if (isFav) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = if (isFav) theme.onPrimary else theme.textSecondary
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = theme.primary,
                        selectedLabelColor = theme.onPrimary,
                        containerColor = theme.surface,
                        labelColor = theme.textSecondary
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isFav,
                        borderColor = Color.White.copy(alpha = 0.10f),
                        selectedBorderColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
            }
            item {
                val isDark = uiState.filterOptions.darkOnly
                FilterChip(
                    selected = isDark,
                    onClick = { viewModel.toggleDarkOnly() },
                    label = { Text("OLED") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.DarkMode,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = if (isDark) theme.onPrimary else theme.textSecondary
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = theme.primary,
                        selectedLabelColor = theme.onPrimary,
                        containerColor = theme.surface,
                        labelColor = theme.textSecondary
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isDark,
                        borderColor = Color.White.copy(alpha = 0.10f),
                        selectedBorderColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
            }
            item {
                val isPortrait = uiState.filterOptions.orientation == OrientationFilter.PORTRAIT
                FilterChip(
                    selected = isPortrait,
                    onClick = { viewModel.updateOrientation(if (isPortrait) OrientationFilter.ALL else OrientationFilter.PORTRAIT) },
                    label = { Text("Portrait") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = theme.primary,
                        selectedLabelColor = theme.onPrimary,
                        containerColor = theme.surface,
                        labelColor = theme.textSecondary
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isPortrait,
                        borderColor = Color.White.copy(alpha = 0.10f),
                        selectedBorderColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        }

        // Batch Action Toolbar
        AnimatedVisibility(visible = uiState.isMultiSelectMode) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .background(theme.surfaceVariant, RoundedCornerShape(12.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.10f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${uiState.selectedWallpaperIds.size} selected",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = theme.textPrimary)
                )

                Row {
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
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(22.dp),
                contentPadding = PaddingValues(bottom = 16.dp),
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
                onButtonClick = { showImportSheet = true },
                modifier = Modifier.fillMaxSize()
            )
        }
    }

    if (showImportSheet) {
        ModalBottomSheet(
            onDismissRequest = { showImportSheet = false },
            containerColor = theme.surface,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Text("Add wallpapers", style = MaterialTheme.typography.titleLarge.copy(color = theme.textPrimary, fontWeight = FontWeight.Medium))
                Text("Import individual images or a complete folder.", style = MaterialTheme.typography.bodySmall.copy(color = theme.textSecondary))
                Spacer(modifier = Modifier.height(20.dp))
                ListItem(
                    headlineContent = { Text("Select images", color = theme.textPrimary) },
                    supportingContent = { Text("Choose one or more files", color = theme.textSecondary) },
                    leadingContent = { Icon(Icons.Filled.PhotoLibrary, contentDescription = null, tint = theme.primary) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            showImportSheet = false
                            pickerLauncher.launch("image/*")
                        }
                )
                ListItem(
                    headlineContent = { Text("Select folder", color = theme.textPrimary) },
                    supportingContent = { Text("Import supported images, including nested folders", color = theme.textSecondary) },
                    leadingContent = { Icon(Icons.Filled.FolderOpen, contentDescription = null, tint = theme.primary) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            showImportSheet = false
                            folderLauncher.launch(null)
                        }
                )
                Spacer(modifier = Modifier.height(18.dp))
            }
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
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Text(
                    text = "FILTER & SORT",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = theme.textPrimary, letterSpacing = 1.sp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Sort Order Selector
                Text("SORT ORDER", style = MaterialTheme.typography.labelSmall.copy(color = theme.textSecondary, letterSpacing = 1.sp))
                Spacer(modifier = Modifier.height(8.dp))
                val sortOptions = listOf(
                    "Recently Added" to SortOrder.RECENTLY_ADDED,
                    "Highest Rated" to SortOrder.RATING_DESC,
                    "Most Liked" to SortOrder.MOST_LIKED,
                    "Most Viewed" to SortOrder.MOST_VIEWED,
                    "Resolution" to SortOrder.HIGHEST_RESOLUTION,
                    "Alphabetical" to SortOrder.NAME_ASC
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    sortOptions.forEach { (label, order) ->
                        val isSelected = uiState.filterOptions.sortOrder == order
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.updateSortOrder(order) },
                            label = { Text(label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = theme.primary.copy(alpha = 0.2f),
                                selectedLabelColor = theme.primary,
                                containerColor = Color.Transparent,
                                labelColor = theme.textSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = theme.borderGlow.copy(alpha = 0.2f),
                                selectedBorderColor = theme.primary
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Minimum Rating Filter
                Text("MINIMUM RATING", style = MaterialTheme.typography.labelSmall.copy(color = theme.textSecondary, letterSpacing = 1.sp))
                Spacer(modifier = Modifier.height(8.dp))
                val ratingOptions = listOf(
                    "All" to 0f,
                    "3★" to 3f,
                    "4★" to 4f,
                    "5★" to 5f
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ratingOptions.forEach { (label, rating) ->
                        val isSelected = if (rating == 0f) uiState.filterOptions.minRating == 0f else uiState.filterOptions.minRating == rating
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.updateMinRating(rating) },
                            label = { Text(label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = theme.primary.copy(alpha = 0.2f),
                                selectedLabelColor = theme.primary,
                                containerColor = Color.Transparent,
                                labelColor = theme.textSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = theme.borderGlow.copy(alpha = 0.2f),
                                selectedBorderColor = theme.primary
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Orientation Filter
                Text("ORIENTATION", style = MaterialTheme.typography.labelSmall.copy(color = theme.textSecondary, letterSpacing = 1.sp))
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OrientationFilter.values().forEach { orient ->
                        val isSelected = uiState.filterOptions.orientation == orient
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.updateOrientation(orient) },
                            label = { Text(orient.name.lowercase().replaceFirstChar { it.uppercase() }) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = theme.primary.copy(alpha = 0.2f),
                                selectedLabelColor = theme.primary,
                                containerColor = Color.Transparent,
                                labelColor = theme.textSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = theme.borderGlow.copy(alpha = 0.2f),
                                selectedBorderColor = theme.primary
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Dark / OLED Only Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("DARK / OLED ONLY", style = MaterialTheme.typography.labelMedium.copy(color = theme.textPrimary, fontWeight = FontWeight.Bold))
                        Text("Show only dark & true-black wallpapers", style = MaterialTheme.typography.bodySmall.copy(color = theme.textSecondary))
                    }
                    Switch(
                        checked = uiState.filterOptions.darkOnly,
                        onCheckedChange = { viewModel.toggleDarkOnly() }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Favorites Only Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("FAVORITES ONLY", style = MaterialTheme.typography.labelMedium.copy(color = theme.textPrimary, fontWeight = FontWeight.Bold))
                        Text("Show only starred wallpapers", style = MaterialTheme.typography.bodySmall.copy(color = theme.textSecondary))
                    }
                    Switch(
                        checked = uiState.filterOptions.favoritesOnly,
                        onCheckedChange = { viewModel.toggleFavoritesOnly() }
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Action Buttons: Reset & Apply
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { viewModel.clearAllFilters() },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, theme.borderGlow.copy(alpha = 0.4f))
                    ) {
                        Icon(Icons.Filled.RestartAlt, contentDescription = null, tint = theme.textSecondary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("RESET", color = theme.textSecondary)
                    }

                    Button(
                        onClick = { showFilterSheet = false },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = theme.primary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("APPLY", style = MaterialTheme.typography.labelLarge.copy(color = theme.onPrimary))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
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
}

@Composable
private fun LibraryActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val theme = LocalThemeColors.current
    IconButton(
        onClick = onClick,
        modifier = modifier
            .size(40.dp)
            .background(theme.surface, RoundedCornerShape(12.dp))
            .border(1.dp, Color.White.copy(alpha = 0.10f), RoundedCornerShape(12.dp))
    ) {
        content()
    }
}
