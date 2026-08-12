package com.akshar.wallpaperengine.domain.strategy

import androidx.sqlite.db.SimpleSQLiteQuery
import com.akshar.wallpaperengine.data.local.dao.WallpaperDao
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*

class SelectionStrategyTest {

    @Test
    fun `RandomStrategy builds query with RANDOM()`() = runBlocking {
        val dao = mock(WallpaperDao::class.java)
        `when`(dao.getSingleWallpaperId(any(SimpleSQLiteQuery::class.java))).thenReturn(5L)

        val strategy = RandomSelectionStrategy()
        val result = strategy.selectWallpaper(dao, "ALL", null, null, null)

        assertEquals(5L, result)
    }

    @Test
    fun `SequentialStrategy builds query for next ID`() = runBlocking {
        val dao = mock(WallpaperDao::class.java)
        `when`(dao.getSingleWallpaperId(any(SimpleSQLiteQuery::class.java))).thenReturn(2L)

        val strategy = SequentialSelectionStrategy()
        val result = strategy.selectWallpaper(dao, "ALL", null, null, 1L)

        assertEquals(2L, result)
    }
}
