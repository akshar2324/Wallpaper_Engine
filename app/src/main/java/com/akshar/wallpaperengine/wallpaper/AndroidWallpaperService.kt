package com.akshar.wallpaperengine.wallpaper

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.util.Log
import com.akshar.wallpaperengine.data.local.entity.WallpaperEntity
import com.akshar.wallpaperengine.data.local.entity.WallpaperPositionEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import kotlin.math.max

class AndroidWallpaperService(private val context: Context) {

    private val wallpaperManager = WallpaperManager.getInstance(context)

    suspend fun applyWallpaper(
        wallpaper: WallpaperEntity,
        targetScreen: String, // "HOME", "LOCK", "HOME_AND_LOCK"
        position: WallpaperPositionEntity? = null
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val flag = when (targetScreen.uppercase()) {
                "HOME" -> WallpaperManager.FLAG_SYSTEM
                "LOCK" -> WallpaperManager.FLAG_LOCK
                else -> WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK
            }

            val uri = Uri.parse(wallpaper.uri)

            // If we have custom positioning, we must decode the bitmap, crop/scale it, and then set the bitmap directly
            if (position != null && (position.scale != 1.0f || position.offsetX != 0f || position.offsetY != 0f)) {
                val inputStream = openStream(uri) ?: return@withContext false
                inputStream.use { stream ->
                    val originalBitmap = BitmapFactory.decodeStream(stream) ?: return@withContext false

                    // Simple center-crop logic with scale and offset simulation
                    // Note: True pan/zoom cropping requires taking screen dimensions into account.
                    // This is a basic implementation of the crop.
                    val targetWidth = (originalBitmap.width / position.scale).toInt().coerceAtMost(originalBitmap.width)
                    val targetHeight = (originalBitmap.height / position.scale).toInt().coerceAtMost(originalBitmap.height)

                    // offsetX and offsetY are usually between -1.0 and 1.0 depending on the UI implementation.
                    // Assuming they represent normalized translation from the center.
                    var cropX = (originalBitmap.width - targetWidth) / 2 - (position.offsetX * originalBitmap.width).toInt()
                    var cropY = (originalBitmap.height - targetHeight) / 2 - (position.offsetY * originalBitmap.height).toInt()

                    // Clamp bounds
                    cropX = cropX.coerceIn(0, originalBitmap.width - targetWidth)
                    cropY = cropY.coerceIn(0, originalBitmap.height - targetHeight)

                    val croppedBitmap = Bitmap.createBitmap(originalBitmap, cropX, cropY, targetWidth, targetHeight)

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        wallpaperManager.setBitmap(croppedBitmap, null, true, flag)
                    } else {
                        wallpaperManager.setBitmap(croppedBitmap)
                    }

                    // Recycle
                    if (originalBitmap != croppedBitmap) {
                        originalBitmap.recycle()
                    }
                    croppedBitmap.recycle()
                }
            } else {
                // Fast path for unmodified wallpapers
                val inputStream = openStream(uri) ?: return@withContext false
                inputStream.use { stream ->
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        wallpaperManager.setStream(stream, null, true, flag)
                    } else {
                        wallpaperManager.setStream(stream)
                    }
                }
            }
            true
        } catch (e: Exception) {
            Log.e("AndroidWallpaperService", "Error applying wallpaper: ${e.message}", e)
            false
        }
    }

    private fun openStream(uri: Uri): InputStream? {
        return if (uri.toString().startsWith("asset:///")) {
            val assetPath = uri.toString().removePrefix("asset:///")
            context.assets.open(assetPath)
        } else {
            context.contentResolver.openInputStream(uri)
        }
    }
}
