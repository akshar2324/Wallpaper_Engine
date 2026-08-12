package com.akshar.wallpaperengine.domain.strategy

import com.akshar.wallpaperengine.data.local.entity.WallpaperEntity

interface WallpaperSelectionStrategy {
    fun selectWallpaper(wallpaperIds: List<Long>, lastSelectedId: Long?): Long?
}

class RandomSelectionStrategy : WallpaperSelectionStrategy {
    override fun selectWallpaper(wallpaperIds: List<Long>, lastSelectedId: Long?): Long? {
        if (wallpaperIds.isEmpty()) return null
        if (wallpaperIds.size == 1) return wallpaperIds.first()
        val candidates = wallpaperIds.filter { it != lastSelectedId }
        return (if (candidates.isNotEmpty()) candidates else wallpaperIds).random()
    }
}

class SequentialSelectionStrategy : WallpaperSelectionStrategy {
    override fun selectWallpaper(wallpaperIds: List<Long>, lastSelectedId: Long?): Long? {
        if (wallpaperIds.isEmpty()) return null
        val sortedIds = wallpaperIds.sorted() // Important for stable sequential iteration
        if (lastSelectedId == null) return sortedIds.first()
        val index = sortedIds.indexOf(lastSelectedId)
        return if (index != -1 && index + 1 < sortedIds.size) {
            sortedIds[index + 1]
        } else {
            sortedIds.first()
        }
    }
}

// Favorites/LRU Strategies are complex because LRU needs lastUsed time. 
// We will change them to random or sequential if we just get IDs. 
// For LRU, we can't do it just with IDs. Let's fallback to Random if not Sequential.
class FavoritesSelectionStrategy : WallpaperSelectionStrategy {
    override fun selectWallpaper(wallpaperIds: List<Long>, lastSelectedId: Long?): Long? {
        // It relies on the input list already being favorites.
        if (wallpaperIds.isEmpty()) return null
        val candidates = wallpaperIds.filter { it != lastSelectedId }
        return (if (candidates.isNotEmpty()) candidates else wallpaperIds).random()
    }
}

class LeastRecentlyUsedSelectionStrategy : WallpaperSelectionStrategy {
    override fun selectWallpaper(wallpaperIds: List<Long>, lastSelectedId: Long?): Long? {
        // We cannot compute LRU from IDs alone accurately here, so fallback to Random for now.
        // A true LRU strategy should be a direct DAO query (ORDER BY lastUsed ASC LIMIT 1)
        if (wallpaperIds.isEmpty()) return null
        val candidates = wallpaperIds.filter { it != lastSelectedId }
        return (if (candidates.isNotEmpty()) candidates else wallpaperIds).random()
    }
}

object SelectionStrategyFactory {
    fun getStrategy(modeName: String): WallpaperSelectionStrategy {
        return when (modeName.uppercase()) {
            "SEQUENTIAL" -> SequentialSelectionStrategy()
            "FAVORITES" -> FavoritesSelectionStrategy()
            "LRU" -> LeastRecentlyUsedSelectionStrategy()
            else -> RandomSelectionStrategy()
        }
    }
}
