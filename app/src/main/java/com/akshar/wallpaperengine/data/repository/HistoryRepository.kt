package com.akshar.wallpaperengine.data.repository

import com.akshar.wallpaperengine.data.local.dao.HistoryDao
import com.akshar.wallpaperengine.data.local.entity.WallpaperHistoryEntity
import kotlinx.coroutines.flow.Flow

class HistoryRepository(private val historyDao: HistoryDao) {

    val allHistory: Flow<List<WallpaperHistoryEntity>> = historyDao.getAllHistory()

    suspend fun recordHistory(record: WallpaperHistoryEntity): Long {
        return historyDao.insertHistoryRecord(record)
    }

    suspend fun clearHistory() {
        historyDao.clearHistory()
    }

    suspend fun deleteHistoryRecord(id: Long) {
        historyDao.deleteHistoryRecord(id)
    }

    suspend fun pruneHistoryDays(daysToKeep: Int) {
        val cutoff = System.currentTimeMillis() - (daysToKeep * 24 * 60 * 60 * 1000L)
        historyDao.pruneHistoryOlderThan(cutoff)
    }
}
