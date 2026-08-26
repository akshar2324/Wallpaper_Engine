package com.akshar.wallpaperengine.domain.strategy

import androidx.sqlite.db.SupportSQLiteQuery
import com.akshar.wallpaperengine.data.local.dao.WallpaperDao
import com.akshar.wallpaperengine.data.local.entity.WallpaperEntity
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.mockito.kotlin.*

class SelectionStrategyTest {

    @Test
    fun randomStrategySelectsWallpaperWithReason() {
        runBlocking {
            val dao = mock<WallpaperDao>()
            whenever(dao.getSingleWallpaperId(any())).thenReturn(5L)

            val strategy = RandomSelectionStrategy()
            val result = strategy.selectWallpaperWithReason(dao, "ALL", null, null, null)

            assertNotNull(result)
            assertEquals(5L, result!!.wallpaperId)
            assertEquals("Random Selection", result.reason)

            val legacyResult = strategy.selectWallpaper(dao, "ALL", null, null, null)
            assertEquals(5L, legacyResult)
        }
    }

    @Test
    fun randomStrategyFallbackWhenExclusionReturnsNull() {
        runBlocking {
            val dao = mock<WallpaperDao>()
            // First call (with exclusion) returns null, second call (fallback without exclusion) returns 5L
            whenever(dao.getSingleWallpaperId(any()))
                .thenReturn(null)
                .thenReturn(5L)

            val strategy = RandomSelectionStrategy()
            val result = strategy.selectWallpaperWithReason(dao, "ALL", null, null, 1L)

            assertNotNull(result)
            assertEquals(5L, result!!.wallpaperId)
            assertEquals("Random Selection", result.reason)
            verify(dao, times(2)).getSingleWallpaperId(any())
        }
    }

    @Test
    fun sequentialStrategyNextId() {
        runBlocking {
            val dao = mock<WallpaperDao>()
            whenever(dao.getSingleWallpaperId(any())).thenReturn(2L)

            val strategy = SequentialSelectionStrategy()
            val result = strategy.selectWallpaperWithReason(dao, "ALL", null, null, 1L)

            assertNotNull(result)
            assertEquals(2L, result!!.wallpaperId)
            assertEquals("Sequential Step", result.reason)
        }
    }

    @Test
    fun sequentialStrategyWrapsAroundWhenNextReturnsNull() {
        runBlocking {
            val dao = mock<WallpaperDao>()
            whenever(dao.getSingleWallpaperId(any()))
                .thenReturn(null)
                .thenReturn(1L)

            val strategy = SequentialSelectionStrategy()
            val result = strategy.selectWallpaperWithReason(dao, "ALL", null, null, 5L)

            assertNotNull(result)
            assertEquals(1L, result!!.wallpaperId)
            assertEquals("Sequential Step", result.reason)
            verify(dao, times(2)).getSingleWallpaperId(any())
        }
    }

    @Test
    fun leastRecentlyUsedStrategySelectsLruWallpaper() {
        runBlocking {
            val dao = mock<WallpaperDao>()
            whenever(dao.getSingleWallpaperId(any())).thenReturn(7L)

            val strategy = LeastRecentlyUsedSelectionStrategy()
            val result = strategy.selectWallpaperWithReason(dao, "ALL", null, null, null)

            assertNotNull(result)
            assertEquals(7L, result!!.wallpaperId)
            assertEquals("Least Recently Used", result.reason)
        }
    }

    @Test
    fun smartShuffleStrategySelectsWallpaperWithAdaptiveScore() {
        runBlocking {
            val dao = mock<WallpaperDao>()
            whenever(dao.getSingleWallpaperId(any())).thenReturn(12L)

            val strategy = SmartShuffleSelectionStrategy()
            val result = strategy.selectWallpaperWithReason(dao, "ALL", null, null, null)

            assertNotNull(result)
            assertEquals(12L, result!!.wallpaperId)
            assertEquals("Smart Shuffle (Adaptive Score)", result.reason)
        }
    }

    @Test
    fun weightedFavoritesStrategySelectsWallpaper() {
        runBlocking {
            val dao = mock<WallpaperDao>()
            whenever(dao.getSingleWallpaperId(any())).thenReturn(15L)

            val strategy = WeightedFavoritesSelectionStrategy()
            val result = strategy.selectWallpaperWithReason(dao, "ALL", null, null, null)

            assertNotNull(result)
            assertEquals(15L, result!!.wallpaperId)
            assertTrue(result.reason.startsWith("Weighted Favorite"))
        }
    }

    @Test
    fun weightedFavoritesStrategyFallbackWhenPoolReturnsNull() {
        runBlocking {
            val dao = mock<WallpaperDao>()
            whenever(dao.getSingleWallpaperId(any()))
                .thenReturn(null)
                .thenReturn(20L)

            val strategy = WeightedFavoritesSelectionStrategy()
            val result = strategy.selectWallpaperWithReason(dao, "ALL", null, null, 1L)

            assertNotNull(result)
            assertEquals(20L, result!!.wallpaperId)
            assertTrue(result.reason.startsWith("Weighted Favorite"))
        }
    }

