package com.akshar.wallpaperengine.data.local.dao

import androidx.room.*
import com.akshar.wallpaperengine.data.local.entity.WallpaperHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {

    @Query("SELECT * FROM wallpaper_history ORDER BY appliedAt DESC")
    fun getAllHistory(): Flow<List<WallpaperHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistoryRecord(record: WallpaperHistoryEntity): Long

    @Query("DELETE FROM wallpaper_history")
    suspend fun clearHistory()

    @Query("DELETE FROM wallpaper_history WHERE id = :id")
    suspend fun deleteHistoryRecord(id: Long)

    @Query("DELETE FROM wallpaper_history WHERE appliedAt < :thresholdTimestamp")
    suspend fun pruneHistoryOlderThan(thresholdTimestamp: Long)
}
