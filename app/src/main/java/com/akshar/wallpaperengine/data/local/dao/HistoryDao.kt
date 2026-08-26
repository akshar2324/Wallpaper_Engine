package com.akshar.wallpaperengine.data.local.dao

import androidx.room.*
import com.akshar.wallpaperengine.data.local.entity.WallpaperHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {

    @Query("SELECT * FROM wallpaper_history ORDER BY appliedAt DESC")
    fun getAllHistory(): Flow<List<WallpaperHistoryEntity>>

    @Query("SELECT * FROM wallpaper_history ORDER BY appliedAt DESC")
    suspend fun getAllHistoryList(): List<WallpaperHistoryEntity>

    @Query("SELECT * FROM wallpaper_history ORDER BY appliedAt DESC LIMIT :limit")
    suspend fun getRecentHistoryList(limit: Int): List<WallpaperHistoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistoryRecord(record: WallpaperHistoryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistoryRecords(records: List<WallpaperHistoryEntity>)

    @Query("DELETE FROM wallpaper_history")
    suspend fun clearHistory()

    @Query("DELETE FROM wallpaper_history WHERE id = :id")
    suspend fun deleteHistoryRecord(id: Long)

    @Query("DELETE FROM wallpaper_history WHERE appliedAt < :thresholdTimestamp")
    suspend fun pruneHistoryOlderThan(thresholdTimestamp: Long)
}
