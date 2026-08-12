package com.akshar.wallpaperengine.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.akshar.wallpaperengine.WallpaperEngineApplication
import com.akshar.wallpaperengine.data.local.entity.WallpaperHistoryEntity
import com.akshar.wallpaperengine.domain.strategy.SelectionStrategyFactory
import java.util.Calendar

class WallpaperChangeWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val app = applicationContext as WallpaperEngineApplication
        val scheduleId = inputData.getLong("schedule_id", -1L)
        
        val database = app.database
        val scheduleDao = database.scheduleDao()
        val wallpaperDao = database.wallpaperDao()
        val historyDao = database.historyDao()
        val wallpaperService = app.wallpaperService

        val schedule = if (scheduleId != -1L) {
            scheduleDao.getScheduleById(scheduleId)
        } else {
            scheduleDao.getEnabledSchedules().firstOrNull()
        } ?: return Result.success()

        if (!schedule.isEnabled) return Result.success()

        // Filter source wallpapers using IDs to save memory
        val candidateWallpaperIds = when (schedule.sourceType.uppercase()) {
            "COLLECTION" -> {
                schedule.sourceCollectionId?.let { colId ->
                    wallpaperDao.getWallpaperIdsForCollection(colId)
                } ?: emptyList()
            }
            "FAVORITES" -> wallpaperDao.getFavoriteWallpaperIds()
            "SPECIFIC" -> {
                schedule.specificWallpaperId?.let { id -> listOf(id) } ?: emptyList()
            }
            else -> wallpaperDao.getAllWallpaperIds()
        }

        if (candidateWallpaperIds.isEmpty()) return Result.success()

        val strategy = SelectionStrategyFactory.getStrategy(schedule.selectionMode)
        val current = wallpaperDao.getCurrentWallpaper()
        
        // Loop up to 5 times to find a valid URI in case some were deleted from storage (Dead URI fallback)
        var selectedId: Long? = null
        var attempts = 0
        var success = false
        var selectedWallpaper: com.akshar.wallpaperengine.data.local.entity.WallpaperEntity? = null
        
        val candidatesMutable = candidateWallpaperIds.toMutableList()
        var lastId = current?.id
        
        while (attempts < 5 && candidatesMutable.isNotEmpty() && !success) {
            selectedId = strategy.selectWallpaper(candidatesMutable, lastId) ?: candidatesMutable.random()
            selectedWallpaper = wallpaperDao.getWallpaperById(selectedId)
            
            if (selectedWallpaper != null) {
                // Attempt to apply it
                success = wallpaperService.applyWallpaper(selectedWallpaper, schedule.targetScreen)
                if (!success) {
                    // Dead URI or application failed. Remove it from candidates and try another.
                    candidatesMutable.remove(selectedId)
                    lastId = selectedId
                }
            } else {
                candidatesMutable.remove(selectedId)
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
        }

        return Result.success()
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
