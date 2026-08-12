package com.akshar.wallpaperengine.shader

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp
import com.akshar.wallpaperengine.theme.LocalThemeColors
import com.akshar.wallpaperengine.theme.ThemeColors
import kotlin.math.*
import kotlin.random.Random

@Composable
fun ShaderBackground(
    modifier: Modifier = Modifier,
    themeColors: ThemeColors = LocalThemeColors.current,
    shaderStyle: String = ShaderStyle.NEBULA.id,
    isEnabled: Boolean = true,
    intensity: String = ShaderIntensity.MEDIUM.id,
    reduceMotion: Boolean = false,
    content: @Composable () -> Unit
) {
    if (!isEnabled) {
        Box(modifier = modifier) {
            content()
        }
        return
    }

    val parsedStyle = ShaderStyle.fromId(shaderStyle)
    val parsedIntensity = ShaderIntensity.fromId(intensity)

    val actualStyle = if (parsedStyle == ShaderStyle.THEME_DEFAULT) {
        when {
            themeColors.primary == Color(0xFFD946EF) -> ShaderStyle.ENERGY_FLOW
            themeColors.primary == Color(0xFFEF4444) -> ShaderStyle.VOID
            themeColors.primary == Color(0xFF818CF8) -> ShaderStyle.AURORA
            !themeColors.isDark -> ShaderStyle.PARTICLES
            else -> ShaderStyle.NEBULA
        }
    } else parsedStyle

    val infiniteTransition = rememberInfiniteTransition(label = "ShaderAnimation")
    val speedFactor = if (reduceMotion) 0.1f else parsedIntensity.multiplier

    val animationProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = (20000 / speedFactor).toInt(), easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Progress"
    )

    val particles = remember {
        List(35) {
            Particle(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                radius = Random.nextFloat() * 3f + 1.5f,
                speed = Random.nextFloat() * 0.15f + 0.05f,
                alpha = Random.nextFloat() * 0.6f + 0.2f
            )
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawBackgroundStyle(
                style = actualStyle,
                progress = animationProgress,
                colors = themeColors,
                particles = particles,
                intensityMultiplier = parsedIntensity.multiplier
            )
        }
        content()
    }
}

private class Particle(
    var x: Float,
    var y: Float,
    val radius: Float,
    val speed: Float,
    val alpha: Float
)

private fun DrawScope.drawBackgroundStyle(
    style: ShaderStyle,
    progress: Float,
    colors: ThemeColors,
    particles: List<Particle>,
    intensityMultiplier: Float
) {
    val width = size.width
    val height = size.height

    // Base background fill
    drawRect(color = colors.background)

    when (style) {
        ShaderStyle.NEBULA -> {
            val cx1 = width * (0.3f + 0.15f * sin(progress * 0.7f))
            val cy1 = height * (0.25f + 0.15f * cos(progress * 0.5f))
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        colors.primary.copy(alpha = 0.08f * intensityMultiplier),
                        colors.secondary.copy(alpha = 0.04f * intensityMultiplier),
                        Color.Transparent
                    ),
                    center = Offset(cx1, cy1),
                    radius = max(width, height) * 0.65f
                )
            )

            val cx2 = width * (0.7f - 0.15f * cos(progress * 0.6f))
            val cy2 = height * (0.75f - 0.15f * sin(progress * 0.8f))
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        colors.primary.copy(alpha = 0.06f * intensityMultiplier),
                        Color.Transparent
                    ),
                    center = Offset(cx2, cy2),
                    radius = max(width, height) * 0.55f
                )
            )
        }

        ShaderStyle.AURORA -> {
            val waveCount = 2
            for (i in 0 until waveCount) {
                val offset = progress + (i * PI.toFloat() / waveCount)
                val yCenter = height * (0.25f + 0.35f * i + 0.08f * sin(offset))
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            colors.primary.copy(alpha = 0.06f * intensityMultiplier),
                            colors.tertiary.copy(alpha = 0.03f * intensityMultiplier),
                            Color.Transparent
                        ),
                        startY = max(0f, yCenter - 180f),
                        endY = min(height, yCenter + 180f)
                    )
                )
            }
        }

        ShaderStyle.CYBER_GRID -> {
            val gridSpacing = 80.dp.toPx()
            val gridAlpha = 0.035f * intensityMultiplier
            val strokeWidth = 1.dp.toPx()

            var x = (progress * 5f) % gridSpacing
            while (x < width) {
                drawLine(
                    color = colors.primary.copy(alpha = gridAlpha),
                    start = Offset(x, 0f),
                    end = Offset(x, height),
                    strokeWidth = strokeWidth
                )
                x += gridSpacing
            }

            var y = (progress * 5f) % gridSpacing
            while (y < height) {
                drawLine(
                    color = colors.primary.copy(alpha = gridAlpha),
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = strokeWidth
                )
                y += gridSpacing
            }
        }

        ShaderStyle.ENERGY_FLOW -> {
            val cx = width / 2f
            val cy = height / 2f
            for (i in 1..3) {
                val r = (sin(progress + i * 0.8f) * 0.5f + 0.5f) * width * 0.4f + 80f
                drawCircle(
                    color = colors.secondary.copy(alpha = (0.04f / i) * intensityMultiplier),
                    center = Offset(cx, cy),
                    radius = r,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5.dp.toPx())
                )
            }
        }

        ShaderStyle.VOID -> {
            val cx = width * (0.5f + 0.03f * sin(progress))
            val cy = height * (0.5f + 0.03f * cos(progress))
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        colors.primary.copy(alpha = 0.07f * intensityMultiplier),
                        Color.Transparent
                    ),
                    center = Offset(cx, cy),
                    radius = max(width, height) * 0.5f
                )
            )
        }

        ShaderStyle.PARTICLES, ShaderStyle.THEME_DEFAULT -> {
            particles.forEach { p ->
                val py = (p.y * height + progress * p.speed * 30f) % height
                val px = (p.x * width + sin(progress + p.y * 10f) * 15f) % width
                drawCircle(
                    color = colors.primary.copy(alpha = p.alpha * 0.25f * intensityMultiplier),
                    center = Offset(px, py),
                    radius = p.radius.dp.toPx()
                )
            }
        }
    }

    // Subtle atmospheric vignette overlay
    drawRect(
        brush = Brush.radialGradient(
            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.35f)),
            center = Offset(width * 0.5f, height * 0.5f),
            radius = max(width, height) * 0.75f
        )
    )
}
