package com.akshar.wallpaperengine.shader

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp
import com.akshar.wallpaperengine.theme.LocalThemeColors
import com.akshar.wallpaperengine.theme.ThemeColors
import kotlin.math.*
import kotlin.random.Random

// Add simple AGSL Shaders for Android 13+ GPU processing.
// Fallback to static or highly simplified Canvas rendering for older APIs.

@Composable
fun ShaderBackground(
    modifier: Modifier = Modifier,
    themeColors: ThemeColors = LocalThemeColors.current,
    shaderStyle: String = ShaderStyle.NEBULA.id,
    isEnabled: Boolean = true,
    intensity: String = ShaderIntensity.LOW.id, // Default to low intensity as requested
    reduceMotion: Boolean = false,
    content: @Composable () -> Unit
) {
    if (!isEnabled) {
        Box(modifier = modifier.background(themeColors.background)) {
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
    val speedFactor = if (reduceMotion) 0.05f else parsedIntensity.multiplier * 0.5f // Extremely slow and atmospheric

    val animationProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = (40000 / speedFactor).toInt(), easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Progress"
    )

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !reduceMotion) {
        AGSLShaderBackground(
            modifier = modifier,
            style = actualStyle,
            progress = animationProgress,
            themeColors = themeColors,
            intensityMultiplier = parsedIntensity.multiplier * 0.5f, // Halve the intensity for subtlety
            content = content
        )
    } else {
        FallbackShaderBackground(
            modifier = modifier,
            style = actualStyle,
            progress = animationProgress,
            themeColors = themeColors,
            intensityMultiplier = parsedIntensity.multiplier * 0.5f,
            content = content
        )
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun AGSLShaderBackground(
    modifier: Modifier,
    style: ShaderStyle,
    progress: Float,
    themeColors: ThemeColors,
    intensityMultiplier: Float,
    content: @Composable () -> Unit
) {
    // AGSL Shader Definition - extremely subtle atmospheric depth
    val shaderSource = """
        uniform float2 resolution;
        uniform float time;
        uniform float4 primaryColor;
        uniform float4 bgColor;
        uniform float intensity;

        half4 main(in float2 fragCoord) {
            float2 uv = fragCoord.xy / resolution.xy;
            float aspect = resolution.x / resolution.y;
            uv.x *= aspect;

            // Atmospheric noise/depth
            float d = length(uv - float2(0.5 * aspect, 0.2));
            float colorAmt = smoothstep(1.0, 0.0, d) * 0.15 * intensity;

            // Extremely slow moving soft light
            float moveX = sin(time * 0.2 + uv.y * 2.0) * 0.2;
            float moveY = cos(time * 0.15 + uv.x * 1.5) * 0.2;

            float d2 = length(uv - float2(0.5 * aspect + moveX, 0.6 + moveY));
            colorAmt += smoothstep(0.8, 0.0, d2) * 0.2 * intensity;

            half4 finalColor = mix(bgColor, primaryColor, colorAmt);
            return finalColor;
        }
    """.trimIndent()

    val runtimeShader = remember(shaderSource) { android.graphics.RuntimeShader(shaderSource) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .drawWithCache {
                runtimeShader.setFloatUniform("resolution", size.width, size.height)
                runtimeShader.setFloatUniform("primaryColor", themeColors.primary.red, themeColors.primary.green, themeColors.primary.blue, themeColors.primary.alpha)
                runtimeShader.setFloatUniform("bgColor", themeColors.background.red, themeColors.background.green, themeColors.background.blue, themeColors.background.alpha)
                runtimeShader.setFloatUniform("intensity", intensityMultiplier)
                onDrawBehind {
                    runtimeShader.setFloatUniform("time", progress)
                    drawRect(
                        brush = ShaderBrush(runtimeShader),
                        size = size
                    )
                }
            }
    ) {
        content()
    }
}

@Composable
fun FallbackShaderBackground(
    modifier: Modifier,
    style: ShaderStyle,
    progress: Float,
    themeColors: ThemeColors,
    intensityMultiplier: Float,
    content: @Composable () -> Unit
) {
    // Static fallback to save battery on older devices
    Box(modifier = modifier
        .fillMaxSize()
        .background(themeColors.background)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // Just draw some static gradients to look decent but save battery
            val cx = width * 0.5f
            val cy = height * 0.3f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        themeColors.primary.copy(alpha = 0.05f * intensityMultiplier),
                        Color.Transparent
                    ),
                    center = Offset(cx, cy),
                    radius = max(width, height) * 0.8f
                )
            )
        }
        content()
    }
}
