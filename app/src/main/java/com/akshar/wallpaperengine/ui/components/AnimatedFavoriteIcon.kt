package com.akshar.wallpaperengine.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import com.akshar.wallpaperengine.theme.LocalThemeColors

@Composable
fun AnimatedFavoriteIcon(
    isFavorite: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = LocalThemeColors.current

    // Animate scale on favorite toggle
    val scale by animateFloatAsState(
        targetValue = if (isFavorite) 1.2f else 1.0f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 100f),
        label = "favoriteScale"
    )

    Icon(
        imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
        contentDescription = "Favorite",
        tint = if (isFavorite) theme.primary else theme.textSecondary,
        modifier = modifier
            .scale(if (scale > 1.1f && isFavorite) 2.2f - scale else scale) // Heartbeat effect
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null, // Disable default ripple for custom scale effect
                onClick = onToggle
            )
    )
}
