package com.akshar.wallpaperengine.data.local.dao

import androidx.room.*
import com.akshar.wallpaperengine.data.local.entity.PlaylistEntity
import com.akshar.wallpaperengine.data.local.entity.PlaylistWallpaperCrossRef
import com.akshar.wallpaperengine.data.local.entity.WallpaperEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {

    @Query("SELECT * FROM playlists ORDER BY name ASC")
    fun getAllPlaylists(): Flow<List<PlaylistEntity>>

    @Query("SELECT * FROM playlists")
    suspend fun getAllPlaylistsList(): List<PlaylistEntity>

    @Query("SELECT * FROM playlists WHERE id = :id")
    suspend fun getPlaylistById(id: Long): PlaylistEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: PlaylistEntity): Long

    @Update
    suspend fun updatePlaylist(playlist: PlaylistEntity)

    @Delete
    suspend fun deletePlaylist(playlist: PlaylistEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylistWallpaperCrossRef(crossRef: PlaylistWallpaperCrossRef)

    @Query("DELETE FROM playlist_wallpaper_cross_ref WHERE playlistId = :playlistId AND wallpaperId = :wallpaperId")
    suspend fun removeWallpaperFromPlaylist(playlistId: Long, wallpaperId: Long)

    @Query("UPDATE playlists SET wallpaperCount = (SELECT COUNT(*) FROM playlist_wallpaper_cross_ref WHERE playlistId = :playlistId) WHERE id = :playlistId")
    suspend fun updatePlaylistCount(playlistId: Long)

    @Query("""
        SELECT w.* FROM wallpapers w
        INNER JOIN playlist_wallpaper_cross_ref ref ON w.id = ref.wallpaperId
        WHERE ref.playlistId = :playlistId
        ORDER BY ref.position ASC
    """)
    fun getWallpapersForPlaylist(playlistId: Long): Flow<List<WallpaperEntity>>

    @Query("SELECT MAX(position) FROM playlist_wallpaper_cross_ref WHERE playlistId = :playlistId")
    suspend fun getMaxPosition(playlistId: Long): Int?
}
