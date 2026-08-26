package com.akshar.wallpaperengine.ui.screens

import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.akshar.wallpaperengine.shader.ShaderStyle
import com.akshar.wallpaperengine.theme.LocalThemeColors
import com.akshar.wallpaperengine.wallpaper.LiveWallpaperEngineService
import kotlin.math.max
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShaderStudioScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val theme = LocalThemeColors.current

    var selectedStyle by remember { mutableStateOf(ShaderStyle.NEBULA) }
    var speedMultiplier by remember { mutableFloatStateOf(1.0f) }
    var intensityValue by remember { mutableFloatStateOf(0.8f) }
    var rippleIntensity by remember { mutableFloatStateOf(1.0f) }
    var selectedAccentColor by remember { mutableStateOf(theme.primary) }

    var lastTouchOffset by remember { mutableStateOf<Offset?>(null) }
    var touchTime by remember { mutableFloatStateOf(0f) }

    // Live Animation Clock
    val infiniteTransition = rememberInfiniteTransition(label = "ShaderStudioClock")
    val timeProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = (100000 / max(0.1f, speedMultiplier)).toInt(), easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "TimeProgress"
    )

    val colorOptions = listOf(
        Color(0xFF8B5CF6), // Electric Violet
        Color(0xFF06B6D4), // Cyan
        Color(0xFF10B981), // Emerald
        Color(0xFFD946EF), // Soft Magenta
        Color(0xFFF59E0B), // Amber
        Color(0xFFEF4444)  // Crimson
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = theme.primary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("AGSL Shader Studio", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        speedMultiplier = 1.0f
                        intensityValue = 0.8f
                        rippleIntensity = 1.0f
                        selectedAccentColor = theme.primary
                    }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Reset", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black.copy(alpha = 0.6f))
            )
        },
        containerColor = Color.Black,
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Live Interactive Shader Viewport
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            lastTouchOffset = offset
                            touchTime = timeProgress
                        }
                    }
            ) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    StudioAgslCanvas(
                        style = selectedStyle,
                        time = timeProgress,
                        primaryColor = selectedAccentColor,
                        intensity = intensityValue,
                        lastTouch = lastTouchOffset,
                        touchTime = touchTime
                    )
                } else {
                    StudioFallbackCanvas(
                        primaryColor = selectedAccentColor,
                        intensity = intensityValue,
                        time = timeProgress
                    )
                }
            }

            // Interactive Controls Bottom Drawer
            Surface(
                color = theme.surface.copy(alpha = 0.92f),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Style Selector Chips
                    Text(
                        text = "SHADER STYLE PRESET",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = theme.textSecondary,
                        letterSpacing = 1.sp
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(ShaderStyle.values().filter { it != ShaderStyle.THEME_DEFAULT }) { style ->
                            val isSelected = style == selectedStyle
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedStyle = style },
                                label = { Text(style.displayName, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = theme.primary,
                                    selectedLabelColor = Color.White,
                                    containerColor = theme.surfaceVariant,
                                    labelColor = theme.textSecondary
                                )
                            )
                        }
                    }

                    // Sliders Row: Speed & Intensity
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "SPEED: ${String.format("%.1fx", speedMultiplier)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = theme.textSecondary
                            )
                            Slider(
                                value = speedMultiplier,
                                onValueChange = { speedMultiplier = it },
                                valueRange = 0.2f..3.0f,
                                colors = SliderDefaults.colors(thumbColor = theme.primary, activeTrackColor = theme.primary)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "INTENSITY: ${String.format("%.1f", intensityValue)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = theme.textSecondary
                            )
                            Slider(
                                value = intensityValue,
                                onValueChange = { intensityValue = it },
                                valueRange = 0.1f..1.5f,
                                colors = SliderDefaults.colors(thumbColor = theme.primary, activeTrackColor = theme.primary)
                            )
                        }
                    }

                    // Palette Accent Chips
                    Text(
                        text = "ACCENT CHROMATIC TINT",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = theme.textSecondary
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        colorOptions.forEach { color ->
                            val isSelected = color == selectedAccentColor
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .pointerInput(Unit) {
                                        detectTapGestures { selectedAccentColor = color }
                                    }
                            ) {
                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .clip(CircleShape)
                                            .background(Color.White)
                                            .align(Alignment.Center)
                                    )
                                }
                            }
                        }
                    }

                    // Apply as System Live Wallpaper Button
                    Button(
                        onClick = {
                            launchLiveWallpaperChooser(context)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = theme.primary),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Icon(Icons.Default.Wallpaper, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "SET AS SYSTEM LIVE WALLPAPER",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StudioAgslCanvas(
    style: ShaderStyle,
    time: Float,
    primaryColor: Color,
    intensity: Float,
    lastTouch: Offset?,
    touchTime: Float
) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

    val shaderSource = """
        uniform float2 resolution;
        uniform float time;
        uniform float4 primaryColor;
        uniform float intensity;
        uniform float2 touchPos;
        uniform float touchAge;

        half4 main(in float2 fragCoord) {
            float2 uv = fragCoord.xy / resolution.xy;
            float aspect = resolution.x / resolution.y;
            uv.x *= aspect;

            // Cosmic waves
            float wave1 = sin(uv.x * 4.0 + time * 0.4 + sin(uv.y * 3.0)) * 0.5 + 0.5;
            float wave2 = cos(uv.y * 5.0 - time * 0.3 + cos(uv.x * 2.5)) * 0.5 + 0.5;
            float energy = wave1 * wave2;

            // Touch ripple wave
            float touchDist = length(uv - touchPos * float2(aspect, 1.0));
            float ripple = sin(touchDist * 20.0 - touchAge * 4.0) * exp(-touchDist * 3.0 - touchAge * 0.8);
            energy += clamp(ripple * 0.4, 0.0, 0.5);

            half4 darkBase = half4(0.02, 0.02, 0.04, 1.0);
            half4 color = mix(darkBase, primaryColor, energy * intensity * 0.7);

            return color;
        }
    """.trimIndent()

    val runtimeShader = remember(shaderSource) { android.graphics.RuntimeShader(shaderSource) }

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .drawWithCache {
                runtimeShader.setFloatUniform("resolution", size.width, size.height)
                runtimeShader.setFloatUniform("primaryColor", primaryColor.red, primaryColor.green, primaryColor.blue, 1f)
                runtimeShader.setFloatUniform("intensity", intensity)

                val touchNorm = if (lastTouch != null && size.width > 0 && size.height > 0) {
                    floatArrayOf(lastTouch.x / size.width, lastTouch.y / size.height)
                } else {
                    floatArrayOf(0.5f, 0.5f)
                }
                runtimeShader.setFloatUniform("touchPos", touchNorm[0], touchNorm[1])
                runtimeShader.setFloatUniform("touchAge", (time - touchTime).coerceAtLeast(0f))

                onDrawBehind {
                    runtimeShader.setFloatUniform("time", time * 0.05f)
                    drawRect(
                        brush = ShaderBrush(runtimeShader),
                        size = size
                    )
                }
            }
    ) {}
}

@Composable
fun StudioFallbackCanvas(
    primaryColor: Color,
    intensity: Float,
    time: Float
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val pulse = (sin(time * 0.05) * 0.2 + 0.8).toFloat()

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    primaryColor.copy(alpha = 0.4f * intensity * pulse),
                    Color.Transparent
                ),
                center = Offset(w * 0.5f, h * 0.4f),
                radius = max(w, h) * 0.7f
            )
        )
    }
}

fun launchLiveWallpaperChooser(context: Context) {
    try {
        val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).apply {
            putExtra(
                WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                ComponentName(context, LiveWallpaperEngineService::class.java)
            )
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        try {
            val fallback = Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER)
            context.startActivity(fallback)
        } catch (e2: Exception) {
            Toast.makeText(context, "Could not open Live Wallpaper chooser", Toast.LENGTH_SHORT).show()
        }
    }
}
