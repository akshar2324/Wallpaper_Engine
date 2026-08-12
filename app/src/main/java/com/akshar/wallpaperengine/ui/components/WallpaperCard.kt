package com.akshar.wallpaperengine.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
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
import com.akshar.wallpaperengine.data.local.entity.WallpaperEntity
import com.akshar.wallpaperengine.theme.LocalThemeColors

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WallpaperCard(
    wallpaper: WallpaperEntity,
    isSelected: Boolean = false,
    isMultiSelectMode: Boolean = false,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
    onFavoriteToggle: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val theme = LocalThemeColors.current
    val shape = RoundedCornerShape(8.dp)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(9f / 16f)
            .clip(shape)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) theme.primary else theme.borderGlow.copy(alpha = 0.3f),
                shape = shape
            )
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .testTag("wallpaper_card_${wallpaper.id}"),
        colors = CardDefaults.cardColors(containerColor = theme.background),
        shape = shape,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (wallpaper.isSample || wallpaper.uri.startsWith("sample_")) {
                ProceduralWallpaperPreview(
                    sampleKey = wallpaper.uri,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(wallpaper.uri)
                        .size(400) // Downsample to prevent excessive memory usage
                        .crossfade(true)
                        .build(),
                    contentDescription = wallpaper.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Bottom gradient overlay - very subtle
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.9f))
                        )
                    )
            )

            // Title
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp)
            ) {
                Text(
                    text = wallpaper.title,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Favorite Button Top-Right
            if (wallpaper.isFavorite) {
                Icon(
                    imageVector = Icons.Filled.Favorite,
                    contentDescription = "Favorite",
                    tint = theme.primary,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(16.dp)
                )
            }

            // Selection Checkbox Badge Top-Left
            if (isMultiSelectMode || isSelected) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = "Selected",
                    tint = if (isSelected) theme.primary else Color.White.copy(alpha = 0.3f),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                        .size(20.dp)
                )
            }
        }
    }
}

@Composable
fun ProceduralWallpaperPreview(
    sampleKey: String,
    modifier: Modifier = Modifier
) {
    val theme = LocalThemeColors.current

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        drawRect(color = theme.background)

        when {
            sampleKey.contains("abyss") -> {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(theme.primary.copy(alpha = 0.2f), Color.Transparent),
                        center = Offset(w * 0.5f, h * 0.4f),
                        radius = w * 0.8f
                    )
                )
            }
            sampleKey.contains("neon") -> {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(theme.tertiary.copy(alpha = 0.2f), Color.Transparent),
                        center = Offset(w * 0.5f, h * 0.35f),
                        radius = w * 0.6f
                    )
                )
            }
            sampleKey.contains("crimson") -> {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(theme.primary.copy(alpha = 0.3f), Color.Transparent),
                        center = Offset(w * 0.5f, h * 0.45f),
                        radius = w * 0.6f
                    )
                )
            }
            else -> {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(theme.primary.copy(alpha = 0.1f), Color.Transparent),
                        center = Offset(w * 0.5f, h * 0.5f),
                        radius = w * 0.6f
                    )
                )
            }
        }
    }
}
