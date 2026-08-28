package com.akshar.wallpaperengine.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.akshar.wallpaperengine.data.remote.RemoteWallpaperItem
import com.akshar.wallpaperengine.theme.LocalThemeColors
import com.akshar.wallpaperengine.ui.viewmodel.ExploreViewModel
import com.akshar.wallpaperengine.ui.components.AuraHeader

@Composable
fun ExploreScreen(viewModel: ExploreViewModel, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val theme = LocalThemeColors.current
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val wallpapers by viewModel.wallpapers.collectAsState()
    val downloadingIds by viewModel.downloadingIds.collectAsState()
    val downloadedIds by viewModel.downloadedIds.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(theme.background)
            .padding(horizontal = 24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            AuraHeader(
                title = "Explore",
                subtitle = "Curated collections",
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { }) {
                Icon(Icons.Filled.Search, contentDescription = "Search explore", tint = theme.textSecondary)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.fillMaxSize().padding(bottom = 80.dp)
        ) {
            wallpapers.firstOrNull()?.let { feature ->
                item(span = { GridItemSpan(maxLineSpan) }) {
                    FeaturedCollection(feature)
                }
            }

            item(span = { GridItemSpan(maxLineSpan) }) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(vertical = 12.dp)
                ) {
                    items(viewModel.categories.take(4)) { category ->
                        val selected = category.equals(selectedCategory, ignoreCase = true)
                        FilterChip(
                            selected = selected,
                            onClick = { viewModel.selectCategory(category) },
                            label = { Text(category.replaceFirstChar { it.uppercase() }) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = theme.primary,
                                selectedLabelColor = theme.onPrimary,
                                containerColor = theme.surfaceVariant,
                                labelColor = theme.textSecondary
                            ),
                            border = null,
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }
            }

            items(wallpapers, key = { it.id }) { item ->
                AuraRemoteWallpaperCard(
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
}

@Composable
private fun FeaturedCollection(item: RemoteWallpaperItem) {
    val theme = LocalThemeColors.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
            .clip(RoundedCornerShape(12.dp))
    ) {
        AsyncImage(item.previewUrl, "After dark collection", Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.9f))))
        )
        Column(modifier = Modifier.align(Alignment.BottomStart).padding(20.dp)) {
            Text("After dark", style = MaterialTheme.typography.headlineSmall.copy(color = Color.White, fontWeight = FontWeight.Medium))
            Spacer(modifier = Modifier.height(4.dp))
            Text("A quiet collection for OLED", style = MaterialTheme.typography.bodyMedium.copy(color = Color.White.copy(alpha = 0.7f)))
            Spacer(modifier = Modifier.height(16.dp))
            Text("View collection", style = MaterialTheme.typography.labelMedium.copy(color = theme.primary, fontWeight = FontWeight.Bold))
        }
    }
}

@Composable
fun AuraRemoteWallpaperCard(
    item: RemoteWallpaperItem,
    isDownloading: Boolean,
    isDownloaded: Boolean,
    onDownloadClick: () -> Unit
) {
    val theme = LocalThemeColors.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = { /* Detail */ })
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.75f)
                .clip(RoundedCornerShape(12.dp))
                .background(theme.surfaceVariant)
        ) {
            AsyncImage(item.previewUrl, item.title, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)

            IconButton(
                onClick = onDownloadClick,
                enabled = !isDownloading && !isDownloaded,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .size(36.dp)
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
            ) {
                when {
                    isDownloading -> CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = theme.primary)
                    isDownloaded -> Icon(Icons.Filled.Check, "Saved", tint = theme.primary, modifier = Modifier.size(20.dp))
                    else -> Icon(Icons.Filled.CloudDownload, "Add to library", tint = Color.White, modifier = Modifier.size(20.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = item.title,
            style = MaterialTheme.typography.titleMedium.copy(
                color = theme.textPrimary,
                fontWeight = FontWeight.Medium
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = item.author,
            style = MaterialTheme.typography.bodySmall.copy(color = theme.textSecondary),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
