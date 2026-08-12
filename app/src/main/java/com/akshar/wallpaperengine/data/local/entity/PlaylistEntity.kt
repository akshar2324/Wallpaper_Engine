package com.akshar.wallpaperengine.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val coverUri: String? = null,
    val rotationMode: String = "SEQUENTIAL", // SEQUENTIAL, SHUFFLE
    val wallpaperCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "playlist_wallpaper_cross_ref",
    primaryKeys = ["playlistId", "wallpaperId"],
    indices = [androidx.room.Index("playlistId"), androidx.room.Index("wallpaperId")]
)
data class PlaylistWallpaperCrossRef(
    val playlistId: Long,
    val wallpaperId: Long,
    val position: Int = 0
)
