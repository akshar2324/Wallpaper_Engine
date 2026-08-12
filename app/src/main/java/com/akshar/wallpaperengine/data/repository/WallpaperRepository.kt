package com.akshar.wallpaperengine.data.repository

import android.content.Context
import com.akshar.wallpaperengine.data.local.dao.CollectionDao
import com.akshar.wallpaperengine.data.local.dao.TagDao
import com.akshar.wallpaperengine.data.local.dao.WallpaperDao
import com.akshar.wallpaperengine.data.local.entity.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

enum class SortOrder {
    RECENTLY_ADDED,
    RECENTLY_USED,
    NAME_ASC,
    NAME_DESC,
    HIGHEST_RESOLUTION,
    LOWEST_RESOLUTION
}

enum class OrientationFilter {
    ALL,
    PORTRAIT,
    LANDSCAPE,
    SQUARE
}

data class FilterOptions(
    val searchQuery: String = "",
    val favoritesOnly: Boolean = false,
    val collectionId: Long? = null,
    val tagId: Long? = null,
    val orientation: OrientationFilter = OrientationFilter.ALL,
    val sortOrder: SortOrder = SortOrder.RECENTLY_ADDED
)

class WallpaperRepository(
    private val wallpaperDao: WallpaperDao,
    private val collectionDao: CollectionDao,
    private val tagDao: TagDao,
    private val context: Context
) {

    val allWallpapers: Flow<List<WallpaperEntity>> = wallpaperDao.getAllWallpapers()
    val favoriteWallpapers: Flow<List<WallpaperEntity>> = wallpaperDao.getFavoriteWallpapers()
    val recentlyUsedWallpapers: Flow<List<WallpaperEntity>> = wallpaperDao.getRecentlyUsedWallpapers(10)
    val currentWallpaper: Flow<WallpaperEntity?> = wallpaperDao.getCurrentWallpaperFlow()
    val wallpaperCount: Flow<Int> = wallpaperDao.getWallpaperCount()

    fun getFilteredWallpapers(options: FilterOptions): Flow<List<WallpaperEntity>> {
        val baseFlow = if (options.collectionId != null) {
            wallpaperDao.getWallpapersForCollection(options.collectionId)
        } else if (options.tagId != null) {
            wallpaperDao.getWallpapersForTag(options.tagId)
        } else {
            wallpaperDao.getAllWallpapers()
        }

        return baseFlow.map { list ->
            var result = list

            if (options.searchQuery.isNotBlank()) {
                val q = options.searchQuery.trim().lowercase()
                result = result.filter { it.title.lowercase().contains(q) }
            }

            if (options.favoritesOnly) {
                result = result.filter { it.isFavorite }
            }

            if (options.orientation != OrientationFilter.ALL) {
                result = result.filter {
                    val ratio = it.width.toFloat() / it.height.toFloat()
                    when (options.orientation) {
                        OrientationFilter.PORTRAIT -> ratio < 0.9f
                        OrientationFilter.LANDSCAPE -> ratio > 1.1f
                        OrientationFilter.SQUARE -> ratio in 0.9f..1.1f
                        else -> true
                    }
                }
            }

            when (options.sortOrder) {
                SortOrder.RECENTLY_ADDED -> result.sortedByDescending { it.dateAdded }
                SortOrder.RECENTLY_USED -> result.sortedByDescending { it.lastUsed ?: 0L }
                SortOrder.NAME_ASC -> result.sortedBy { it.title.lowercase() }
                SortOrder.NAME_DESC -> result.sortedByDescending { it.title.lowercase() }
                SortOrder.HIGHEST_RESOLUTION -> result.sortedByDescending { it.width * it.height }
                SortOrder.LOWEST_RESOLUTION -> result.sortedBy { it.width * it.height }
            }
        }
    }

    suspend fun getWallpaperById(id: Long): WallpaperEntity? = wallpaperDao.getWallpaperById(id)

    suspend fun insertWallpaper(wallpaper: WallpaperEntity): Long = wallpaperDao.insertWallpaper(wallpaper)

    suspend fun updateWallpaper(wallpaper: WallpaperEntity) = wallpaperDao.updateWallpaper(wallpaper)

    suspend fun deleteWallpaper(wallpaper: WallpaperEntity) = wallpaperDao.deleteWallpaper(wallpaper)

    suspend fun deleteWallpapersByIds(ids: List<Long>) = wallpaperDao.deleteWallpapersByIds(ids)

    suspend fun toggleFavorite(id: Long, isFavorite: Boolean) = wallpaperDao.updateFavorite(id, isFavorite)

    suspend fun updateLastUsed(id: Long) = wallpaperDao.updateLastUsed(id, System.currentTimeMillis())

    suspend fun addWallpaperToCollection(wallpaperId: Long, collectionId: Long) {
        wallpaperDao.insertWallpaperCollectionCrossRef(
            WallpaperCollectionCrossRef(wallpaperId = wallpaperId, collectionId = collectionId)
        )
        collectionDao.updateCollectionCount(collectionId)
    }

    suspend fun removeWallpaperFromCollection(wallpaperId: Long, collectionId: Long) {
        wallpaperDao.removeWallpaperFromCollection(wallpaperId, collectionId)
        collectionDao.updateCollectionCount(collectionId)
    }

    suspend fun addTagToWallpaper(wallpaperId: Long, tagId: Long) {
        wallpaperDao.insertWallpaperTagCrossRef(
            WallpaperTagCrossRef(wallpaperId = wallpaperId, tagId = tagId)
        )
    }

    suspend fun removeTagFromWallpaper(wallpaperId: Long, tagId: Long) {
        wallpaperDao.removeTagFromWallpaper(wallpaperId, tagId)
    }

    suspend fun seedInitialDataIfEmpty() {
        val count = wallpaperDao.getAllWallpapersList().size
        if (count > 0) return

        // Create sample wallpapers
        val sampleList = listOf(
            WallpaperEntity(
                uri = "sample_abyss_nebula",
                title = "Abyss Void Core",
                width = 1080,
                height = 2400,
                aspectRatio = 9f / 20f,
                fileSize = 1024000L,
                isFavorite = true,
                isSample = true
            ),
            WallpaperEntity(
                uri = "sample_neon_cyberpunk",
                title = "Neon Tokyo Skyline",
                width = 1080,
                height = 2340,
                aspectRatio = 9f / 19.5f,
                fileSize = 1240000L,
                isFavorite = true,
                isSample = true
            ),
            WallpaperEntity(
                uri = "sample_crimson_ronin",
                title = "Crimson Blade Samurai",
                width = 1440,
                height = 3200,
                aspectRatio = 9f / 20f,
                fileSize = 2048000L,
                isFavorite = false,
                isSample = true
            ),
            WallpaperEntity(
                uri = "sample_moonlight_horizon",
                title = "Moonlight Astral Gate",
                width = 1080,
                height = 1920,
                aspectRatio = 9f / 16f,
                fileSize = 850000L,
                isFavorite = true,
                isSample = true
            ),
            WallpaperEntity(
                uri = "sample_sakura_cyber",
                title = "Sakura Cyberpunk Alley",
                width = 1080,
                height = 2400,
                aspectRatio = 9f / 20f,
                fileSize = 1500000L,
                isFavorite = false,
                isSample = true
            ),
            WallpaperEntity(
                uri = "sample_aurora_gateway",
                title = "Aurora Prism Horizon",
                width = 1080,
                height = 2340,
                aspectRatio = 9f / 19.5f,
                fileSize = 1100000L,
                isFavorite = false,
                isSample = true
            )
        )

        val wallpaperIds = wallpaperDao.insertWallpapers(sampleList)

        // Seed Collections
        val animeCol = collectionDao.insertCollection(CollectionEntity(name = "Anime Cyberpunk", description = "Futuristic anime cities & cybernetics"))
        val darkCol = collectionDao.insertCollection(CollectionEntity(name = "Dark Void", description = "Obsidian surfaces and electric purple glows"))
        val gamingCol = collectionDao.insertCollection(CollectionEntity(name = "Gaming & Tech", description = "High contrast sci-fi & game visuals"))

        if (wallpaperIds.size >= 4) {
            addWallpaperToCollection(wallpaperIds[0], darkCol)
            addWallpaperToCollection(wallpaperIds[1], animeCol)
            addWallpaperToCollection(wallpaperIds[2], gamingCol)
            addWallpaperToCollection(wallpaperIds[3], darkCol)
            addWallpaperToCollection(wallpaperIds[4], animeCol)
        }

        // Seed Tags
        val tag1 = tagDao.insertTag(TagEntity(name = "anime"))
        val tag2 = tagDao.insertTag(TagEntity(name = "cyberpunk"))
        val tag3 = tagDao.insertTag(TagEntity(name = "purple"))
        val tag4 = tagDao.insertTag(TagEntity(name = "dark"))
        val tag5 = tagDao.insertTag(TagEntity(name = "neon"))

        if (wallpaperIds.isNotEmpty()) {
            wallpaperIds.forEach { id ->
                addTagToWallpaper(id, tag1)
                addTagToWallpaper(id, tag2)
                addTagToWallpaper(id, tag3)
                addTagToWallpaper(id, tag4)
            }
        }
    }
}
