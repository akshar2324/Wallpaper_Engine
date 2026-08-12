package com.akshar.wallpaperengine.domain.usecase

import android.util.Log
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
            // Basic conflict resolution / handling: Just take the most recently triggered enabled schedule
            scheduleDao.getEnabledSchedules().filter { isScheduleDayActive(it.activeDaysCsv) }
                .minByOrNull { it.nextExecution ?: 0L }
        } ?: return false

        if (!schedule.isEnabled || !isScheduleDayActive(schedule.activeDaysCsv)) return false

        val strategy = SelectionStrategyFactory.getStrategy(schedule.selectionMode)
        val current = wallpaperDao.getCurrentWallpaper()

        var selectedId: Long? = null
        var attempts = 0
        var success = false
        var selectedWallpaper: com.akshar.wallpaperengine.data.local.entity.WallpaperEntity? = null

        var lastId = current?.id

        while (attempts < 5 && !success) {
            try {
                selectedId = strategy.selectWallpaper(
                    wallpaperDao = wallpaperDao,
                    sourceType = schedule.sourceType,
                    collectionId = schedule.sourceCollectionId, // Also acts as playlistId if source is playlist
                    specificWallpaperId = schedule.specificWallpaperId,
                    lastSelectedId = lastId
                )
            } catch (e: Exception) {
                Log.e("RotateWallpaperUseCase", "Strategy failed: ${e.message}")
                break
            }

            if (selectedId == null) {
                Log.w("RotateWallpaperUseCase", "Source pool exhausted or invalid.")
                break
            }

            selectedWallpaper = wallpaperDao.getWallpaperById(selectedId!!)

            if (selectedWallpaper != null) {
                success = wallpaperService.applyWallpaper(selectedWallpaper, schedule.targetScreen, null) // Defaulting position null for automated tasks for now
                if (!success) {
                    lastId = selectedId
                    Log.w("RotateWallpaperUseCase", "Failed to apply wallpaper id: $selectedId, URI might be dead.")
                }
            } else {
                lastId = selectedId
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
        } else {
            // Still update execution time so we don't get stuck in a loop trying a broken schedule every minute
            scheduleDao.updateScheduleExecutionTime(
                id = schedule.id,
                lastExecution = System.currentTimeMillis(),
                nextExecution = calculateNextExecutionTime(schedule.timeHour, schedule.timeMinute, schedule.activeDaysCsv)
            )
        }

        return false
    }

    private fun isScheduleDayActive(daysCsv: String): Boolean {
        val today = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
        val dayString = when(today) {
            Calendar.MONDAY -> "MON"
            Calendar.TUESDAY -> "TUE"
            Calendar.WEDNESDAY -> "WED"
            Calendar.THURSDAY -> "THU"
            Calendar.FRIDAY -> "FRI"
            Calendar.SATURDAY -> "SAT"
            Calendar.SUNDAY -> "SUN"
            else -> ""
        }
        return daysCsv.contains(dayString)
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

        // Find next valid day
        var attempts = 0
        while (!isScheduleDayActiveForCalendar(calendar, daysCsv) && attempts < 7) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
            attempts++
        }

        return calendar.timeInMillis
    }

    private fun isScheduleDayActiveForCalendar(cal: Calendar, daysCsv: String): Boolean {
        val day = cal.get(Calendar.DAY_OF_WEEK)
        val dayString = when(day) {
            Calendar.MONDAY -> "MON"
            Calendar.TUESDAY -> "TUE"
            Calendar.WEDNESDAY -> "WED"
            Calendar.THURSDAY -> "THU"
            Calendar.FRIDAY -> "FRI"
            Calendar.SATURDAY -> "SAT"
            Calendar.SUNDAY -> "SUN"
            else -> ""
        }
        return daysCsv.contains(dayString)
    }
}
