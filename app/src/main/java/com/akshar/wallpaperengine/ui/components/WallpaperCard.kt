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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
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
    val shape = RoundedCornerShape(14.dp)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(9f / 16f)
            .clip(shape)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) theme.primary else Color.White.copy(alpha = 0.08f),
                shape = shape
            )
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .testTag("wallpaper_card_${wallpaper.id}"),
        colors = CardDefaults.cardColors(containerColor = theme.cardBackground),
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

            // Preserve the artwork while keeping the title legible on every wallpaper.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(76.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.88f))
                        )
                    )
            )

            // Title
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(10.dp)
            ) {
                Text(
                    text = wallpaper.title,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Medium,
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
                        .padding(10.dp)
                        .size(18.dp)
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

        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(theme.background, theme.surfaceVariant.copy(alpha = 0.88f))
            )
        )

        when {
            sampleKey.contains("abyss") -> {
                drawCircle(Color(0xFF170B36), radius = w * 0.85f, center = Offset(w * 0.5f, h * 0.42f))
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFFC4B5FD), Color(0xFF5B21B6), Color.Transparent),
                        center = Offset(w * 0.5f, h * 0.4f),
                        radius = w * 0.8f
                    )
                )
                drawCircle(Color(0xFF090512), radius = w * 0.25f, center = Offset(w * 0.5f, h * 0.4f))
                repeat(3) { index ->
                    val inset = w * (0.10f + index * 0.08f)
                    drawArc(
                        color = Color(0xFFDDD6FE).copy(alpha = 0.24f - index * 0.05f),
                        startAngle = 218f + index * 20f,
                        sweepAngle = 185f,
                        useCenter = false,
                        topLeft = Offset(inset, h * (0.12f + index * 0.05f)),
                        size = androidx.compose.ui.geometry.Size(w - inset * 2, w - inset * 2),
                        style = Stroke(2.dp.toPx())
                    )
                }
            }
            sampleKey.contains("neon") -> {
                drawCircle(Color(0xFF102C51), radius = w * 0.46f, center = Offset(w * 0.68f, h * 0.26f))
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF22D3EE), Color(0xFF1D4ED8), Color.Transparent),
                        center = Offset(w * 0.68f, h * 0.26f),
                        radius = w * 0.72f
                    )
                )
                repeat(7) { index ->
                    val buildingWidth = w * (0.09f + (index % 3) * 0.035f)
                    val x = index * w * 0.15f
                    val top = h * (0.42f + (index % 4) * 0.06f)
                    drawRect(Color(0xFF07101E), Offset(x, top), androidx.compose.ui.geometry.Size(buildingWidth, h - top))
                    drawLine(Color(0xFF22D3EE).copy(alpha = 0.5f), Offset(x + buildingWidth * 0.5f, top + 12.dp.toPx()), Offset(x + buildingWidth * 0.5f, h * 0.84f), 1.dp.toPx())
                }
            }
            sampleKey.contains("crimson") -> {
                val blade = Path().apply {
                    moveTo(w * 0.12f, h * 0.9f)
                    lineTo(w * 0.9f, h * 0.12f)
                    lineTo(w * 0.82f, h * 0.35f)
                    lineTo(w * 0.26f, h * 0.96f)
                    close()
                }
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFFFB7185), Color(0xFF7F1D1D), Color.Transparent),
                        center = Offset(w * 0.65f, h * 0.28f),
                        radius = w * 0.75f
                    )
                )
                drawPath(blade, Brush.linearGradient(listOf(Color(0xFFFFE4E6), Color(0xFF991B1B))))
                drawLine(Color(0xFFFF7185).copy(alpha = 0.65f), Offset(0f, h * 0.64f), Offset(w, h * 0.35f), 2.dp.toPx())
            }
            sampleKey.contains("moonlight") -> {
                drawCircle(Color(0xFFE0F2FE), radius = w * 0.25f, center = Offset(w * 0.68f, h * 0.27f))
                drawCircle(brush = Brush.radialGradient(listOf(Color(0xFF7DD3FC), Color(0xFF1E3A8A), Color.Transparent), Offset(w * 0.68f, h * 0.30f), w * 0.8f))
                drawArc(Color(0xFFA5F3FC).copy(alpha = 0.55f), 195f, 150f, false, Offset(-w * 0.2f, h * 0.48f), androidx.compose.ui.geometry.Size(w * 1.4f, h * 0.45f), style = Stroke(3.dp.toPx()))
                drawRect(Color(0xFF061225), Offset(0f, h * 0.72f), androidx.compose.ui.geometry.Size(w, h * 0.28f))
            }
            sampleKey.contains("sakura") -> {
                drawCircle(brush = Brush.radialGradient(listOf(Color(0xFFFFB8DB), Color(0xFF7E164F), Color.Transparent), Offset(w * 0.70f, h * 0.28f), w * 0.72f))
                repeat(16) { index ->
                    val x = ((index * 37) % 100) / 100f * w
                    val y = (0.18f + ((index * 19) % 60) / 100f) * h
                    drawCircle(Color(0xFFFFC7E7).copy(alpha = 0.72f), radius = 3.dp.toPx(), center = Offset(x, y))
                }
                drawLine(Color(0xFF40102D), Offset(w * 0.1f, h), Offset(w * 0.78f, h * 0.18f), 7.dp.toPx())
            }
            sampleKey.contains("aurora") -> {
                drawCircle(brush = Brush.radialGradient(listOf(Color(0xFF34D399), Color(0xFF0F766E), Color.Transparent), Offset(w * 0.48f, h * 0.35f), w * 0.9f))
                repeat(3) { index ->
                    val path = Path().apply {
                        moveTo(-w * 0.1f, h * (0.28f + index * 0.13f))
                        quadraticBezierTo(w * 0.35f, h * (0.05f + index * 0.12f), w * 1.08f, h * (0.34f + index * 0.12f))
                    }
                    drawPath(path, Color(0xFFA7F3D0).copy(alpha = 0.45f - index * 0.08f), style = Stroke((10 - index * 2).dp.toPx()))
                }
            }
            else -> {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(theme.primary.copy(alpha = 0.35f), Color.Transparent),
                        center = Offset(w * 0.5f, h * 0.5f),
                        radius = w * 0.6f
                    )
                )
            }
        }
    }
}
