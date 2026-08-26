package com.akshar.wallpaperengine.domain.usecase

import com.akshar.wallpaperengine.data.local.dao.HistoryDao
import com.akshar.wallpaperengine.data.local.dao.ScheduleDao
import com.akshar.wallpaperengine.data.local.dao.WallpaperDao
import com.akshar.wallpaperengine.data.local.entity.WallpaperEntity
import com.akshar.wallpaperengine.data.local.entity.WallpaperHistoryEntity
import com.akshar.wallpaperengine.domain.strategy.SelectionResult
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

        var selectedResult: SelectionResult? = null
        var attempts = 0
        var success = false
        var selectedWallpaper: WallpaperEntity? = null

        var lastId = current?.id

        while (attempts < 5 && !success) {
            selectedResult = strategy.selectWallpaperWithReason(
                wallpaperDao = wallpaperDao,
                sourceType = schedule.sourceType,
                collectionId = schedule.sourceCollectionId,
                specificWallpaperId = schedule.specificWallpaperId,
                lastSelectedId = lastId
            )

            if (selectedResult == null) break // Pool exhausted or empty

            val selectedId = selectedResult.wallpaperId
            selectedWallpaper = wallpaperDao.getWallpaperById(selectedId)

            if (selectedWallpaper != null) {
                success = wallpaperService.applyWallpaper(selectedWallpaper, schedule.targetScreen)
                if (!success) {
                    lastId = selectedId
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
                    scheduleId = schedule.id,
                    selectionReason = selectedResult?.reason ?: "Scheduled Rotation"
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

    /**
     * Executes context-aware dynamic rotation triggered by hardware/environmental events
     * (e.g. Battery Saver, Power Connected/Disconnected, DND, Time-of-Day shift).
     */
    suspend fun rotateWithContextTrigger(triggerType: String, reasonOverride: String? = null): Boolean {
        val triggerSchedules = scheduleDao.getEnabledSchedulesByTrigger(triggerType)
        val schedule = triggerSchedules.firstOrNull()

        val selectionMode = schedule?.selectionMode ?: when (triggerType) {
            "BATTERY_SAVER" -> "TIME_OF_DAY"
            "CHARGING" -> "VARIETY"
            "DND" -> "TIME_OF_DAY"
            else -> "TIME_OF_DAY"
        }
        val targetScreen = schedule?.targetScreen ?: "HOME_AND_LOCK"
        val sourceType = schedule?.sourceType ?: "ALL"
        val collectionId = schedule?.sourceCollectionId
        val specificWallpaperId = schedule?.specificWallpaperId

        val strategy = SelectionStrategyFactory.getStrategy(selectionMode)
        val current = wallpaperDao.getCurrentWallpaper()

        var selectedResult: SelectionResult? = null
        var attempts = 0
        var success = false
        var selectedWallpaper: WallpaperEntity? = null
        var lastId = current?.id

        while (attempts < 5 && !success) {
            selectedResult = strategy.selectWallpaperWithReason(
                wallpaperDao = wallpaperDao,
                sourceType = sourceType,
                collectionId = collectionId,
                specificWallpaperId = specificWallpaperId,
                lastSelectedId = lastId
            )

            if (selectedResult == null) break

            val selectedId = selectedResult.wallpaperId
            selectedWallpaper = wallpaperDao.getWallpaperById(selectedId)

            if (selectedWallpaper != null) {
                success = wallpaperService.applyWallpaper(selectedWallpaper, targetScreen)
                if (!success) {
                    lastId = selectedId
                }
            }
            attempts++
        }

        if (success && selectedWallpaper != null) {
            val now = System.currentTimeMillis()
            wallpaperDao.updateLastUsed(selectedWallpaper.id, now)

            val reason = reasonOverride ?: selectedResult?.reason ?: "Context Trigger ($triggerType)"

            historyDao.insertHistoryRecord(
                WallpaperHistoryEntity(
                    wallpaperId = selectedWallpaper.id,
                    wallpaperTitle = selectedWallpaper.title,
                    wallpaperUri = selectedWallpaper.uri,
                    appliedAt = now,
                    targetScreen = targetScreen,
                    source = "CONTEXT_TRIGGER",
                    scheduleId = schedule?.id,
                    selectionReason = reason
                )
            )

            if (schedule != null) {
                scheduleDao.updateScheduleExecutionTime(schedule.id, now, now)
            }
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
