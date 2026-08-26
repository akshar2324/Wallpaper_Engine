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
class WallpaperDnaAnalyzerTest {

    private lateinit var context: Context
    private lateinit var analyzer: WallpaperDnaAnalyzer

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        analyzer = WallpaperDnaAnalyzer(context)
    }

    @Test
    fun testBrightWhiteBitmapAnalysis() {
        val bitmap = Bitmap.createBitmap(16, 16, Bitmap.Config.ARGB_8888)
        bitmap.eraseColor(Color.WHITE)

        val dna = analyzer.analyzeBitmap(bitmap)

        assertEquals(1.0f, dna.brightness, 0.05f)
        assertFalse(dna.isDark)
        assertNotNull(dna.dominantColor)
    }

    @Test
    fun testDarkBlackBitmapAnalysis() {
        val bitmap = Bitmap.createBitmap(16, 16, Bitmap.Config.ARGB_8888)
        bitmap.eraseColor(Color.BLACK)

        val dna = analyzer.analyzeBitmap(bitmap)

        assertEquals(0.0f, dna.brightness, 0.05f)
        assertTrue(dna.isDark)
        assertNotNull(dna.dominantColor)
    }

    @Test
    fun testDominantAndSecondaryColorExtraction() {
        val bitmap = Bitmap.createBitmap(20, 20, Bitmap.Config.ARGB_8888)
        // 75% Red (300 pixels), 25% Blue (100 pixels)
        for (x in 0 until 20) {
            for (y in 0 until 20) {
                if (y < 15) {
                    bitmap.setPixel(x, y, Color.RED)
                } else {
                    bitmap.setPixel(x, y, Color.BLUE)
                }
            }
        }

        val dna = analyzer.analyzeBitmap(bitmap)

        assertNotNull(dna.dominantColor)
        assertNotNull(dna.secondaryColor)

        val dominant = dna.dominantColor!!
        val secondary = dna.secondaryColor!!

        // Dominant should be reddish: High Red channel, Low Blue/Green
        val dominantRed = (dominant shr 16) and 0xFF
        val dominantBlue = dominant and 0xFF
        assertTrue(dominantRed > 200)
        assertTrue(dominantBlue < 50)

        // Secondary should be bluish: High Blue channel, Low Red/Green
        val secondaryRed = (secondary shr 16) and 0xFF
        val secondaryBlue = secondary and 0xFF
        assertTrue(secondaryBlue > 200)
        assertTrue(secondaryRed < 50)
    }

    @Test
    fun testSampleUriAnalysis() {
        val uri = Uri.parse("sample_abyss_nebula")
        val dna = analyzer.analyzeUri(uri)

        assertNotNull(dna.dominantColor)
        assertNotNull(dna.secondaryColor)
        assertTrue(dna.isDark)
        assertTrue(dna.brightness < 0.45f)
    }

    @Test
    fun testInvalidUriReturnsFallbackDna() {
        val uri = Uri.parse("invalid_content://non_existent_file.png")
        val dna = analyzer.analyzeUri(uri)

        assertNull(dna.dominantColor)
        assertNull(dna.secondaryColor)
        assertEquals(0.5f, dna.brightness, 0.001f)
        assertFalse(dna.isDark)
    }
}
