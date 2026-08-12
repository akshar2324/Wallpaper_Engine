package com.akshar.wallpaperengine.domain.strategy

import androidx.sqlite.db.SimpleSQLiteQuery
import com.akshar.wallpaperengine.data.local.dao.WallpaperDao

interface WallpaperSelectionStrategy {
    suspend fun selectWallpaper(
        wallpaperDao: WallpaperDao,
        sourceType: String,
        collectionId: Long?,
        specificWallpaperId: Long?,
        lastSelectedId: Long?
    ): Long?
}

abstract class BaseSqlSelectionStrategy : WallpaperSelectionStrategy {
    protected fun buildBaseQuery(
        sourceType: String,
        collectionId: Long?,
        specificWallpaperId: Long?,
        additionalWhere: String = "",
        orderBy: String = ""
    ): SimpleSQLiteQuery {
        var query = "SELECT w.id FROM wallpapers w"

        if (sourceType.uppercase() == "COLLECTION" && collectionId != null) {
            query += " INNER JOIN wallpaper_collection_cross_ref c ON w.id = c.wallpaperId AND c.collectionId = $collectionId"
        }

        val conditions = mutableListOf<String>()

        when (sourceType.uppercase()) {
            "FAVORITES" -> conditions.add("w.isFavorite = 1")
            "SPECIFIC" -> {
                if (specificWallpaperId != null) {
                    conditions.add("w.id = $specificWallpaperId")
                }
            }
        }

        if (additionalWhere.isNotBlank()) {
            conditions.add(additionalWhere)
        }

        if (conditions.isNotEmpty()) {
            query += " WHERE " + conditions.joinToString(" AND ")
        }

        if (orderBy.isNotBlank()) {
            query += " ORDER BY $orderBy LIMIT 1"
        } else {
            query += " LIMIT 1"
        }

        return SimpleSQLiteQuery(query)
    }
}

class RandomSelectionStrategy : BaseSqlSelectionStrategy() {
    override suspend fun selectWallpaper(
        wallpaperDao: WallpaperDao,
        sourceType: String,
        collectionId: Long?,
        specificWallpaperId: Long?,
        lastSelectedId: Long?
    ): Long? {
        val excludeCondition = if (lastSelectedId != null) "w.id != $lastSelectedId" else ""
        var query = buildBaseQuery(sourceType, collectionId, specificWallpaperId, excludeCondition, "RANDOM()")
        var id = wallpaperDao.getSingleWallpaperId(query)

        // Fallback if exclusion removed the only item
        if (id == null && lastSelectedId != null) {
            query = buildBaseQuery(sourceType, collectionId, specificWallpaperId, "", "RANDOM()")
            id = wallpaperDao.getSingleWallpaperId(query)
        }
        return id
    }
}

class SequentialSelectionStrategy : BaseSqlSelectionStrategy() {
    override suspend fun selectWallpaper(
        wallpaperDao: WallpaperDao,
        sourceType: String,
        collectionId: Long?,
        specificWallpaperId: Long?,
        lastSelectedId: Long?
    ): Long? {
        // Try getting the next item with id > lastSelectedId
        if (lastSelectedId != null) {
            val query = buildBaseQuery(sourceType, collectionId, specificWallpaperId, "w.id > $lastSelectedId", "w.id ASC")
            val nextId = wallpaperDao.getSingleWallpaperId(query)
            if (nextId != null) return nextId
        }

        // Wrap around to the first item (smallest id)
        val wrapQuery = buildBaseQuery(sourceType, collectionId, specificWallpaperId, "", "w.id ASC")
        return wallpaperDao.getSingleWallpaperId(wrapQuery)
    }
}

class LeastRecentlyUsedSelectionStrategy : BaseSqlSelectionStrategy() {
    override suspend fun selectWallpaper(
        wallpaperDao: WallpaperDao,
        sourceType: String,
        collectionId: Long?,
        specificWallpaperId: Long?,
        lastSelectedId: Long?
    ): Long? {
        val excludeCondition = if (lastSelectedId != null) "w.id != $lastSelectedId" else ""
        var query = buildBaseQuery(sourceType, collectionId, specificWallpaperId, excludeCondition, "IFNULL(w.lastUsed, 0) ASC")
        var id = wallpaperDao.getSingleWallpaperId(query)

        // Fallback
        if (id == null && lastSelectedId != null) {
            query = buildBaseQuery(sourceType, collectionId, specificWallpaperId, "", "IFNULL(w.lastUsed, 0) ASC")
            id = wallpaperDao.getSingleWallpaperId(query)
        }
        return id
    }
}

class NoRepeatRandomStrategy : BaseSqlSelectionStrategy() {
    override suspend fun selectWallpaper(
        wallpaperDao: WallpaperDao,
        sourceType: String,
        collectionId: Long?,
        specificWallpaperId: Long?,
        lastSelectedId: Long?
    ): Long? {
        // Exclude the last 5 recently used wallpapers to prevent repeating history loops
        val additionalWhere = "w.id NOT IN (SELECT id FROM wallpapers ORDER BY lastUsed DESC LIMIT 5)"
        var query = buildBaseQuery(sourceType, collectionId, specificWallpaperId, additionalWhere, "RANDOM()")
        var id = wallpaperDao.getSingleWallpaperId(query)

        // Fallback if there are less than 5 walls in pool
        if (id == null) {
            val excludeCondition = if (lastSelectedId != null) "w.id != $lastSelectedId" else ""
            query = buildBaseQuery(sourceType, collectionId, specificWallpaperId, excludeCondition, "RANDOM()")
            id = wallpaperDao.getSingleWallpaperId(query)
        }
        return id
    }
}

class WeightedFavoritesStrategy : BaseSqlSelectionStrategy() {
    override suspend fun selectWallpaper(
        wallpaperDao: WallpaperDao,
        sourceType: String,
        collectionId: Long?,
        specificWallpaperId: Long?,
        lastSelectedId: Long?
    ): Long? {
        // Note: SQLite random is purely random. A true weighted random needs an order by with a weight trick.
        // E.g., ORDER BY RANDOM() * (CASE WHEN isFavorite = 1 THEN 3 ELSE 1 END) DESC
        val excludeCondition = if (lastSelectedId != null) "w.id != $lastSelectedId" else ""
        val orderBy = "(ABS(RANDOM() % 1000) * (CASE WHEN w.isFavorite = 1 THEN 3 ELSE 1 END)) DESC"

        var query = buildBaseQuery(sourceType, collectionId, specificWallpaperId, excludeCondition, orderBy)
        var id = wallpaperDao.getSingleWallpaperId(query)

        if (id == null && lastSelectedId != null) {
            query = buildBaseQuery(sourceType, collectionId, specificWallpaperId, "", orderBy)
            id = wallpaperDao.getSingleWallpaperId(query)
        }
        return id
    }
}

class FavoritesSelectionStrategy : RandomSelectionStrategy()

object SelectionStrategyFactory {
    fun getStrategy(modeName: String): WallpaperSelectionStrategy {
        return when (modeName.uppercase()) {
            "SEQUENTIAL" -> SequentialSelectionStrategy()
            "LRU" -> LeastRecentlyUsedSelectionStrategy()
            "NO_REPEAT_RANDOM" -> NoRepeatRandomStrategy()
            "WEIGHTED_FAVORITES" -> WeightedFavoritesStrategy()
            "PLAYLIST_SEQUENTIAL" -> PlaylistSequentialStrategy()
            "PLAYLIST_SHUFFLE" -> PlaylistShuffleStrategy()
            else -> RandomSelectionStrategy() // Default to Random
        }
    }
}
