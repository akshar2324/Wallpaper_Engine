package com.akshar.wallpaperengine.domain.usecase

import com.akshar.wallpaperengine.data.local.dao.HistoryDao
import com.akshar.wallpaperengine.data.local.dao.ScheduleDao
import com.akshar.wallpaperengine.data.local.dao.WallpaperDao
import com.akshar.wallpaperengine.data.local.entity.ScheduleEntity
import com.akshar.wallpaperengine.data.local.entity.WallpaperEntity
import com.akshar.wallpaperengine.wallpaper.AndroidWallpaperService
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*

class RotateWallpaperUseCaseTest {

    @Test
    fun `returns false if schedule is disabled`() = runBlocking {
        val wallpaperDao = mock(WallpaperDao::class.java)
        val scheduleDao = mock(ScheduleDao::class.java)
        val historyDao = mock(HistoryDao::class.java)
        val wallpaperService = mock(AndroidWallpaperService::class.java)

        val disabledSchedule = ScheduleEntity(id = 1L, name = "Test", isEnabled = false)
        `when`(scheduleDao.getScheduleById(1L)).thenReturn(disabledSchedule)

        val useCase = RotateWallpaperUseCase(wallpaperDao, scheduleDao, historyDao, wallpaperService)
        val result = useCase(1L)

        assertFalse(result)
        verify(wallpaperService, never()).applyWallpaper(any(), any())
    }

    @Test
    fun `applies wallpaper successfully if valid`() = runBlocking {
        val wallpaperDao = mock(WallpaperDao::class.java)
        val scheduleDao = mock(ScheduleDao::class.java)
        val historyDao = mock(HistoryDao::class.java)
        val wallpaperService = mock(AndroidWallpaperService::class.java)

        val schedule = ScheduleEntity(id = 1L, name = "Test", isEnabled = true, selectionMode = "RANDOM", sourceType = "ALL")
        val wallpaper = WallpaperEntity(id = 10L, uri = "test", title = "test")

        `when`(scheduleDao.getScheduleById(1L)).thenReturn(schedule)
        `when`(wallpaperDao.getSingleWallpaperId(any())).thenReturn(10L)
        `when`(wallpaperDao.getWallpaperById(10L)).thenReturn(wallpaper)
        `when`(wallpaperService.applyWallpaper(wallpaper, schedule.targetScreen)).thenReturn(true)

        val useCase = RotateWallpaperUseCase(wallpaperDao, scheduleDao, historyDao, wallpaperService)
        val result = useCase(1L)

        assertTrue(result)
        verify(wallpaperService).applyWallpaper(wallpaper, schedule.targetScreen)
        verify(historyDao).insertHistoryRecord(any())
    }
}
