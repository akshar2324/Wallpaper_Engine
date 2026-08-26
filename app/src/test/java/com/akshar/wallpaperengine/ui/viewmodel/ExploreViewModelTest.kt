package com.akshar.wallpaperengine.ui.viewmodel

import android.app.Application
import com.akshar.wallpaperengine.data.local.entity.WallpaperEntity
import com.akshar.wallpaperengine.data.remote.RemoteWallpaperItem
import com.akshar.wallpaperengine.data.remote.RemoteWallpaperManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.*
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class)
class ExploreViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testCategorySelectionFiltersWallpapers() = runTest(testDispatcher) {
        val remoteManager = mock<RemoteWallpaperManager>()
        val viewModel = ExploreViewModel(remoteManager)

        assertEquals("ALL", viewModel.selectedCategory.value)
        assertTrue(viewModel.wallpapers.value.isNotEmpty())

        viewModel.selectCategory("AMOLED")
        assertEquals("AMOLED", viewModel.selectedCategory.value)
        assertTrue(viewModel.wallpapers.value.all { it.category == "AMOLED" })
    }

    @Test
    fun testDownloadAndImportUpdatesState() = runTest(testDispatcher) {
        val remoteManager = mock<RemoteWallpaperManager>()
        val item = RemoteWallpaperItem(
            id = "rem_test",
            title = "Test Item",
            previewUrl = "http://preview",
            downloadUrl = "http://download",
            category = "AMOLED",
            style = "Sci-Fi",
            mood = "Calm",
            isDark = true,
            author = "Test Author"
        )
        whenever(remoteManager.downloadAndImportWallpaper(item)).thenReturn(
            Result.success(WallpaperEntity(id = 50L, uri = "file:///dest", title = "Test Item"))
        )

        val viewModel = ExploreViewModel(remoteManager)
        var callbackSuccess = false

        viewModel.downloadAndImport(item) { success ->
            callbackSuccess = success
        }

        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(callbackSuccess)
        assertTrue(viewModel.downloadedIds.value.contains("rem_test"))
        assertFalse(viewModel.downloadingIds.value.contains("rem_test"))
    }
}
