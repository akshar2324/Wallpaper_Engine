package com.akshar.wallpaperengine.domain.usecase

import com.akshar.wallpaperengine.data.local.dao.HistoryDao
import com.akshar.wallpaperengine.data.local.dao.ScheduleDao
import com.akshar.wallpaperengine.data.local.dao.WallpaperDao
import com.akshar.wallpaperengine.data.local.entity.ScheduleEntity
import com.akshar.wallpaperengine.data.local.entity.WallpaperEntity
import com.akshar.wallpaperengine.data.local.entity.WallpaperHistoryEntity
import com.akshar.wallpaperengine.wallpaper.AndroidWallpaperService
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.*

class RotateWallpaperUseCaseTest {

    @Test
    fun returnsFalseIfScheduleIsDisabled() {
        runBlocking {
            val wallpaperDao = mock<WallpaperDao>()
            val scheduleDao = mock<ScheduleDao>()
            val historyDao = mock<HistoryDao>()
            val wallpaperService = mock<AndroidWallpaperService>()

            val disabledSchedule = ScheduleEntity(id = 1L, name = "Test", isEnabled = false)
            whenever(scheduleDao.getScheduleById(1L)).thenReturn(disabledSchedule)

            val useCase = RotateWallpaperUseCase(wallpaperDao, scheduleDao, historyDao, wallpaperService)
            val result = useCase(1L)

            assertFalse(result)
            verify(wallpaperService, never()).applyWallpaper(any(), any())
        }
    }

    @Test
    fun appliesWallpaperSuccessfullyIfValid() {
        runBlocking {
            val wallpaperDao = mock<WallpaperDao>()
            val scheduleDao = mock<ScheduleDao>()
            val historyDao = mock<HistoryDao>()
            val wallpaperService = mock<AndroidWallpaperService>()

            val schedule = ScheduleEntity(id = 1L, name = "Test", isEnabled = true, selectionMode = "RANDOM", sourceType = "ALL")
            val wallpaper = WallpaperEntity(id = 10L, uri = "test", title = "test")

            whenever(scheduleDao.getScheduleById(1L)).thenReturn(schedule)
            whenever(wallpaperDao.getSingleWallpaperId(any())).thenReturn(10L)
            whenever(wallpaperDao.getWallpaperById(10L)).thenReturn(wallpaper)
            whenever(wallpaperService.applyWallpaper(eq(wallpaper), eq(schedule.targetScreen))).thenReturn(true)

            val useCase = RotateWallpaperUseCase(wallpaperDao, scheduleDao, historyDao, wallpaperService)
            val result = useCase(1L)

            assertTrue(result)
            verify(wallpaperService).applyWallpaper(eq(wallpaper), eq(schedule.targetScreen))

            val captor = argumentCaptor<WallpaperHistoryEntity>()
            verify(historyDao).insertHistoryRecord(captor.capture())
            val recordedHistory = captor.firstValue
            assertEquals(10L, recordedHistory.wallpaperId)
            assertEquals("test", recordedHistory.wallpaperTitle)
            assertEquals("test", recordedHistory.wallpaperUri)
            assertEquals("SCHEDULE", recordedHistory.source)
            assertEquals(1L, recordedHistory.scheduleId)
            assertEquals("Random Selection", recordedHistory.selectionReason)
        }
    }

    @Test
    fun appliesWallpaperWithSequentialSelectionReason() {
        runBlocking {
            val wallpaperDao = mock<WallpaperDao>()
            val scheduleDao = mock<ScheduleDao>()
            val historyDao = mock<HistoryDao>()
            val wallpaperService = mock<AndroidWallpaperService>()

            val schedule = ScheduleEntity(id = 2L, name = "Sequential Schedule", isEnabled = true, selectionMode = "SEQUENTIAL", sourceType = "ALL")
            val wallpaper = WallpaperEntity(id = 15L, uri = "content://img/15", title = "Seq Wallpaper")

            whenever(scheduleDao.getScheduleById(2L)).thenReturn(schedule)
            whenever(wallpaperDao.getSingleWallpaperId(any())).thenReturn(15L)
            whenever(wallpaperDao.getWallpaperById(15L)).thenReturn(wallpaper)
            whenever(wallpaperService.applyWallpaper(eq(wallpaper), eq(schedule.targetScreen))).thenReturn(true)

            val useCase = RotateWallpaperUseCase(wallpaperDao, scheduleDao, historyDao, wallpaperService)
            val result = useCase(2L)

            assertTrue(result)
            val captor = argumentCaptor<WallpaperHistoryEntity>()
            verify(historyDao).insertHistoryRecord(captor.capture())
            assertEquals("Sequential Step", captor.firstValue.selectionReason)
        }
    }

    @Test
    fun returnsFalseWhenNoWallpaperFound() {
        runBlocking {
            val wallpaperDao = mock<WallpaperDao>()
            val scheduleDao = mock<ScheduleDao>()
            val historyDao = mock<HistoryDao>()
            val wallpaperService = mock<AndroidWallpaperService>()

            val schedule = ScheduleEntity(id = 1L, name = "Test", isEnabled = true, selectionMode = "RANDOM", sourceType = "ALL")

            whenever(scheduleDao.getScheduleById(1L)).thenReturn(schedule)
            whenever(wallpaperDao.getSingleWallpaperId(any())).thenReturn(null)

            val useCase = RotateWallpaperUseCase(wallpaperDao, scheduleDao, historyDao, wallpaperService)
            val result = useCase(1L)

            assertFalse(result)
            verify(wallpaperService, never()).applyWallpaper(any(), any())
            verify(historyDao, never()).insertHistoryRecord(any())
        }
    }

    @Test
    fun testRotateWithContextTrigger() {
        runBlocking {
            val wallpaperDao = mock<WallpaperDao>()
            val scheduleDao = mock<ScheduleDao>()
            val historyDao = mock<HistoryDao>()
            val wallpaperService = mock<AndroidWallpaperService>()

            val wallpaper = WallpaperEntity(id = 25L, uri = "content://img/25", title = "OLED Void", isDark = true)

            whenever(scheduleDao.getEnabledSchedulesByTrigger("BATTERY_SAVER")).thenReturn(emptyList())
            whenever(wallpaperDao.getSingleWallpaperId(any())).thenReturn(25L)
            whenever(wallpaperDao.getWallpaperById(25L)).thenReturn(wallpaper)
            whenever(wallpaperService.applyWallpaper(eq(wallpaper), any())).thenReturn(true)

            val useCase = RotateWallpaperUseCase(wallpaperDao, scheduleDao, historyDao, wallpaperService)
            val result = useCase.rotateWithContextTrigger("BATTERY_SAVER", "Battery Saver (OLED Dark Mode)")

            assertTrue(result)
            val captor = argumentCaptor<WallpaperHistoryEntity>()
            verify(historyDao).insertHistoryRecord(captor.capture())
            assertEquals("CONTEXT_TRIGGER", captor.firstValue.source)
            assertEquals("Battery Saver (OLED Dark Mode)", captor.firstValue.selectionReason)
        }
    }
}
