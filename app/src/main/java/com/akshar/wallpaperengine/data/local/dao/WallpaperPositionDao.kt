package com.akshar.wallpaperengine.data.local.dao

import androidx.room.*
import com.akshar.wallpaperengine.data.local.entity.WallpaperPositionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WallpaperPositionDao {

    @Query("SELECT * FROM wallpaper_positions WHERE wallpaperId = :wallpaperId AND targetScreen = :targetScreen LIMIT 1")
    suspend fun getPosition(wallpaperId: Long, targetScreen: String): WallpaperPositionEntity?

    @Query("SELECT * FROM wallpaper_positions WHERE wallpaperId = :wallpaperId")
    fun getPositionsForWallpaper(wallpaperId: Long): Flow<List<WallpaperPositionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPosition(position: WallpaperPositionEntity): Long

    @Delete
    suspend fun deletePosition(position: WallpaperPositionEntity)
}
