package com.akshar.wallpaperengine.data.repository

import com.akshar.wallpaperengine.data.local.dao.PlaylistDao
import com.akshar.wallpaperengine.data.local.entity.PlaylistEntity
import com.akshar.wallpaperengine.data.local.entity.PlaylistWallpaperCrossRef
import com.akshar.wallpaperengine.data.local.entity.WallpaperEntity
import kotlinx.coroutines.flow.Flow

class PlaylistRepository(private val playlistDao: PlaylistDao) {

    val allPlaylists: Flow<List<PlaylistEntity>> = playlistDao.getAllPlaylists()

    suspend fun getPlaylistById(id: Long): PlaylistEntity? = playlistDao.getPlaylistById(id)

    suspend fun createPlaylist(name: String, rotationMode: String = "SEQUENTIAL"): Long {
        return playlistDao.insertPlaylist(
            PlaylistEntity(name = name, rotationMode = rotationMode)
        )
    }

    suspend fun updatePlaylist(playlist: PlaylistEntity) = playlistDao.updatePlaylist(playlist)

    suspend fun deletePlaylist(playlist: PlaylistEntity) = playlistDao.deletePlaylist(playlist)

    suspend fun addWallpaperToPlaylist(playlistId: Long, wallpaperId: Long) {
        val currentMax = playlistDao.getMaxPosition(playlistId) ?: -1
        playlistDao.insertPlaylistWallpaperCrossRef(
            PlaylistWallpaperCrossRef(playlistId = playlistId, wallpaperId = wallpaperId, position = currentMax + 1)
        )
        playlistDao.updatePlaylistCount(playlistId)
    }

    suspend fun removeWallpaperFromPlaylist(playlistId: Long, wallpaperId: Long) {
        playlistDao.removeWallpaperFromPlaylist(playlistId, wallpaperId)
        playlistDao.updatePlaylistCount(playlistId)
    }

    fun getWallpapersForPlaylist(playlistId: Long): Flow<List<WallpaperEntity>> {
        return playlistDao.getWallpapersForPlaylist(playlistId)
    }
}
