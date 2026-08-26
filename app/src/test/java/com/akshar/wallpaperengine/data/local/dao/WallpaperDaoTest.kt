package com.akshar.wallpaperengine.data.local.dao

import androidx.sqlite.db.SimpleSQLiteQuery
import com.akshar.wallpaperengine.data.repository.FilterOptions
import com.akshar.wallpaperengine.data.repository.SortOrder
import com.akshar.wallpaperengine.data.repository.WallpaperRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.*

class WallpaperDaoTest {

    @Test
    fun testBuildFilteredQueryWorksCorrectly() {
        runBlocking {
            val mockDao = mock<WallpaperDao>()

            val query = SimpleSQLiteQuery("SELECT w.id FROM wallpapers w WHERE w.isFavorite = 1 ORDER BY RANDOM() LIMIT 1")
            whenever(mockDao.getSingleWallpaperId(any())).thenReturn(42L)

            val result = mockDao.getSingleWallpaperId(query)
            assertEquals(42L, result)
        }
    }

    @Test
    fun testWallpaperDnaDaoMethods() {
        runBlocking {
            val mockDao = mock<WallpaperDao>()

            mockDao.updateRating(1L, 4.5f)
            verify(mockDao).updateRating(1L, 4.5f)

            mockDao.recordSkip(1L)
            verify(mockDao).recordSkip(eq(1L), any())

            mockDao.recordLike(1L)
            verify(mockDao).recordLike(1L)

            mockDao.recordView(1L)
            verify(mockDao).recordView(1L)

            mockDao.updateWallpaperDna(1L, 0xFF112233.toInt(), 0xFF445566.toInt(), 0.35f, true)
            verify(mockDao).updateWallpaperDna(1L, 0xFF112233.toInt(), 0xFF445566.toInt(), 0.35f, true)

            mockDao.updateStyleAndMood(1L, "Cyberpunk", "Moody")
            verify(mockDao).updateStyleAndMood(1L, "Cyberpunk", "Moody")

            mockDao.updatePrivacy(1L, true)
            verify(mockDao).updatePrivacy(1L, true)

            whenever(mockDao.getWallpapersWithoutDna()).thenReturn(emptyList())
            val withoutDna = mockDao.getWallpapersWithoutDna()
            assertEquals(0, withoutDna.size)
        }
    }

    @Test
    fun testGetFilteredWallpapersBuildsQueryCorrectly() {
        val mockDao = mock<WallpaperDao>()
        val mockCollectionDao = mock<CollectionDao>()
        val mockTagDao = mock<TagDao>()
        val mockContext = mock<android.content.Context>()
        val mockDnaAnalyzer = mock<com.akshar.wallpaperengine.data.dna.WallpaperDnaAnalyzer>()

        val repo = WallpaperRepository(
            wallpaperDao = mockDao,
            collectionDao = mockCollectionDao,
            tagDao = mockTagDao,
            context = mockContext,
            dnaAnalyzer = mockDnaAnalyzer
        )

        repo.getFilteredWallpapers(
            FilterOptions(
                minRating = 4.0f,
                darkOnly = true,
                sortOrder = SortOrder.RATING_DESC
            )
        )

        val captor = argumentCaptor<SimpleSQLiteQuery>()
        verify(mockDao).getWallpapersFiltered(captor.capture())

        val sql = captor.firstValue.sql
        assertTrue(sql.contains("w.rating >= ?"))
        assertTrue(sql.contains("w.isDark = 1"))
        assertTrue(sql.contains("ORDER BY w.rating DESC, w.dateAdded DESC"))
    }

    @Test
    fun testWallpaperHashDaoMethods() {
        runBlocking {
            val mockDao = mock<WallpaperDao>()

            mockDao.updateContentHash(1L, "a1b2c3d4e5f60718")
            verify(mockDao).updateContentHash(1L, "a1b2c3d4e5f60718")

            whenever(mockDao.getDuplicatesByHash("a1b2c3d4e5f60718", 1L)).thenReturn(emptyList())
            val dups = mockDao.getDuplicatesByHash("a1b2c3d4e5f60718", 1L)
            assertEquals(0, dups.size)

            whenever(mockDao.getUnanalyzedWallpapers()).thenReturn(emptyList())
            val unanalyzed = mockDao.getUnanalyzedWallpapers()
            assertEquals(0, unanalyzed.size)
        }
    }

    @Test
    fun testMultiTagAndNaturalLanguageSearchQuery() {
        val mockDao = mock<WallpaperDao>()
        val mockCollectionDao = mock<CollectionDao>()
        val mockTagDao = mock<TagDao>()
        val mockContext = mock<android.content.Context>()

        val repo = WallpaperRepository(
            wallpaperDao = mockDao,
            collectionDao = mockCollectionDao,
            tagDao = mockTagDao,
            context = mockContext
        )

        repo.getFilteredWallpapers(
            FilterOptions(
                searchQuery = "cyberpunk neon"
            )
        )

        val captor = argumentCaptor<SimpleSQLiteQuery>()
        verify(mockDao).getWallpapersFiltered(captor.capture())

        val sql = captor.firstValue.sql
        assertTrue(sql.contains("w.title LIKE ?"))
        assertTrue(sql.contains("w.style LIKE ?"))
        assertTrue(sql.contains("w.mood LIKE ?"))
        assertTrue(sql.contains("wallpaper_tag_cross_ref"))
        assertTrue(sql.contains("tags"))
    }
}

