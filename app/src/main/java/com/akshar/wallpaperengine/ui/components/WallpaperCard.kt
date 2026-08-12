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
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WallpaperCard(
    wallpaper: WallpaperEntity,
    isSelected: Boolean = false,
    isMultiSelectMode: Boolean = false,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
    onFavoriteToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = LocalThemeColors.current
    val shape = RoundedCornerShape(12.dp)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(9f / 16f)
            .clip(shape)
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) theme.primary else Color.White.copy(alpha = 0.08f),
                shape = shape
            )
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .testTag("wallpaper_card_${wallpaper.id}"),
        colors = CardDefaults.cardColors(containerColor = theme.surface),
        shape = shape
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

            // Bottom gradient overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                        )
                    )
            )

            // Title & Resolution
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp)
            ) {
                Text(
                    text = wallpaper.title,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${wallpaper.width}×${wallpaper.height}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 10.sp,
                        color = theme.textSecondary
                    )
                )
            }

            // Favorite Button Top-Right
            IconButton(
                onClick = onFavoriteToggle,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .size(36.dp)
                    .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(18.dp))
            ) {
                Icon(
                    imageVector = if (wallpaper.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (wallpaper.isFavorite) Color(0xFFEF4444) else Color.White
                )
            }

            // Selection Checkbox Badge Top-Left
            if (isMultiSelectMode || isSelected) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = "Selected",
                    tint = if (isSelected) theme.primary else Color.White.copy(alpha = 0.5f),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(10.dp)
                        .size(26.dp)
                        .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(13.dp))
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

        when {
            sampleKey.contains("abyss") -> {
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF030008), Color(0xFF1B0033), Color(0xFF070010))
                    )
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFFC084FC).copy(alpha = 0.6f), Color(0xFFA855F7).copy(alpha = 0.2f), Color.Transparent),
                        center = Offset(w * 0.5f, h * 0.4f),
                        radius = w * 0.6f
                    )
                )
                drawCircle(color = Color.White, center = Offset(w * 0.5f, h * 0.4f), radius = w * 0.12f)
            }

            sampleKey.contains("neon") -> {
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF0F001C), Color(0xFF2E004F), Color(0xFF05000A))
                    )
                )
                // Neon skyline
                for (i in 0..8) {
                    val bx = (i * (w / 7))
                    val bh = (h * 0.3f) + (i % 3) * 40f
                    drawRect(
                        color = Color(0xFF1D003B),
                        topLeft = Offset(bx, h - bh),
                        size = androidx.compose.ui.geometry.Size(w / 8, bh)
                    )
                }
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFFF43F5E), Color(0xFFD946EF), Color.Transparent),
                        center = Offset(w * 0.5f, h * 0.35f),
                        radius = w * 0.45f
                    )
                )
            }

            sampleKey.contains("crimson") -> {
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF0D0004), Color(0xFF330010), Color(0xFF050002))
                    )
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFFEF4444).copy(alpha = 0.8f), Color(0xFF991B1B).copy(alpha = 0.3f), Color.Transparent),
                        center = Offset(w * 0.5f, h * 0.45f),
                        radius = w * 0.55f
                    )
                )
            }

            sampleKey.contains("moonlight") -> {
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF020617), Color(0xFF1E1B4B), Color(0xFF090D16))
                    )
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF818CF8), Color(0xFF312E81), Color.Transparent),
                        center = Offset(w * 0.5f, h * 0.3f),
                        radius = w * 0.5f
                    )
                )
                drawCircle(color = Color(0xFFE0E7FF), center = Offset(w * 0.5f, h * 0.3f), radius = w * 0.15f)
            }

            else -> {
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF05000A), Color(0xFF220038), Color(0xFF090014))
                    )
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(theme.primary.copy(alpha = 0.5f), Color.Transparent),
                        center = Offset(w * 0.5f, h * 0.5f),
                        radius = w * 0.6f
                    )
                )
            }
        }
    }
}
