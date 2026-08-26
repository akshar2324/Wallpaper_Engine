package com.akshar.wallpaperengine.data.ai

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import java.io.InputStream
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

data class ClassificationResult(
    val style: String,
    val mood: String,
    val tags: List<String>,
    val confidence: Float
)

/**
 * On-Device Heuristic & Visual Feature Wallpaper Classifier.
 * Analyzes color distribution, saturation, edge density, luminance contrast,
 * and OLED purity to classify style, mood, and descriptive tags.
 */
class WallpaperTagClassifier(private val context: Context? = null) {

    fun classifyUri(uri: Uri): ClassificationResult {
        return try {
            val uriString = uri.toString()
            if (uriString.startsWith("sample_")) {
                return classifySampleUri(uriString)
            }

            if (context == null) return fallbackResult()

            fun openStream(): InputStream? {
                return if (uriString.startsWith("asset:///")) {
                    context.assets.open(uriString.removePrefix("asset:///"))
                } else if (uri.scheme == "file" || (uri.scheme == null && uri.path?.startsWith("/") == true)) {
                    val path = uri.path ?: uriString
                    java.io.File(path).inputStream()
                } else {
                    context.contentResolver.openInputStream(uri)
                }
            }

            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            openStream()?.use { stream ->
                BitmapFactory.decodeStream(stream, null, options)
            } ?: return fallbackResult()

            if (options.outWidth <= 0 || options.outHeight <= 0) {
                return fallbackResult()
            }

            val maxDim = maxOf(options.outWidth, options.outHeight)
            var sampleSize = 1
            while (maxDim / (sampleSize * 2) >= 64) {
                sampleSize *= 2
            }

            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }

            val bitmap = openStream()?.use { stream ->
                BitmapFactory.decodeStream(stream, null, decodeOptions)
            } ?: return fallbackResult()

            classifyBitmap(bitmap)
        } catch (e: Exception) {
            fallbackResult()
        }
    }

    fun classifyBitmap(bitmap: Bitmap): ClassificationResult {
        if (bitmap.isRecycled || bitmap.width <= 0 || bitmap.height <= 0) return fallbackResult()

        val n = 32
        val scaled = Bitmap.createScaledBitmap(bitmap, n, n, true)
        val pixels = IntArray(n * n)
        scaled.getPixels(pixels, 0, n, 0, 0, n, n)
        if (scaled != bitmap) scaled.recycle()

        var totalLum = 0.0
        var totalSat = 0.0
        var oledBlackPixels = 0
        var highSatPixels = 0

        val lumGrid = Array(n) { DoubleArray(n) }
        val hsv = FloatArray(3)

        // Hue bins: Red, Orange, Yellow, Green, Cyan, Blue, Purple/Magenta
        var redCount = 0
        var greenCount = 0
        var blueCount = 0
        var cyanCount = 0
        var purpleCount = 0
        var yellowCount = 0

        for (y in 0 until n) {
            for (x in 0 until n) {
                val p = pixels[y * n + x]
                val r = (p shr 16) and 0xFF
                val g = (p shr 8) and 0xFF
                val b = p and 0xFF

                val lum = (0.299 * r + 0.587 * g + 0.114 * b) / 255.0
                lumGrid[y][x] = lum
                totalLum += lum

                if (lum < 0.04) oledBlackPixels++

                Color.RGBToHSV(r, g, b, hsv)
                val hue = hsv[0]
                val sat = hsv[1]
                totalSat += sat

                if (sat > 0.5f) {
                    highSatPixels++
                    when {
                        hue in 0f..20f || hue >= 340f -> redCount++
                        hue in 21f..65f -> yellowCount++
                        hue in 66f..165f -> greenCount++
                        hue in 166f..210f -> cyanCount++
                        hue in 211f..275f -> blueCount++
                        hue in 276f..339f -> purpleCount++
                    }
                }
            }
        }

        val totalPixels = (n * n).toDouble()
        val avgBrightness = (totalLum / totalPixels).toFloat()
        val avgSaturation = (totalSat / totalPixels).toFloat()
        val oledRatio = oledBlackPixels / totalPixels
        val highSatRatio = highSatPixels / totalPixels

        // Edge / gradient density estimation via Sobel operator on 32x32 grid
        var totalGradient = 0.0
        for (y in 1 until n - 1) {
            for (x in 1 until n - 1) {
                val gx = (lumGrid[y - 1][x + 1] + 2 * lumGrid[y][x + 1] + lumGrid[y + 1][x + 1]) -
                         (lumGrid[y - 1][x - 1] + 2 * lumGrid[y][x - 1] + lumGrid[y + 1][x - 1])
                val gy = (lumGrid[y + 1][x - 1] + 2 * lumGrid[y + 1][x] + lumGrid[y + 1][x + 1]) -
                         (lumGrid[y - 1][x - 1] + 2 * lumGrid[y - 1][x] + lumGrid[y - 1][x + 1])
                totalGradient += sqrt(gx * gx + gy * gy)
            }
        }
        val edgeDensity = (totalGradient / ((n - 2) * (n - 2))).toFloat()

        // Deduce Style
        val tags = mutableSetOf<String>()
        val style: String
        val confidence: Float

        when {
            avgSaturation < 0.08f -> {
                style = "Monochrome"
                tags.add("monochrome")
                tags.add("minimal")
                if (avgBrightness < 0.3f) tags.add("dark")
                confidence = 0.90f
            }
            oledRatio > 0.40 && (cyanCount > 15 || purpleCount > 15 || redCount > 15) -> {
                style = "Cyberpunk"
                tags.add("cyberpunk")
                tags.add("neon")
                tags.add("dark")
                tags.add("oled")
                confidence = 0.88f
            }
            oledRatio > 0.45 && edgeDensity < 0.18f -> {
                style = "Minimalist"
                tags.add("minimal")
                tags.add("dark")
                tags.add("oled")
                confidence = 0.85f
            }
            greenCount > 50 || (greenCount > 30 && blueCount > 30) -> {
                style = "Nature"
                tags.add("nature")
                tags.add("landscape")
                if (greenCount > 50) tags.add("forest")
                if (blueCount > 40) tags.add("sky")
                confidence = 0.85f
            }
            avgBrightness < 0.25f && (blueCount > 30 || purpleCount > 30 || cyanCount > 20) -> {
                style = "Sci-Fi"
                tags.add("sci-fi")
                tags.add("space")
                tags.add("dark")
                tags.add("glow")
                confidence = 0.82f
            }
            highSatRatio > 0.45 && edgeDensity > 0.25f -> {
                style = "Anime"
                tags.add("anime")
                tags.add("illustration")
                tags.add("vibrant")
                confidence = 0.80f
            }
            edgeDensity > 0.35f -> {
                style = "Architecture"
                tags.add("architecture")
                tags.add("urban")
                tags.add("geometric")
                confidence = 0.78f
            }
            else -> {
                style = "Abstract"
                tags.add("abstract")
                if (avgBrightness < 0.4f) tags.add("dark")
                if (highSatRatio > 0.3f) tags.add("vibrant")
                confidence = 0.75f
            }
        }

        // Deduce Mood
        val mood: String = when {
            style == "Cyberpunk" -> "Energetic"
            style == "Nature" && avgBrightness > 0.4f -> "Serene"
            style == "Minimalist" -> "Calm"
            style == "Sci-Fi" -> "Ethereal"
            avgBrightness < 0.25f -> "Mysterious"
            highSatRatio > 0.40f -> "Energetic"
            edgeDensity < 0.15f -> "Calm"
            else -> "Serene"
        }

        // Add additional contextual tags
        if (avgBrightness < 0.45f) tags.add("dark")
        if (oledRatio > 0.30) tags.add("oled")
        if (avgSaturation > 0.50f) tags.add("vibrant")
        if (blueCount > 40) tags.add("blue")
        if (redCount > 40) tags.add("red")
        if (purpleCount > 40) tags.add("purple")
        if (cyanCount > 40) tags.add("cyan")
        if (greenCount > 40) tags.add("green")
        tags.add(mood.lowercase())

        return ClassificationResult(
            style = style,
            mood = mood,
            tags = tags.toList().take(8),
            confidence = confidence
        )
    }

    private fun fallbackResult() = ClassificationResult(
        style = "Abstract",
        mood = "Calm",
        tags = listOf("wallpaper", "abstract"),
        confidence = 0.5f
    )

    private fun classifySampleUri(sampleKey: String): ClassificationResult {
        return when {
            sampleKey.contains("abyss") -> ClassificationResult(
                style = "Abstract",
                mood = "Mysterious",
                tags = listOf("abstract", "dark", "oled", "purple", "void", "space", "nebula"),
                confidence = 0.95f
            )
            sampleKey.contains("neon") -> ClassificationResult(
                style = "Cyberpunk",
                mood = "Energetic",
                tags = listOf("cyberpunk", "neon", "tokyo", "city", "dark", "cyan", "vibrant"),
                confidence = 0.95f
            )
            sampleKey.contains("crimson") -> ClassificationResult(
                style = "Anime",
                mood = "Action",
                tags = listOf("anime", "samurai", "crimson", "red", "dark", "illustration"),
                confidence = 0.95f
            )
            sampleKey.contains("moonlight") -> ClassificationResult(
                style = "Nature",
                mood = "Calm",
                tags = listOf("nature", "moonlight", "night", "astral", "blue", "calm", "serene"),
                confidence = 0.95f
            )
            sampleKey.contains("sakura") -> ClassificationResult(
                style = "Cyberpunk",
                mood = "Serene",
                tags = listOf("cyberpunk", "sakura", "pink", "alley", "neon", "vibrant"),
                confidence = 0.95f
            )
            sampleKey.contains("aurora") -> ClassificationResult(
                style = "Sci-Fi",
                mood = "Ethereal",
                tags = listOf("sci-fi", "aurora", "green", "horizon", "ethereal", "glow"),
                confidence = 0.95f
            )
            else -> ClassificationResult(
                style = "Abstract",
                mood = "Calm",
                tags = listOf("abstract", "minimal", "dark"),
                confidence = 0.8f
            )
        }
    }
}
