package com.akshar.wallpaperengine.data.editor

import android.content.Context
import android.graphics.*
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.math.max

data class EditorParameters(
    val brightness: Float = 0f,           // -100 to 100
    val contrast: Float = 1f,             // 0.5 to 2.0
    val saturation: Float = 1f,           // 0.0 to 2.0
    val vignette: Float = 0f,             // 0.0 to 1.0
    val oledDarkener: Float = 0f,         // 0.0 to 1.0 (crushes dark shadows to pure 0x000000)
    val cropLeft: Float = 0f,             // 0.0 to 1.0
    val cropTop: Float = 0f,              // 0.0 to 1.0
    val cropRight: Float = 1f,            // 0.0 to 1.0
    val cropBottom: Float = 1f            // 0.0 to 1.0
)

object WallpaperEditorProcessor {

    fun applyEditsToBitmap(source: Bitmap, params: EditorParameters): Bitmap {
        // 1. Crop if specified
        val cropL = (params.cropLeft * source.width).toInt().coerceIn(0, source.width - 1)
        val cropT = (params.cropTop * source.height).toInt().coerceIn(0, source.height - 1)
        val cropR = (params.cropRight * source.width).toInt().coerceIn(cropL + 1, source.width)
        val cropB = (params.cropBottom * source.height).toInt().coerceIn(cropT + 1, source.height)

        val cropped = if (cropL > 0 || cropT > 0 || cropR < source.width || cropB < source.height) {
            Bitmap.createBitmap(source, cropL, cropT, cropR - cropL, cropB - cropT)
        } else {
            source
        }

        val result = Bitmap.createBitmap(cropped.width, cropped.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)

        // 2. ColorMatrix for Brightness, Contrast, Saturation
        val cm = ColorMatrix()

        // Saturation
        val satMatrix = ColorMatrix()
        satMatrix.setSaturation(params.saturation)

        // Contrast & Brightness
        // formula: c * (pixel + b) = c * pixel + c * b
        val scale = params.contrast
        val translate = params.brightness + (1f - scale) * 128f / 255f * 255f
        val contrastMatrix = ColorMatrix(
            floatArrayOf(
                scale, 0f, 0f, 0f, translate,
                0f, scale, 0f, 0f, translate,
                0f, 0f, scale, 0f, translate,
                0f, 0f, 0f, 1f, 0f
            )
        )

        cm.postConcat(satMatrix)
        cm.postConcat(contrastMatrix)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        paint.colorFilter = ColorMatrixColorFilter(cm)
        canvas.drawBitmap(cropped, 0f, 0f, paint)

        // 3. Vignette
        if (params.vignette > 0.05f) {
            val w = result.width.toFloat()
            val h = result.height.toFloat()
            val radius = max(w, h) * 0.7f
            val vignettePaint = Paint(Paint.ANTI_ALIAS_FLAG)
            val alphaInt = (params.vignette * 255).toInt().coerceIn(0, 255)
            val vignetteShader = RadialGradient(
                w / 2f, h / 2f, radius,
                intArrayOf(Color.TRANSPARENT, Color.argb(alphaInt, 0, 0, 0)),
                floatArrayOf(0.4f, 1.0f),
                Shader.TileMode.CLAMP
            )
            vignettePaint.shader = vignetteShader
            canvas.drawRect(0f, 0f, w, h, vignettePaint)
        }

        // 4. OLED Darkener (pure black floor crush)
        if (params.oledDarkener > 0.05f) {
            val threshold = (params.oledDarkener * 60).toInt() // Threshold 0..60
            val pixels = IntArray(result.width * result.height)
            result.getPixels(pixels, 0, result.width, 0, 0, result.width, result.height)

            for (i in pixels.indices) {
                val p = pixels[i]
                val r = (p shr 16) and 0xFF
                val g = (p shr 8) and 0xFF
                val b = p and 0xFF
                val lum = (0.299 * r + 0.587 * g + 0.114 * b).toInt()
                if (lum < threshold) {
                    pixels[i] = (p and -0x1000000) // Alpha only, RGB = 0, 0, 0
                }
            }
            result.setPixels(pixels, 0, result.width, 0, 0, result.width, result.height)
        }

        return result
    }

    suspend fun saveEditedWallpaper(
        context: Context,
        editedBitmap: Bitmap,
        baseTitle: String
    ): Pair<Uri, Long> = withContext(Dispatchers.IO) {
        val editsDir = File(context.filesDir, "edited_wallpapers").apply { mkdirs() }
        val filename = "edit_${System.currentTimeMillis()}_${baseTitle.take(15).replace(" ", "_")}.jpg"
        val destFile = File(editsDir, filename)

        FileOutputStream(destFile).use { out ->
            editedBitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
        }

        val uri = Uri.fromFile(destFile)
        Pair(uri, destFile.length())
    }
}
