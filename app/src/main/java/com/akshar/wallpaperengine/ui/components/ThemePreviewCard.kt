package com.akshar.wallpaperengine.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.akshar.wallpaperengine.theme.AppThemeId
import com.akshar.wallpaperengine.theme.LocalThemeColors
import com.akshar.wallpaperengine.theme.ThemePalette

@Composable
fun ThemePreviewCard(
    themeId: AppThemeId,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentTheme = LocalThemeColors.current
    val previewColors = ThemePalette.getThemeColors(themeId.id)
    val shape = RoundedCornerShape(12.dp)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) currentTheme.primary else currentTheme.borderGlow,
                shape = shape
            )
            .clickable(onClick = onSelect),
        colors = CardDefaults.cardColors(containerColor = previewColors.background),
        shape = shape
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Miniature UI preview
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(previewColors.surface)
                    .border(1.dp, previewColors.borderGlow, RoundedCornerShape(8.dp))
            ) {
                // Mini accent header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .background(previewColors.primary.copy(alpha = 0.2f))
                )
                // Mini accent line
                Box(
                    modifier = Modifier
                        .padding(start = 8.dp, top = 20.dp)
                        .width(20.dp)
                        .height(4.dp)
                        .background(previewColors.primary)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = themeId.displayName,
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = previewColors.textPrimary,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = themeId.description,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = previewColors.textSecondary
                    )
                )
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = "Selected Theme",
                    tint = currentTheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
