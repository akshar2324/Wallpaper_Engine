package com.akshar.wallpaperengine.data.editor

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Color
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class)
class WallpaperEditorProcessorTest {

    @Test
    fun testApplyEditsProducesValidBitmap() {
        val sourceBitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(sourceBitmap)
        canvas.drawColor(Color.BLUE)

        val params = EditorParameters(
            brightness = 10f,
            contrast = 1.2f,
            saturation = 1.1f,
            vignette = 0.3f,
            oledDarkener = 0.2f,
            cropLeft = 0.1f,
            cropTop = 0.1f,
            cropRight = 0.9f,
            cropBottom = 0.9f
        )

        val result = WallpaperEditorProcessor.applyEditsToBitmap(sourceBitmap, params)

        assertNotNull(result)
        assertEquals(80, result.width)
        assertEquals(80, result.height)
    }

    @Test
    fun testOledDarkenerCrushesDarkPixelsToPureBlack() {
        val sourceBitmap = Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888)
        // Set a very dark grey color: RGB(5, 5, 5)
        val darkGrey = Color.rgb(5, 5, 5)
        for (x in 0 until 10) {
            for (y in 0 until 10) {
                sourceBitmap.setPixel(x, y, darkGrey)
            }
        }

        val params = EditorParameters(
            oledDarkener = 0.5f // High floor threshold
        )

        val result = WallpaperEditorProcessor.applyEditsToBitmap(sourceBitmap, params)
        val pixel = result.getPixel(5, 5)

        assertEquals(0, Color.red(pixel))
        assertEquals(0, Color.green(pixel))
        assertEquals(0, Color.blue(pixel))
    }
}
