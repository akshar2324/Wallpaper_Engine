package com.akshar.wallpaperengine.data.maintenance

import android.app.Application
import android.content.Context
import com.akshar.wallpaperengine.data.local.dao.WallpaperDao
import com.akshar.wallpaperengine.data.local.entity.WallpaperEntity
import com.akshar.wallpaperengine.data.repository.WallpaperRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.*
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class)
class LibraryHealthManagerTest {

    @Test
    fun testScanLibraryHealthIdentifiesLowResAndUnused() {
        runBlocking {
            val context = mock<Context>()
            val dao = mock<WallpaperDao>()
            val repo = mock<WallpaperRepository>()

            val oldTimestamp = System.currentTimeMillis() - (40L * 24L * 60L * 60L * 1000L)

            val wallpaper1 = WallpaperEntity(id = 1L, uri = "sample_1", title = "Low Res Sample", width = 800, height = 600, fileSize = 1000L, isSample = true, dateAdded = oldTimestamp)
            val wallpaper2 = WallpaperEntity(id = 2L, uri = "sample_2", title = "HD Wallpaper", width = 1920, height = 1080, fileSize = 5000L, isSample = true, dateAdded = System.currentTimeMillis(), isFavorite = true)

            whenever(dao.getAllWallpapersList()).thenReturn(listOf(wallpaper1, wallpaper2))
            whenever(repo.findDuplicateClusters()).thenReturn(emptyList())

            val manager = LibraryHealthManager(context, dao, repo)
            val report = manager.scanLibraryHealth()

            assertEquals(2, report.totalWallpapers)
            assertEquals(6000L, report.totalStorageBytes)
            assertEquals(1, report.lowResWallpapers.size)
            assertEquals(1L, report.lowResWallpapers.first().id)
            assertEquals(1, report.unusedWallpapers.size)
            assertEquals(1L, report.unusedWallpapers.first().id)
        }
    }

    @Test
    fun testCleanDuplicatesKeepsHighestResolution() {
        runBlocking {
            val context = mock<Context>()
            val dao = mock<WallpaperDao>()
            val repo = mock<WallpaperRepository>()

            val original = WallpaperEntity(id = 10L, uri = "sample_10", title = "Original 4K", width = 3840, height = 2160, fileSize = 8000L, isSample = true)
            val dupLowRes = WallpaperEntity(id = 11L, uri = "sample_11", title = "Dup 1080p", width = 1920, height = 1080, fileSize = 2000L, isSample = true)

            whenever(repo.findDuplicateClusters()).thenReturn(listOf(listOf(original, dupLowRes)))

            val manager = LibraryHealthManager(context, dao, repo)
            val removedCount = manager.cleanDuplicates(keepHighestResolution = true)

            assertEquals(1, removedCount)
            verify(dao).deleteWallpapersByIds(listOf(11L))
        }
    }
}
