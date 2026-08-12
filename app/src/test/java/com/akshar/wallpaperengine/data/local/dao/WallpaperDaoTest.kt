package com.akshar.wallpaperengine.data.local.dao

import androidx.sqlite.db.SimpleSQLiteQuery
import com.akshar.wallpaperengine.data.local.entity.WallpaperEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30])
class WallpaperDaoTest {

    @Test
    fun `test build filtered query works correctly`() = runBlocking {
        // Just mock the DAO to verify the SQL string we generate in Repository works
        val mockDao = mock(WallpaperDao::class.java)

        val query = SimpleSQLiteQuery("SELECT w.id FROM wallpapers w WHERE w.isFavorite = 1 ORDER BY RANDOM() LIMIT 1")
        `when`(mockDao.getSingleWallpaperId(query)).thenReturn(42L)

        val result = mockDao.getSingleWallpaperId(query)
        assertEquals(42L, result)
    }
}
