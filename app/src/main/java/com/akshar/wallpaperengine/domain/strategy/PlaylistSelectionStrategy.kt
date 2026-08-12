package com.akshar.wallpaperengine.domain.strategy

import androidx.sqlite.db.SimpleSQLiteQuery
import com.akshar.wallpaperengine.data.local.dao.WallpaperDao

class PlaylistSequentialStrategy : BaseSqlSelectionStrategy() {
    override suspend fun selectWallpaper(
        wallpaperDao: WallpaperDao,
        sourceType: String,
        collectionId: Long?,
        specificWallpaperId: Long?,
        lastSelectedId: Long?
    ): Long? {
        val playlistId = collectionId ?: return null // Reusing collectionId for playlistId

        if (lastSelectedId != null) {
            // Find current position
            val positionQuery = SimpleSQLiteQuery("SELECT position FROM playlist_wallpaper_cross_ref WHERE playlistId = $playlistId AND wallpaperId = $lastSelectedId")
            val currentPos = wallpaperDao.getSingleWallpaperId(positionQuery) // Actually returns Int casted to Long in our DAO impl

            if (currentPos != null) {
                // Get next
                val nextQuery = SimpleSQLiteQuery("SELECT wallpaperId FROM playlist_wallpaper_cross_ref WHERE playlistId = $playlistId AND position > $currentPos ORDER BY position ASC LIMIT 1")
                val nextId = wallpaperDao.getSingleWallpaperId(nextQuery)
                if (nextId != null) return nextId
            }
        }

        // Start from beginning
        val firstQuery = SimpleSQLiteQuery("SELECT wallpaperId FROM playlist_wallpaper_cross_ref WHERE playlistId = $playlistId ORDER BY position ASC LIMIT 1")
        return wallpaperDao.getSingleWallpaperId(firstQuery)
    }
}

class PlaylistShuffleStrategy : BaseSqlSelectionStrategy() {
    override suspend fun selectWallpaper(
        wallpaperDao: WallpaperDao,
        sourceType: String,
        collectionId: Long?,
        specificWallpaperId: Long?,
        lastSelectedId: Long?
    ): Long? {
        val playlistId = collectionId ?: return null

        // Similar to no-repeat, exclude last 3 from history
        val excludeCondition = "AND wallpaperId NOT IN (SELECT wallpaperId FROM wallpaper_history WHERE source = 'SCHEDULE' AND scheduleId = (SELECT id FROM schedules WHERE sourceCollectionId = $playlistId LIMIT 1) ORDER BY appliedAt DESC LIMIT 3)"

        var query = SimpleSQLiteQuery("SELECT wallpaperId FROM playlist_wallpaper_cross_ref WHERE playlistId = $playlistId $excludeCondition ORDER BY RANDOM() LIMIT 1")
        var id = wallpaperDao.getSingleWallpaperId(query)

        if (id == null) {
            val excludeJustLast = if (lastSelectedId != null) "AND wallpaperId != $lastSelectedId" else ""
            query = SimpleSQLiteQuery("SELECT wallpaperId FROM playlist_wallpaper_cross_ref WHERE playlistId = $playlistId $excludeJustLast ORDER BY RANDOM() LIMIT 1")
            id = wallpaperDao.getSingleWallpaperId(query)
        }
        return id
    }
}
