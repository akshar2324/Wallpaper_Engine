package com.akshar.wallpaperengine.data.dna

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class)
class PerceptualHashAnalyzerTest {

    private lateinit var context: Context
    private lateinit var analyzer: PerceptualHashAnalyzer

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        analyzer = PerceptualHashAnalyzer(context)
    }

    @Test
    fun testDHashIdenticalBitmapsHaveZeroDistance() {
        val bitmap1 = Bitmap.createBitmap(32, 32, Bitmap.Config.ARGB_8888)
        val bitmap2 = Bitmap.createBitmap(32, 32, Bitmap.Config.ARGB_8888)

        for (x in 0 until 32) {
            for (y in 0 until 32) {
                val color = if (x % 2 == 0) Color.WHITE else Color.BLACK
                bitmap1.setPixel(x, y, color)
                bitmap2.setPixel(x, y, color)
            }
        }

        val hash1 = analyzer.calculateDHash(bitmap1)
        val hash2 = analyzer.calculateDHash(bitmap2)

        assertEquals(hash1, hash2)
        assertEquals(0, analyzer.hammingDistance(hash1, hash2))
        assertTrue(analyzer.isNearDuplicate(hash1, hash2, 0))
    }

    @Test
    fun testPHashScaleInvariance() {
        val small = Bitmap.createBitmap(32, 32, Bitmap.Config.ARGB_8888)
        val large = Bitmap.createBitmap(128, 128, Bitmap.Config.ARGB_8888)

        // 2D Pattern: quadrant shapes and diagonal gradient
        for (x in 0 until 32) {
            for (y in 0 until 32) {
                val gray = ((x + y) * 255 / 64)
                val isCenter = (x in 10..22 && y in 10..22)
                val color = if (isCenter) Color.WHITE else Color.rgb(gray, gray, gray)
                small.setPixel(x, y, color)
            }
        }
        for (x in 0 until 128) {
            for (y in 0 until 128) {
                val gray = ((x + y) * 255 / 256)
                val isCenter = (x in 40..88 && y in 40..88)
                val color = if (isCenter) Color.WHITE else Color.rgb(gray, gray, gray)
                large.setPixel(x, y, color)
            }
        }

        val hashSmall = analyzer.calculatePHash(small)
        val hashLarge = analyzer.calculatePHash(large)

        val distance = analyzer.hammingDistance(hashSmall, hashLarge)
        assertTrue("Distance should be small for scaled versions of the same image: $distance", distance <= 5)
    }

    @Test
    fun testDistinctImagesHaveHighHammingDistance() {
        val horizontalBars = Bitmap.createBitmap(32, 32, Bitmap.Config.ARGB_8888)
        val verticalBars = Bitmap.createBitmap(32, 32, Bitmap.Config.ARGB_8888)

        for (x in 0 until 32) {
            for (y in 0 until 32) {
                horizontalBars.setPixel(x, y, if (y % 4 == 0) Color.WHITE else Color.BLACK)
                verticalBars.setPixel(x, y, if (x % 4 == 0) Color.WHITE else Color.BLACK)
            }
        }

        val hash1 = analyzer.calculateDHash(horizontalBars)
        val hash2 = analyzer.calculateDHash(verticalBars)

        val distance = analyzer.hammingDistance(hash1, hash2)
        assertTrue("Completely different pattern should yield high hamming distance: $distance", distance > 10)
    }

    @Test
    fun testSampleUriPerceptualHash() {
        val uri1 = Uri.parse("sample_abyss_nebula")
        val uri2 = Uri.parse("sample_neon_cyberpunk")

        val hex1 = analyzer.analyzeUri(uri1)
        val hex2 = analyzer.analyzeUri(uri2)

        assertNotNull(hex1)
        assertNotNull(hex2)
        assertEquals(16, hex1.length)
        assertEquals(16, hex2.length)
        assertNotEquals(hex1, hex2)
    }
}
