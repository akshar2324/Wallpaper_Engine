package com.akshar.wallpaperengine.widget

import android.app.Application
import android.appwidget.AppWidgetManager
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.*
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class)
class WallpaperAppWidgetProviderTest {

    @Test
    fun testUpdateAppWidgetExecutesWithoutCrashing() {
        val context: Context = ApplicationProvider.getApplicationContext()
        val appWidgetManager = mock<AppWidgetManager>()

        WallpaperAppWidgetProvider.updateAppWidget(
            context = context,
            appWidgetManager = appWidgetManager,
            appWidgetId = 1,
            title = "Test Cyber Wallpaper",
            style = "Cyberpunk",
            mood = "Energetic",
            isFavorite = true,
            wallpaperId = 100L
        )

        verify(appWidgetManager).updateAppWidget(eq(1), any())
    }
}
