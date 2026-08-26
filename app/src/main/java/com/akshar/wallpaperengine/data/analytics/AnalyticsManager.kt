package com.akshar.wallpaperengine.data.analytics

import com.akshar.wallpaperengine.data.local.dao.HistoryDao
import com.akshar.wallpaperengine.data.local.dao.WallpaperDao
import com.akshar.wallpaperengine.data.local.entity.WallpaperEntity

data class EngineAnalytics(
    val totalWallpapers: Int,
    val totalRotations: Int,
    val manualRotations: Int,
    val scheduledRotations: Int,
    val contextTriggerRotations: Int,
    val averageRating: Float,
    val totalLikes: Int,
    val totalSkips: Int,
    val averageLuminance: Float,
    val darkOledPercentage: Int,
    val topStyles: List<Pair<String, Int>>,
    val topMoods: List<Pair<String, Int>>,
    val mostRotatedWallpapers: List<WallpaperEntity>
)

class AnalyticsManager(
    private val wallpaperDao: WallpaperDao,
    private val historyDao: HistoryDao
) {

    suspend fun getAnalytics(): EngineAnalytics {
        val wallpapers = wallpaperDao.getAllWallpapersList()
        val history = historyDao.getAllHistoryList()

        val totalRotations = history.size
        val manualRotations = history.count { it.source == "MANUAL" }
        val scheduledRotations = history.count { it.source == "SCHEDULE" }
        val contextTriggerRotations = history.count { it.source == "CONTEXT_TRIGGER" }

        val totalLikes = wallpapers.sumOf { it.likeCount }
        val totalSkips = wallpapers.sumOf { it.skipCount }
        val avgRating = if (wallpapers.isNotEmpty()) {
            wallpapers.map { it.rating }.average().toFloat()
        } else {
            0.0f
        }

        val avgLuminance = if (wallpapers.isNotEmpty()) {
            wallpapers.map { it.brightness }.average().toFloat()
        } else {
            0.5f
        }

        val darkCount = wallpapers.count { it.isDark }
        val darkOledPercentage = if (wallpapers.isNotEmpty()) {
            ((darkCount.toDouble() / wallpapers.size) * 100).toInt()
        } else {
            0
        }

        val styleCounts = wallpapers.mapNotNull { it.style }
            .groupingBy { it }
            .eachCount()
            .toList()
            .sortedByDescending { it.second }
            .take(5)

        val moodCounts = wallpapers.mapNotNull { it.mood }
            .groupingBy { it }
            .eachCount()
            .toList()
            .sortedByDescending { it.second }
            .take(5)

        // Wallpapers that appeared most in history
        val wallpaperHistoryCounts = history.groupingBy { it.wallpaperId }.eachCount()
        val topRotated = wallpapers
            .filter { wallpaperHistoryCounts.containsKey(it.id) }
            .sortedByDescending { wallpaperHistoryCounts[it.id] ?: 0 }
            .take(5)

        return EngineAnalytics(
            totalWallpapers = wallpapers.size,
            totalRotations = totalRotations,
            manualRotations = manualRotations,
            scheduledRotations = scheduledRotations,
            contextTriggerRotations = contextTriggerRotations,
            averageRating = avgRating,
            totalLikes = totalLikes,
            totalSkips = totalSkips,
            averageLuminance = avgLuminance,
            darkOledPercentage = darkOledPercentage,
            topStyles = styleCounts,
            topMoods = moodCounts,
            mostRotatedWallpapers = topRotated
        )
    }
}