    @Test
    fun neverRepeatStrategyPrefersUnusedWallpaper() {
        runBlocking {
            val dao = mock<WallpaperDao>()
            whenever(dao.getSingleWallpaperId(any())).thenReturn(30L)

            val strategy = NeverRepeatSelectionStrategy()
            val result = strategy.selectWallpaperWithReason(dao, "ALL", null, null, null)

            assertNotNull(result)
            assertEquals(30L, result!!.wallpaperId)
            assertEquals("Never Repeat (Unused/Oldest)", result.reason)
            verify(dao, times(1)).getSingleWallpaperId(any())
        }
    }

    @Test
    fun neverRepeatStrategyFallsBackToOldestUsed() {
        runBlocking {
            val dao = mock<WallpaperDao>()
            // Unused query returns null, oldest used query returns 31L
            whenever(dao.getSingleWallpaperId(any()))
                .thenReturn(null)
                .thenReturn(31L)

            val strategy = NeverRepeatSelectionStrategy()
            val result = strategy.selectWallpaperWithReason(dao, "ALL", null, null, null)

            assertNotNull(result)
            assertEquals(31L, result!!.wallpaperId)
            assertEquals("Never Repeat (Unused/Oldest)", result.reason)
            verify(dao, times(2)).getSingleWallpaperId(any())
        }
    }

    @Test
    fun varietyStrategyUsesLastWallpaperMetadata() {
        runBlocking {
            val dao = mock<WallpaperDao>()
            val lastWallpaper = WallpaperEntity(
                id = 40L,
                uri = "uri",
                title = "title",
                style = "Anime",
                mood = "Calm",
                dominantColor = 0xFF0000.toInt()
            )
            whenever(dao.getWallpaperById(40L)).thenReturn(lastWallpaper)
            whenever(dao.getSingleWallpaperId(any())).thenReturn(41L)

            val strategy = VarietySelectionStrategy()
            val result = strategy.selectWallpaperWithReason(dao, "ALL", null, null, 40L)

            assertNotNull(result)
            assertEquals(41L, result!!.wallpaperId)
            assertEquals("Variety Mode", result.reason)
            verify(dao).getWallpaperById(40L)
        }
    }

    @Test
    fun varietyStrategyFallbackWhenVarietyQueryReturnsNull() {
        runBlocking {
            val dao = mock<WallpaperDao>()
            whenever(dao.getWallpaperById(40L)).thenReturn(null)
            whenever(dao.getSingleWallpaperId(any()))
                .thenReturn(null)
                .thenReturn(42L)

            val strategy = VarietySelectionStrategy()
            val result = strategy.selectWallpaperWithReason(dao, "ALL", null, null, 40L)

            assertNotNull(result)
            assertEquals(42L, result!!.wallpaperId)
            assertEquals("Variety Mode", result.reason)
        }
    }

    @Test
    fun timeOfDayStrategySelectsWithProfile() {
        runBlocking {
            val dao = mock<WallpaperDao>()
            whenever(dao.getSingleWallpaperId(any())).thenReturn(50L)

            val nightCal = java.util.Calendar.getInstance().apply {
                set(java.util.Calendar.HOUR_OF_DAY, 23)
                set(java.util.Calendar.MINUTE, 0)
            }

            val strategy = TimeOfDaySelectionStrategy(calendarProvider = { nightCal })
            val result = strategy.selectWallpaperWithReason(dao, "ALL", null, null, null)

            assertNotNull(result)
            assertEquals(50L, result!!.wallpaperId)
            assertTrue(result.reason.startsWith("Time of Day"))
        }
    }

    @Test
    fun selectionStrategyFactoryReturnsCorrectInstances() {
        assertTrue(SelectionStrategyFactory.getStrategy("TIME_OF_DAY") is TimeOfDaySelectionStrategy)
        assertTrue(SelectionStrategyFactory.getStrategy("SOLAR") is TimeOfDaySelectionStrategy)
        assertTrue(SelectionStrategyFactory.getStrategy("SMART_SHUFFLE") is SmartShuffleSelectionStrategy)
        assertTrue(SelectionStrategyFactory.getStrategy("SMART") is SmartShuffleSelectionStrategy)
        assertTrue(SelectionStrategyFactory.getStrategy("WEIGHTED_FAVORITES") is WeightedFavoritesSelectionStrategy)
        assertTrue(SelectionStrategyFactory.getStrategy("FAVORITES_WEIGHTED") is WeightedFavoritesSelectionStrategy)
        assertTrue(SelectionStrategyFactory.getStrategy("NEVER_REPEAT") is NeverRepeatSelectionStrategy)
        assertTrue(SelectionStrategyFactory.getStrategy("VARIETY") is VarietySelectionStrategy)
        assertTrue(SelectionStrategyFactory.getStrategy("SEQUENTIAL") is SequentialSelectionStrategy)
        assertTrue(SelectionStrategyFactory.getStrategy("LRU") is LeastRecentlyUsedSelectionStrategy)
        assertTrue(SelectionStrategyFactory.getStrategy("LEAST_RECENTLY_USED") is LeastRecentlyUsedSelectionStrategy)
        assertTrue(SelectionStrategyFactory.getStrategy("RANDOM") is RandomSelectionStrategy)
        assertTrue(SelectionStrategyFactory.getStrategy("UNKNOWN_MODE") is RandomSelectionStrategy)
    }
}
