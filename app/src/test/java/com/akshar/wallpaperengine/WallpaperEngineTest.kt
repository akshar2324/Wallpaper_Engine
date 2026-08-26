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
        assertEquals(SortOrder.RECENTLY_ADDED, options.sortOrder)
        assertEquals(0f, options.minRating)
        assertEquals(false, options.darkOnly)
        assertEquals(null, options.style)
        assertEquals(null, options.mood)
    }

    @Test
    fun testCustomFilterOptions() {
        val options = FilterOptions(
            searchQuery = "cyber",
            favoritesOnly = true,
            minRating = 4.5f,
            darkOnly = true,
            style = "Cyberpunk",
            mood = "Energetic"
        )
        assertEquals("cyber", options.searchQuery)
        assertTrue(options.favoritesOnly)
        assertEquals(4.5f, options.minRating)
        assertTrue(options.darkOnly)
        assertEquals("Cyberpunk", options.style)
        assertEquals("Energetic", options.mood)
    }

    @Test
    fun testWallpaperEntityCreation() {
        val wallpaper = WallpaperEntity(
            id = 1L,
            uri = "sample_nebula",
            title = "Cyber Nebula",
            width = 1080,
            height = 2400,
            isFavorite = true,
            rating = 4.5f,
            likeCount = 10,
            skipCount = 1,
            dominantColor = 0xFF0D0B18.toInt(),
            secondaryColor = 0xFF7C4DFF.toInt(),
            brightness = 0.15f,
            isDark = true,
            style = "Abstract",
            mood = "Mysterious"
        )

        assertNotNull(wallpaper)
        assertEquals("Cyber Nebula", wallpaper.title)
        assertTrue(wallpaper.isFavorite)
        assertEquals(4.5f, wallpaper.rating)
        assertEquals(10, wallpaper.likeCount)
        assertEquals(1, wallpaper.skipCount)
        assertTrue(wallpaper.isDark)
        assertEquals("Abstract", wallpaper.style)
        assertEquals("Mysterious", wallpaper.mood)
    }
}
