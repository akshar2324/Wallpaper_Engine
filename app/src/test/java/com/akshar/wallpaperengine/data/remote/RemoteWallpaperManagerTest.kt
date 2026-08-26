package com.akshar.wallpaperengine.data.remote

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.akshar.wallpaperengine.data.local.dao.WallpaperDao
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.*
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class)
class RemoteWallpaperManagerTest {

    @Test
    fun testCuratedCatalogueContainsValidCategoriesAndItems() {
        val categories = RemoteWallpaperCatalogue.getCuratedCategories()
        assertTrue(categories.contains("ALL"))
        assertTrue(categories.contains("AMOLED"))
        assertTrue(categories.contains("CYBERPUNK"))

        val featured = RemoteWallpaperCatalogue.getFeaturedWallpapers()
        assertTrue(featured.isNotEmpty())
        assertEquals(4, featured.size)

        val item = featured.first()
        assertEquals("rem_1", item.id)
        assertEquals("Neon Skyline 2077", item.title)
        assertEquals("CYBERPUNK", item.category)
        assertTrue(item.isDark)
    }

    @Test
    fun testRemoteWallpaperManagerInstantiation() {
        val context: Context = ApplicationProvider.getApplicationContext()
        val dao = mock<WallpaperDao>()
        val manager = RemoteWallpaperManager(context, dao)
        assertNotNull(manager)
    }
}
