package com.akshar.wallpaperengine

import com.akshar.wallpaperengine.data.local.entity.WallpaperEntity
import com.akshar.wallpaperengine.data.repository.FilterOptions
import com.akshar.wallpaperengine.data.repository.SortOrder
import com.akshar.wallpaperengine.shader.ShaderIntensity
import com.akshar.wallpaperengine.shader.ShaderStyle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WallpaperEngineTest {

    @Test
    fun testShaderStyleResolution() {
        val nebula = ShaderStyle.fromId("NEBULA")
        assertEquals(ShaderStyle.NEBULA, nebula)

        val defaultStyle = ShaderStyle.fromId("NON_EXISTENT")
        assertEquals(ShaderStyle.THEME_DEFAULT, defaultStyle)
    }

    @Test
    fun testShaderIntensityResolution() {
        val high = ShaderIntensity.fromId("HIGH")
        assertEquals(ShaderIntensity.HIGH, high)

        val medium = ShaderIntensity.fromId("INVALID")
        assertEquals(ShaderIntensity.MEDIUM, medium)
    }

    @Test
    fun testDefaultFilterOptions() {
        val options = FilterOptions()
        assertEquals("", options.searchQuery)
        assertEquals(false, options.favoritesOnly)
        assertEquals(SortOrder.DATE_ADDED_DESC, options.sortOrder)
    }

    @Test
    fun testWallpaperEntityCreation() {
        val wallpaper = WallpaperEntity(
            id = 1L,
            uri = "sample_nebula",
            title = "Cyber Nebula",
            width = 1080,
            height = 2400,
            isFavorite = true
        )

        assertNotNull(wallpaper)
        assertEquals("Cyber Nebula", wallpaper.title)
        assertTrue(wallpaper.isFavorite)
    }
}
