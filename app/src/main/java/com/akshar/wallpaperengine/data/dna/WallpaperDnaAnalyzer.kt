package com.akshar.wallpaperengine.data.dna

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.InputStream
import kotlin.math.sqrt

data class WallpaperDna(
    val dominantColor: Int?,
    val secondaryColor: Int?,
    val brightness: Float,
    val isDark: Boolean
)

class WallpaperDnaAnalyzer(private val context: Context) {

    fun analyzeUri(uri: Uri): WallpaperDna {
        return try {
            val uriString = uri.toString()
            if (uriString.startsWith("sample_")) {
                return analyzeSampleUri(uriString)
            }

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

            // 1. Decode bounds
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            openStream()?.use { stream ->
                BitmapFactory.decodeStream(stream, null, options)
            } ?: return WallpaperDna(null, null, 0.5f, false)

            if (options.outWidth <= 0 || options.outHeight <= 0) {
                return WallpaperDna(null, null, 0.5f, false)
            }

            // 2. Downsample efficiently to max 64x64 using inSampleSize
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
            } ?: return WallpaperDna(null, null, 0.5f, false)

            analyzeBitmap(bitmap)
        } catch (e: Exception) {
            WallpaperDna(null, null, 0.5f, false)
        }
    }

    fun analyzeBitmap(bitmap: Bitmap): WallpaperDna {
        return try {
            if (bitmap.isRecycled || bitmap.width <= 0 || bitmap.height <= 0) {
                return WallpaperDna(null, null, 0.5f, false)
            }

            // Scale to max 64x64 if necessary
            val maxDim = maxOf(bitmap.width, bitmap.height)
            val scaledBitmap = if (maxDim > 64) {
                val scale = 64f / maxDim
                val targetW = maxOf(1, (bitmap.width * scale).toInt())
                val targetH = maxOf(1, (bitmap.height * scale).toInt())
                Bitmap.createScaledBitmap(bitmap, targetW, targetH, true)
            } else {
                bitmap
            }

            val width = scaledBitmap.width
            val height = scaledBitmap.height
            val pixels = IntArray(width * height)
            scaledBitmap.getPixels(pixels, 0, width, 0, 0, width, height)

            if (scaledBitmap != bitmap) {
                scaledBitmap.recycle()
            }

            if (pixels.isEmpty()) {
                return WallpaperDna(null, null, 0.5f, false)
            }

            var totalLuminance = 0.0
            var validPixelCount = 0
            val histogram = HashMap<Int, Int>()

            for (pixel in pixels) {
                val alpha = (pixel ushr 24) and 0xFF
                if (alpha < 32) continue // Skip transparent/invisible pixels

                val r = (pixel shr 16) and 0xFF
                val g = (pixel shr 8) and 0xFF
                val b = pixel and 0xFF

                // Standard luminance formula: (0.299 * R + 0.587 * G + 0.114 * B) / 255.0f
                val lum = (0.299f * r + 0.587f * g + 0.114f * b) / 255.0f
                totalLuminance += lum
                validPixelCount++

                // Quantize 5 bits per channel (32 levels mapped back to 0..255)
                val qr = ((r shr 3) * 255) / 31
                val qg = ((g shr 3) * 255) / 31
                val qb = ((b shr 3) * 255) / 31
                val quantized = (0xFF shl 24) or (qr shl 16) or (qg shl 8) or qb

                histogram[quantized] = (histogram[quantized] ?: 0) + 1
            }

            val brightness = if (validPixelCount > 0) {
                (totalLuminance / validPixelCount).toFloat().coerceIn(0.0f, 1.0f)
            } else {
                0.5f
            }

            val isDark = brightness < 0.45f

            val sortedEntries = histogram.entries.sortedByDescending { it.value }
            val dominantColor = sortedEntries.firstOrNull()?.key
            val secondaryColor = if (dominantColor != null) {
                sortedEntries.firstOrNull { entry ->
                    colorDistance(entry.key, dominantColor) > 40.0
                }?.key ?: sortedEntries.getOrNull(1)?.key
            } else {
                null
            }

            WallpaperDna(
                dominantColor = dominantColor,
                secondaryColor = secondaryColor,
                brightness = brightness,
                isDark = isDark
            )
        } catch (e: Exception) {
            WallpaperDna(null, null, 0.5f, false)
        }
    }

    private fun colorDistance(c1: Int, c2: Int): Double {
        val r1 = (c1 shr 16) and 0xFF
        val g1 = (c1 shr 8) and 0xFF
        val b1 = c1 and 0xFF
        val r2 = (c2 shr 16) and 0xFF
        val g2 = (c2 shr 8) and 0xFF
        val b2 = c2 and 0xFF
        val dr = r1 - r2
        val dg = g1 - g2
        val db = b1 - b2
        return sqrt((dr * dr + dg * dg + db * db).toDouble())
    }

    private fun analyzeSampleUri(sampleKey: String): WallpaperDna {
        return when {
            sampleKey.contains("abyss") -> WallpaperDna(
                dominantColor = 0xFF0D0B18.toInt(),
                secondaryColor = 0xFF7C4DFF.toInt(),
                brightness = 0.15f,
                isDark = true
            )
            sampleKey.contains("neon") -> WallpaperDna(
                dominantColor = 0xFF0A0E1A.toInt(),
                secondaryColor = 0xFF00E5FF.toInt(),
                brightness = 0.25f,
                isDark = true
            )
            sampleKey.contains("crimson") -> WallpaperDna(
                dominantColor = 0xFF1A0505.toInt(),
                secondaryColor = 0xFFFF1744.toInt(),
                brightness = 0.20f,
                isDark = true
            )
            sampleKey.contains("moonlight") -> WallpaperDna(
                dominantColor = 0xFF080C14.toInt(),
                secondaryColor = 0xFF80D8FF.toInt(),
                brightness = 0.18f,
                isDark = true
            )
            sampleKey.contains("sakura") -> WallpaperDna(
                dominantColor = 0xFF140810.toInt(),
                secondaryColor = 0xFFFF4081.toInt(),
                brightness = 0.22f,
                isDark = true
            )
            sampleKey.contains("aurora") -> WallpaperDna(
                dominantColor = 0xFF051410.toInt(),
                secondaryColor = 0xFF00E676.toInt(),
                brightness = 0.20f,
                isDark = true
            )
            else -> WallpaperDna(
                dominantColor = 0xFF121212.toInt(),
                secondaryColor = 0xFFBB86FC.toInt(),
                brightness = 0.20f,
                isDark = true
            )
        }
    }
}
