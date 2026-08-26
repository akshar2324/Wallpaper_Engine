package com.akshar.wallpaperengine.data.analytics

import com.akshar.wallpaperengine.data.local.dao.HistoryDao
import com.akshar.wallpaperengine.data.local.dao.WallpaperDao
import com.akshar.wallpaperengine.data.local.entity.WallpaperEntity
import com.akshar.wallpaperengine.data.local.entity.WallpaperHistoryEntity
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.mockito.kotlin.*

class AnalyticsManagerTest {

    @Test
    fun testGetAnalyticsComputesCorrectMetrics() {
        runBlocking {
            val wallpaperDao = mock<WallpaperDao>()
            val historyDao = mock<HistoryDao>()

            val w1 = WallpaperEntity(id = 1L, uri = "sample_1", title = "Cyber Night", rating = 5.0f, likeCount = 3, skipCount = 0, isDark = true, style = "Cyberpunk", mood = "Mysterious")
            val w2 = WallpaperEntity(id = 2L, uri = "sample_2", title = "Nature Morning", rating = 3.0f, likeCount = 1, skipCount = 2, isDark = false, style = "Nature", mood = "Calm")

            whenever(wallpaperDao.getAllWallpapersList()).thenReturn(listOf(w1, w2))

            val h1 = WallpaperHistoryEntity(id = 1L, wallpaperId = 1L, wallpaperTitle = "Cyber Night", wallpaperUri = "sample_1", appliedAt = 1000L, source = "SCHEDULE")
            val h2 = WallpaperHistoryEntity(id = 2L, wallpaperId = 1L, wallpaperTitle = "Cyber Night", wallpaperUri = "sample_1", appliedAt = 2000L, source = "CONTEXT_TRIGGER")
            val h3 = WallpaperHistoryEntity(id = 3L, wallpaperId = 2L, wallpaperTitle = "Nature Morning", wallpaperUri = "sample_2", appliedAt = 3000L, source = "MANUAL")

            whenever(historyDao.getAllHistoryList()).thenReturn(listOf(h1, h2, h3))

            val manager = AnalyticsManager(wallpaperDao, historyDao)
            val analytics = manager.getAnalytics()

            assertEquals(2, analytics.totalWallpapers)
            assertEquals(3, analytics.totalRotations)
            assertEquals(1, analytics.scheduledRotations)
            assertEquals(1, analytics.contextTriggerRotations)
            assertEquals(1, analytics.manualRotations)
            assertEquals(4.0f, analytics.averageRating, 0.01f)
            assertEquals(4, analytics.totalLikes)
            assertEquals(2, analytics.totalSkips)
            assertEquals(50, analytics.darkOledPercentage)
            assertEquals("Cyberpunk", analytics.topStyles.first().first)
        }
    }
}
