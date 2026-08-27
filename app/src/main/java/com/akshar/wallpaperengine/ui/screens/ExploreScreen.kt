package com.akshar.wallpaperengine.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.akshar.wallpaperengine.data.remote.RemoteWallpaperItem
import com.akshar.wallpaperengine.theme.LocalThemeColors
import com.akshar.wallpaperengine.ui.viewmodel.ExploreViewModel

@Composable
fun ExploreScreen(viewModel: ExploreViewModel, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val theme = LocalThemeColors.current
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val wallpapers by viewModel.wallpapers.collectAsState()
    val downloadingIds by viewModel.downloadingIds.collectAsState()
    val downloadedIds by viewModel.downloadedIds.collectAsState()

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier.fillMaxSize()
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            Column {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "Explore",
                        style = MaterialTheme.typography.headlineSmall.copy(color = theme.textPrimary, fontWeight = FontWeight.Medium)
                    )
                    IconButton(modifier = Modifier.align(Alignment.CenterEnd), onClick = { }) {
                        Icon(Icons.Filled.Search, contentDescription = "Search explore", tint = theme.textSecondary)
                    }
                }
                Spacer(modifier = Modifier.height(18.dp))
            }
        }

        wallpapers.firstOrNull()?.let { feature ->
            item(span = { GridItemSpan(maxLineSpan) }) {
                FeaturedCollection(feature)
                Spacer(modifier = Modifier.height(20.dp))
            }
        }

        item(span = { GridItemSpan(maxLineSpan) }) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(viewModel.categories.take(4)) { category ->
                    val selected = category.equals(selectedCategory, ignoreCase = true)
                    FilterChip(
                        selected = selected,
                        onClick = { viewModel.selectCategory(category) },
                        label = { Text(category.replaceFirstChar { it.uppercase() }) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = theme.primary,
                            selectedLabelColor = theme.onPrimary,
                            containerColor = theme.surface,
                            labelColor = theme.textSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selected,
                            borderColor = Color.White.copy(alpha = 0.08f),
                            selectedBorderColor = Color.Transparent
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.height(18.dp))
            Text("Trending", style = MaterialTheme.typography.titleMedium.copy(color = theme.textPrimary, fontWeight = FontWeight.Medium))
            Spacer(modifier = Modifier.height(2.dp))
        }

        items(wallpapers, key = { it.id }) { item ->
            RemoteWallpaperCard(
                item = item,
                isDownloading = item.id in downloadingIds,
                isDownloaded = item.id in downloadedIds,
                onDownloadClick = {
                    viewModel.downloadAndImport(item) { success ->
                        Toast.makeText(context, if (success) "Added to Library" else "Download failed", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }
    }
}

@Composable
private fun FeaturedCollection(item: RemoteWallpaperItem) {
    val theme = LocalThemeColors.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(212.dp)
            .clip(RoundedCornerShape(16.dp))
    ) {
        AsyncImage(item.previewUrl, "After dark collection", Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.82f))))
        )
        Column(modifier = Modifier.align(Alignment.BottomStart).padding(16.dp)) {
            Text("After dark", style = MaterialTheme.typography.titleLarge.copy(color = Color.White, fontWeight = FontWeight.Medium))
            Text("A quiet collection for OLED", style = MaterialTheme.typography.bodySmall.copy(color = Color.White.copy(alpha = 0.72f)))
            Spacer(modifier = Modifier.height(6.dp))
            Text("View collection", style = MaterialTheme.typography.labelMedium.copy(color = theme.primary, fontWeight = FontWeight.SemiBold))
        }
    }
}

@Composable
fun RemoteWallpaperCard(item: RemoteWallpaperItem, isDownloading: Boolean, isDownloaded: Boolean, onDownloadClick: () -> Unit) {
    val theme = LocalThemeColors.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(244.dp)
            .clip(RoundedCornerShape(16.dp))
    ) {
        AsyncImage(item.previewUrl, item.title, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))))
                .padding(10.dp)
        ) {
            Text(item.title, style = MaterialTheme.typography.labelMedium.copy(color = Color.White, fontWeight = FontWeight.Medium), maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        IconButton(
            onClick = onDownloadClick,
            enabled = !isDownloading && !isDownloaded,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
                .size(34.dp)
                .background(Color.Black.copy(alpha = 0.46f), CircleShape)
        ) {
            when {
                isDownloading -> androidx.compose.material3.CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color.White)
                isDownloaded -> Icon(Icons.Filled.Check, "Saved", tint = theme.primary, modifier = Modifier.size(17.dp))
                else -> Icon(Icons.Filled.CloudDownload, "Add to library", tint = Color.White, modifier = Modifier.size(17.dp))
            }
        }
    }
}
