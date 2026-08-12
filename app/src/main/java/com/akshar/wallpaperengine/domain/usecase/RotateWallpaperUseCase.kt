package com.akshar.wallpaperengine.domain.usecase

import com.akshar.wallpaperengine.data.local.dao.HistoryDao
import com.akshar.wallpaperengine.data.local.dao.ScheduleDao
import com.akshar.wallpaperengine.data.local.dao.WallpaperDao
import com.akshar.wallpaperengine.data.local.entity.ScheduleEntity
import com.akshar.wallpaperengine.data.local.entity.WallpaperHistoryEntity
import com.akshar.wallpaperengine.domain.strategy.SelectionStrategyFactory
import com.akshar.wallpaperengine.wallpaper.AndroidWallpaperService
import java.util.Calendar

class RotateWallpaperUseCase(
    private val wallpaperDao: WallpaperDao,
    private val scheduleDao: ScheduleDao,
    private val historyDao: HistoryDao,
    private val wallpaperService: AndroidWallpaperService
) {

    suspend operator fun invoke(scheduleId: Long = -1L): Boolean {
        val schedule = if (scheduleId != -1L) {
            scheduleDao.getScheduleById(scheduleId)
        } else {
            scheduleDao.getEnabledSchedules().firstOrNull()
        } ?: return false

        if (!schedule.isEnabled) return false

        val strategy = SelectionStrategyFactory.getStrategy(schedule.selectionMode)
        val current = wallpaperDao.getCurrentWallpaper()

        var selectedId: Long? = null
        var attempts = 0
        var success = false
        var selectedWallpaper: com.akshar.wallpaperengine.data.local.entity.WallpaperEntity? = null

        var lastId = current?.id

        while (attempts < 5 && !success) {
            selectedId = strategy.selectWallpaper(
                wallpaperDao = wallpaperDao,
                sourceType = schedule.sourceType,
                collectionId = schedule.sourceCollectionId,
                specificWallpaperId = schedule.specificWallpaperId,
                lastSelectedId = lastId
            )

            if (selectedId == null) break // Pool exhausted or empty

            selectedWallpaper = wallpaperDao.getWallpaperById(selectedId!!)

            if (selectedWallpaper != null) {
                success = wallpaperService.applyWallpaper(selectedWallpaper, schedule.targetScreen)
                if (!success) {
                    // Dead URI or system rejection. Update lastId to try a different wallpaper
                    lastId = selectedId

                    // Optional: Mark as invalid, delete it, or just let it be skipped next time.
                    // For now, we just skip. If we want to clean it:
                    // wallpaperDao.deleteWallpaper(selectedWallpaper)
                }
            }
            attempts++
        }

        if (success && selectedWallpaper != null) {
            val now = System.currentTimeMillis()
            wallpaperDao.updateLastUsed(selectedWallpaper.id, now)

            historyDao.insertHistoryRecord(
                WallpaperHistoryEntity(
                    wallpaperId = selectedWallpaper.id,
                    wallpaperTitle = selectedWallpaper.title,
                    wallpaperUri = selectedWallpaper.uri,
                    appliedAt = now,
                    targetScreen = schedule.targetScreen,
                    source = "SCHEDULE",
                    scheduleId = schedule.id
                )
            )

            scheduleDao.updateScheduleExecutionTime(
                id = schedule.id,
                lastExecution = now,
                nextExecution = calculateNextExecutionTime(schedule.timeHour, schedule.timeMinute, schedule.activeDaysCsv)
            )
            return true
        }

        return false
    }

    private fun calculateNextExecutionTime(hour: Int, minute: Int, daysCsv: String): Long {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        return calendar.timeInMillis
    }
}
