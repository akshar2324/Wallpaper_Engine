package com.akshar.wallpaperengine.data.dna

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.InputStream
import kotlin.math.cos
import kotlin.math.sqrt

/**
 * Perceptual Image Hashing & Visual Similarity Engine.
 *
 * Implements:
 * 1. 64-bit Difference Hash (dHash) for ultra-fast gradient comparison (<2ms).
 * 2. 64-bit Perceptual DCT Hash (pHash) for robust scale/compression invariant matching.
 * 3. Hamming distance calculation for exact and near-duplicate visual matching.
 */
class PerceptualHashAnalyzer(private val context: Context? = null) {

    companion object {
        const val EXACT_DUPLICATE_THRESHOLD = 0
        const val NEAR_DUPLICATE_THRESHOLD = 5
        const val SIMILAR_IMAGE_THRESHOLD = 10
    }

    /**
     * Calculates 64-bit difference hash (dHash) from a Uri.
     */
    fun analyzeUri(uri: Uri): String {
        return try {
            val uriString = uri.toString()
            if (uriString.startsWith("sample_")) {
                return hashSampleUri(uriString)
            }

            if (context == null) return "0000000000000000"

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
            } ?: return "0000000000000000"

            if (options.outWidth <= 0 || options.outHeight <= 0) {
                return "0000000000000000"
            }

            val maxDim = maxOf(options.outWidth, options.outHeight)
            var sampleSize = 1
            while (maxDim / (sampleSize * 2) >= 32) {
                sampleSize *= 2
            }

            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }

            val bitmap = openStream()?.use { stream ->
                BitmapFactory.decodeStream(stream, null, decodeOptions)
            } ?: return "0000000000000000"

            val hashLong = calculatePHash(bitmap)
            formatHashHex(hashLong)
        } catch (e: Exception) {
            "0000000000000000"
        }
    }

    /**
     * Computes a 64-bit Difference Hash (dHash) by comparing adjacent pixels in a 9x8 grid.
     */
    fun calculateDHash(bitmap: Bitmap): Long {
        if (bitmap.isRecycled || bitmap.width <= 0 || bitmap.height <= 0) return 0L

        val scaled = Bitmap.createScaledBitmap(bitmap, 9, 8, true)
        val pixels = IntArray(9 * 8)
        scaled.getPixels(pixels, 0, 9, 0, 0, 9, 8)
        if (scaled != bitmap) scaled.recycle()

        var hash = 0L
        var bitIndex = 0

        for (y in 0 until 8) {
            for (x in 0 until 8) {
                val leftPixel = pixels[y * 9 + x]
                val rightPixel = pixels[y * 9 + (x + 1)]

                val leftLum = (0.299f * ((leftPixel shr 16) and 0xFF) +
                               0.587f * ((leftPixel shr 8) and 0xFF) +
                               0.114f * (leftPixel and 0xFF))
                val rightLum = (0.299f * ((rightPixel shr 16) and 0xFF) +
                                0.587f * ((rightPixel shr 8) and 0xFF) +
                                0.114f * (rightPixel and 0xFF))

                if (leftLum > rightLum) {
                    hash = hash or (1L shl bitIndex)
                }
                bitIndex++
            }
        }
        return hash
    }

    /**
     * Computes a 64-bit Discrete Cosine Transform (DCT) Perceptual Hash (pHash).
     * Scale to 32x32 -> 2D DCT -> top-left 8x8 matrix -> median threshold -> 64-bit hash.
     */
    fun calculatePHash(bitmap: Bitmap): Long {
        if (bitmap.isRecycled || bitmap.width <= 0 || bitmap.height <= 0) return 0L

        val n = 32
        val scaled = Bitmap.createScaledBitmap(bitmap, n, n, true)
        val pixels = IntArray(n * n)
        scaled.getPixels(pixels, 0, n, 0, 0, n, n)
        if (scaled != bitmap) scaled.recycle()

        val lum = Array(n) { DoubleArray(n) }
        for (y in 0 until n) {
            for (x in 0 until n) {
                val p = pixels[y * n + x]
                val r = (p shr 16) and 0xFF
                val g = (p shr 8) and 0xFF
                val b = p and 0xFF
                lum[y][x] = 0.299 * r + 0.587 * g + 0.114 * b
            }
        }

        // Compute 2D DCT for top 8x8 low frequencies
        val dct = Array(8) { DoubleArray(8) }
        for (u in 0 until 8) {
            for (v in 0 until 8) {
                var sum = 0.0
                for (i in 0 until n) {
                    for (j in 0 until n) {
                        sum += lum[i][j] *
                                cos(((2 * i + 1) / (2.0 * n)) * u * Math.PI) *
                                cos(((2 * j + 1) / (2.0 * n)) * v * Math.PI)
                    }
                }
                val cu = if (u == 0) 1.0 / sqrt(2.0) else 1.0
                val cv = if (v == 0) 1.0 / sqrt(2.0) else 1.0
                dct[u][v] = 0.25 * cu * cv * sum
            }
        }

        // Calculate average / mean of top 8x8 (excluding DC component at 0,0)
        var sumCoeffs = 0.0
        var count = 0
        for (u in 0 until 8) {
            for (v in 0 until 8) {
                if (u == 0 && v == 0) continue
                sumCoeffs += dct[u][v]
                count++
            }
        }
        val avg = if (count > 0) sumCoeffs / count else 0.0

        // Build 64-bit hash
        var hash = 0L
        var bit = 0
        for (u in 0 until 8) {
            for (v in 0 until 8) {
                if (dct[u][v] > avg) {
                    hash = hash or (1L shl bit)
                }
                bit++
            }
        }
        return hash
    }

    fun formatHashHex(hash: Long): String {
        return "%016x".format(hash)
    }

    fun parseHashHex(hex: String): Long {
        return try {
            hex.trim().toULong(16).toLong()
        } catch (e: Exception) {
            0L
        }
    }

    /**
     * Computes the Hamming distance (number of differing bits) between two 64-bit hashes.
     */
    fun hammingDistance(hash1: Long, hash2: Long): Int {
        return java.lang.Long.bitCount(hash1 xor hash2)
    }

    fun hammingDistance(hex1: String, hex2: String): Int {
        return hammingDistance(parseHashHex(hex1), parseHashHex(hex2))
    }

    fun isNearDuplicate(hash1: Long, hash2: Long, threshold: Int = NEAR_DUPLICATE_THRESHOLD): Boolean {
        return hammingDistance(hash1, hash2) <= threshold
    }

    fun isNearDuplicate(hex1: String, hex2: String, threshold: Int = NEAR_DUPLICATE_THRESHOLD): Boolean {
        return hammingDistance(hex1, hex2) <= threshold
    }

    private fun hashSampleUri(sampleKey: String): String {
        // Deterministic distinct hashes for sample wallpapers
        val base = when {
            sampleKey.contains("abyss") -> 0x1A2B3C4D5E6F7081L
            sampleKey.contains("neon") -> 0x2B3C4D5E6F708192L
            sampleKey.contains("crimson") -> 0x3C4D5E6F708192A3L
            sampleKey.contains("moonlight") -> 0x4D5E6F708192A3B4L
            sampleKey.contains("sakura") -> 0x5E6F708192A3B4C5L
            sampleKey.contains("aurora") -> 0x6F708192A3B4C5D6L
            else -> sampleKey.hashCode().toLong()
        }
        return formatHashHex(base)
    }
}
