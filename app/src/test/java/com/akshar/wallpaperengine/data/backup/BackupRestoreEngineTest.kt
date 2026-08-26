package com.akshar.wallpaperengine.data.backup

import android.app.Application
import com.akshar.wallpaperengine.data.local.dao.*
import com.akshar.wallpaperengine.data.local.entity.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.*
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class)
class BackupRestoreEngineTest {

    @Test
    fun testExportAndRestoreBackupJsonRoundTrip() {
        runBlocking {
            val wallpaperDao = mock<WallpaperDao>()
            val collectionDao = mock<CollectionDao>()
            val tagDao = mock<TagDao>()
            val scheduleDao = mock<ScheduleDao>()
            val historyDao = mock<HistoryDao>()

            val wallpaper = WallpaperEntity(
                id = 100L,
                uri = "sample_100",
                title = "Backup Test Wallpaper",
                style = "Cyberpunk",
                mood = "Ethereal",
                rating = 4.5f
            )
            val collection = CollectionEntity(id = 10L, name = "Cyber Vault")
            val tag = TagEntity(id = 5L, name = "neon")
            val schedule = ScheduleEntity(id = 1L, name = "Morning Rotation", timeHour = 7, timeMinute = 30)
            val history = WallpaperHistoryEntity(
                id = 1L,
                wallpaperId = 100L,
                wallpaperTitle = "Backup Test Wallpaper",
                wallpaperUri = "sample_100",
                appliedAt = 123456L
            )

            whenever(wallpaperDao.getAllWallpapersList()).thenReturn(listOf(wallpaper))
            whenever(collectionDao.getAllCollectionsList()).thenReturn(listOf(collection))
            whenever(tagDao.getAllTagsList()).thenReturn(listOf(tag))
            whenever(wallpaperDao.getAllCollectionCrossRefs()).thenReturn(listOf(WallpaperCollectionCrossRef(100L, 10L)))
            whenever(wallpaperDao.getAllTagCrossRefs()).thenReturn(listOf(WallpaperTagCrossRef(100L, 5L)))
            whenever(scheduleDao.getAllSchedulesList()).thenReturn(listOf(schedule))
            whenever(historyDao.getAllHistoryList()).thenReturn(listOf(history))

            val engine = BackupRestoreEngine(wallpaperDao, collectionDao, tagDao, scheduleDao, historyDao)
            val exportedJson = engine.exportBackupJson()

            assertTrue(exportedJson.contains("Backup Test Wallpaper"))
            assertTrue(exportedJson.contains("Cyber Vault"))
            assertTrue(exportedJson.contains("neon"))

            // Now test restore
            val summary = engine.restoreBackupJson(exportedJson)

            assertEquals(1, summary.wallpaperCount)
            assertEquals(1, summary.collectionCount)
            assertEquals(1, summary.tagCount)
            assertEquals(1, summary.scheduleCount)
            assertEquals(1, summary.historyCount)

            verify(wallpaperDao).insertWallpapers(any())
            verify(collectionDao).insertCollections(any())
            verify(tagDao).insertTags(any())
            verify(scheduleDao).insertSchedules(any())
            verify(historyDao).insertHistoryRecords(any())
        }
    }
}
