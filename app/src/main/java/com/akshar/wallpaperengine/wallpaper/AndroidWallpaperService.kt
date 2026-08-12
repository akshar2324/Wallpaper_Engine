package com.akshar.wallpaperengine.wallpaper

import android.app.WallpaperManager
import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.Log
import com.akshar.wallpaperengine.data.local.entity.WallpaperEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream

class AndroidWallpaperService(private val context: Context) {

    private val wallpaperManager = WallpaperManager.getInstance(context)

    suspend fun applyWallpaper(
        wallpaper: WallpaperEntity,
        targetScreen: String // "HOME", "LOCK", "HOME_AND_LOCK"
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val flag = when (targetScreen.uppercase()) {
                "HOME" -> WallpaperManager.FLAG_SYSTEM
                "LOCK" -> WallpaperManager.FLAG_LOCK
                else -> WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK
            }

            val uri = Uri.parse(wallpaper.uri)
            val inputStream: InputStream? = if (wallpaper.uri.startsWith("asset:///")) {
                val assetPath = wallpaper.uri.removePrefix("asset:///")
                context.assets.open(assetPath)
            } else {
                context.contentResolver.openInputStream(uri)
            }

            if (inputStream == null) return@withContext false

            inputStream.use { stream ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    wallpaperManager.setStream(stream, null, true, flag)
                } else {
                    wallpaperManager.setStream(stream)
                }
            }
            true
        } catch (e: Exception) {
            Log.e("AndroidWallpaperService", "Error applying wallpaper: ${e.message}", e)
            false
        }
    }
}
