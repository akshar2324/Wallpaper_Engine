package com.akshar.wallpaperengine.data.ai

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
class WallpaperTagClassifierTest {

    private lateinit var context: Context
    private lateinit var classifier: WallpaperTagClassifier

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        classifier = WallpaperTagClassifier(context)
    }

    @Test
    fun testMonochromeDarkBitmapClassification() {
        val bitmap = Bitmap.createBitmap(32, 32, Bitmap.Config.ARGB_8888)
        bitmap.eraseColor(Color.rgb(20, 20, 20))

        val result = classifier.classifyBitmap(bitmap)

        assertEquals("Monochrome", result.style)
        assertTrue(result.tags.contains("monochrome"))
        assertTrue(result.tags.contains("dark"))
        assertTrue(result.confidence > 0.8f)
    }

    @Test
    fun testNatureGreenBitmapClassification() {
        val bitmap = Bitmap.createBitmap(32, 32, Bitmap.Config.ARGB_8888)
        // Set all pixels to rich forest green (Hue around 120)
        bitmap.eraseColor(Color.rgb(20, 160, 40))

        val result = classifier.classifyBitmap(bitmap)

        assertEquals("Nature", result.style)
        assertTrue(result.tags.contains("nature"))
        assertTrue(result.tags.contains("forest") || result.tags.contains("green"))
    }

    @Test
    fun testCyberpunkBitmapClassification() {
        val bitmap = Bitmap.createBitmap(32, 32, Bitmap.Config.ARGB_8888)
        bitmap.eraseColor(Color.rgb(5, 5, 10)) // Pure dark OLED

        // Add vibrant cyan & magenta neon lines
        for (i in 0 until 32) {
            bitmap.setPixel(i, 8, Color.rgb(0, 240, 255)) // Cyan
            bitmap.setPixel(i, 24, Color.rgb(255, 0, 180)) // Magenta
        }

        val result = classifier.classifyBitmap(bitmap)

        assertEquals("Cyberpunk", result.style)
        assertEquals("Energetic", result.mood)
        assertTrue(result.tags.contains("cyberpunk"))
        assertTrue(result.tags.contains("neon"))
        assertTrue(result.tags.contains("dark"))
    }

    @Test
    fun testSampleUriClassification() {
        val uri = Uri.parse("sample_neon_cyberpunk")
        val result = classifier.classifyUri(uri)

        assertEquals("Cyberpunk", result.style)
        assertEquals("Energetic", result.mood)
        assertTrue(result.tags.contains("neon"))
        assertTrue(result.confidence >= 0.9f)
    }

    @Test
    fun testInvalidUriReturnsFallback() {
        val uri = Uri.parse("invalid_content://missing.jpg")
        val result = classifier.classifyUri(uri)

        assertNotNull(result.style)
        assertNotNull(result.mood)
        assertTrue(result.tags.isNotEmpty())
    }
}
