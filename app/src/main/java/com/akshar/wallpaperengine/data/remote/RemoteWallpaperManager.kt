package com.akshar.wallpaperengine.data.remote

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.akshar.wallpaperengine.data.dna.WallpaperDnaAnalyzer
import com.akshar.wallpaperengine.data.local.dao.WallpaperDao
import com.akshar.wallpaperengine.data.local.entity.WallpaperEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

data class RemoteWallpaperItem(
    val id: String,
    val title: String,
    val previewUrl: String,
    val downloadUrl: String,
    val category: String,
    val style: String,
    val mood: String,
    val isDark: Boolean,
    val author: String
)

object RemoteWallpaperCatalogue {

    fun getCuratedCategories(): List<String> {
        return listOf("ALL", "AMOLED", "CYBERPUNK", "NATURE", "ANIME", "MINIMALIST")
    }

    fun getFeaturedWallpapers(): List<RemoteWallpaperItem> {
        return listOf(
            RemoteWallpaperItem(
                id = "rem_1",
                title = "Neon Skyline 2077",
                previewUrl = "https://images.unsplash.com/photo-1542751371-adc38448a05e?w=400&q=80",
                downloadUrl = "https://images.unsplash.com/photo-1542751371-adc38448a05e?q=80",
                category = "CYBERPUNK",
                style = "Cyberpunk",
                mood = "Energetic",
                isDark = true,
                author = "Unsplash Curated"
            ),
            RemoteWallpaperItem(
                id = "rem_2",
                title = "OLED Obsidian Flow",
                previewUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=400&q=80",
                downloadUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?q=80",
                category = "AMOLED",
                style = "Minimalist",
                mood = "Mysterious",
                isDark = true,
                author = "Curated Drops"
            ),
            RemoteWallpaperItem(
                id = "rem_3",
                title = "Ethereal Alpine Mist",
                previewUrl = "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=400&q=80",
                downloadUrl = "https://images.unsplash.com/photo-1506744038136-46273834b3fb?q=80",
                category = "NATURE",
                style = "Nature",
                mood = "Calm",
                isDark = false,
                author = "Nature Collective"
            ),
            RemoteWallpaperItem(
                id = "rem_4",
                title = "Cosmic Aurora Borealis",
                previewUrl = "https://images.unsplash.com/photo-1517411032315-54ef2cb783bb?w=400&q=80",
                downloadUrl = "https://images.unsplash.com/photo-1517411032315-54ef2cb783bb?q=80",
                category = "AMOLED",
                style = "Abstract",
                mood = "Ethereal",
                isDark = true,
                author = "Night Sky Guild"
            )
        )
    }
}

class RemoteWallpaperManager(
    private val context: Context,
    private val wallpaperDao: WallpaperDao
) {

    suspend fun downloadAndImportWallpaper(item: RemoteWallpaperItem): Result<WallpaperEntity> = withContext(Dispatchers.IO) {
        try {
            val remoteDir = File(context.filesDir, "remote_wallpapers").apply { mkdirs() }
            val destFile = File(remoteDir, "rem_${item.id}_${System.currentTimeMillis()}.jpg")

            val url = URL(item.downloadUrl)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 10000
                readTimeout = 15000
                instanceFollowRedirects = true
            }

            connection.inputStream.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }

            // Decode bounds & DNA profile
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(destFile.absolutePath, options)
            val width = options.outWidth.coerceAtLeast(1080)
            val height = options.outHeight.coerceAtLeast(1920)

            val uri = Uri.fromFile(destFile).toString()
            val dna = WallpaperDnaAnalyzer(context).analyzeUri(Uri.fromFile(destFile))

            val entity = WallpaperEntity(
                uri = uri,
                title = item.title,
                width = width,
                height = height,
                aspectRatio = width.toFloat() / height.toFloat(),
                fileSize = destFile.length(),
                mimeType = "image/jpeg",
                dateAdded = System.currentTimeMillis(),
                dominantColor = dna.dominantColor,
                secondaryColor = dna.secondaryColor,
                brightness = dna.brightness,
                isDark = dna.isDark,
                style = item.style,
                mood = item.mood,
                rating = 4.0f
            )

            val newId = wallpaperDao.insertWallpaper(entity)
            Result.success(entity.copy(id = newId))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
