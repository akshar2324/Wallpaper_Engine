package com.akshar.wallpaperengine.data.maintenance

import android.content.Context
import android.net.Uri
import com.akshar.wallpaperengine.data.local.dao.WallpaperDao
import com.akshar.wallpaperengine.data.local.entity.WallpaperEntity
import com.akshar.wallpaperengine.data.repository.WallpaperRepository
import java.io.File
import java.io.InputStream

data class LibraryHealthReport(
    val totalWallpapers: Int,
    val totalStorageBytes: Long,
    val brokenWallpapers: List<WallpaperEntity>,
    val lowResWallpapers: List<WallpaperEntity>,
    val unusedWallpapers: List<WallpaperEntity>,
    val duplicateClusters: List<List<WallpaperEntity>>
)

class LibraryHealthManager(
    private val context: Context,
    private val wallpaperDao: WallpaperDao,
    private val wallpaperRepository: WallpaperRepository
) {

    suspend fun scanLibraryHealth(): LibraryHealthReport {
        val allWallpapers = wallpaperDao.getAllWallpapersList()
        var totalBytes = 0L

        val brokenList = mutableListOf<WallpaperEntity>()
        val lowResList = mutableListOf<WallpaperEntity>()
        val unusedList = mutableListOf<WallpaperEntity>()

        val thirtyDaysAgo = System.currentTimeMillis() - (30L * 24L * 60L * 60L * 1000L)

        for (wallpaper in allWallpapers) {
            totalBytes += wallpaper.fileSize

            // Check if URI is accessible / broken
            if (!isUriAccessible(wallpaper.uri, wallpaper.isSample)) {
                brokenList.add(wallpaper)
            }

            // Low resolution check (< 1080x1080)
            if (wallpaper.width < 1080 || wallpaper.height < 1080) {
                lowResList.add(wallpaper)
            }

            // Unused check: never used and added > 30 days ago, or not used in > 30 days and not favorite
            if (!wallpaper.isFavorite && (wallpaper.lastUsed == null && wallpaper.dateAdded < thirtyDaysAgo ||
                        (wallpaper.lastUsed != null && wallpaper.lastUsed < thirtyDaysAgo))) {
                unusedList.add(wallpaper)
            }
        }

        val duplicateClusters = wallpaperRepository.findDuplicateClusters()

        return LibraryHealthReport(
            totalWallpapers = allWallpapers.size,
            totalStorageBytes = totalBytes,
            brokenWallpapers = brokenList,
            lowResWallpapers = lowResList,
            unusedWallpapers = unusedList,
            duplicateClusters = duplicateClusters
        )
    }

    suspend fun cleanBrokenWallpapers(): Int {
        val allWallpapers = wallpaperDao.getAllWallpapersList()
        val brokenIds = allWallpapers.filter { !isUriAccessible(it.uri, it.isSample) }.map { it.id }
        if (brokenIds.isNotEmpty()) {
            wallpaperDao.deleteWallpapersByIds(brokenIds)
        }
        return brokenIds.size
    }

    suspend fun cleanDuplicates(keepHighestResolution: Boolean = true): Int {
        val clusters = wallpaperRepository.findDuplicateClusters()
        var removedCount = 0

        for (cluster in clusters) {
            if (cluster.size <= 1) continue

            // Sort: favorites first, then resolution (width * height) descending
            val sorted = if (keepHighestResolution) {
                cluster.sortedWith(
                    compareByDescending<WallpaperEntity> { it.isFavorite }
                        .thenByDescending { it.width * it.height }
                        .thenByDescending { it.fileSize }
                )
            } else {
                cluster.sortedByDescending { it.isFavorite }
            }

            val toKeep = sorted.first()
            val toRemove = sorted.drop(1).filter { !it.isFavorite }

            if (toRemove.isNotEmpty()) {
                val idsToRemove = toRemove.map { it.id }
                wallpaperDao.deleteWallpapersByIds(idsToRemove)
                removedCount += idsToRemove.size
            }
        }
        return removedCount
    }

    suspend fun cleanUnusedWallpapers(): Int {
        val thirtyDaysAgo = System.currentTimeMillis() - (30L * 24L * 60L * 60L * 1000L)
        val allWallpapers = wallpaperDao.getAllWallpapersList()
        val unusedIds = allWallpapers.filter {
            !it.isFavorite && !it.isSample &&
                    (it.lastUsed == null && it.dateAdded < thirtyDaysAgo || (it.lastUsed != null && it.lastUsed < thirtyDaysAgo))
        }.map { it.id }

        if (unusedIds.isNotEmpty()) {
            wallpaperDao.deleteWallpapersByIds(unusedIds)
        }
        return unusedIds.size
    }

    private fun isUriAccessible(uriString: String, isSample: Boolean): Boolean {
        if (isSample || uriString.startsWith("sample_")) return true
        return try {
            val uri = Uri.parse(uriString)
            if (uriString.startsWith("asset:///")) {
                context.assets.open(uriString.removePrefix("asset:///")).use { true }
            } else if (uri.scheme == "file" || (uri.scheme == null && uri.path?.startsWith("/") == true)) {
                val file = File(uri.path ?: uriString)
                file.exists() && file.canRead()
            } else {
                context.contentResolver.openInputStream(uri)?.use { true } ?: false
            }
        } catch (e: Exception) {
            false
        }
    }
}
