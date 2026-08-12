package com.akshar.wallpaperengine.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.akshar.wallpaperengine.WallpaperEngineApplication
import com.akshar.wallpaperengine.data.local.entity.WallpaperHistoryEntity
import com.akshar.wallpaperengine.domain.strategy.SelectionStrategyFactory

/**
 * Worker responsible for rotating wallpapers based on user-defined frequency intervals.
 */
class WallpaperWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val app = applicationContext as WallpaperEngineApplication
        val scheduleId = inputData.getLong("schedule_id", -1L)
        
        val database = app.database
        val wallpaperDao = database.wallpaperDao()
        val historyDao = database.historyDao()
        val scheduleDao = database.scheduleDao()
        val wallpaperService = app.wallpaperService

        val schedule = if (scheduleId != -1L) {
            scheduleDao.getScheduleById(scheduleId)
        } else {
            scheduleDao.getEnabledSchedules().firstOrNull()
        }
        
        // Use optimized ID queries to avoid loading 10,000+ objects into memory
        val candidateWallpaperIds = if (schedule != null) {
            when (schedule.sourceType.uppercase()) {
                "COLLECTION" -> schedule.sourceCollectionId?.let { colId ->
                    wallpaperDao.getWallpaperIdsForCollection(colId)
                } ?: emptyList()
                "FAVORITES" -> wallpaperDao.getFavoriteWallpaperIds()
                "SPECIFIC" -> schedule.specificWallpaperId?.let { id -> listOf(id) } ?: emptyList()
                else -> wallpaperDao.getAllWallpaperIds()
            }
        } else {
            wallpaperDao.getAllWallpaperIds()
        }

        if (candidateWallpaperIds.isEmpty()) {
            return Result.success()
        }

        val selectionMode = schedule?.selectionMode ?: "RANDOM"
        val targetScreen = schedule?.targetScreen ?: "HOME_AND_LOCK"
        
        val strategy = SelectionStrategyFactory.getStrategy(selectionMode)
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
                success = wallpaperService.applyWallpaper(selectedWallpaper, targetScreen)
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
                    targetScreen = targetScreen,
                    source = "INTERVAL_WORKER",
                    scheduleId = if (scheduleId != -1L) scheduleId else null
                )
            )
            
            if (schedule != null) {
                scheduleDao.updateScheduleExecutionTime(
                    id = schedule.id,
                    lastExecution = now,
                    nextExecution = schedule.nextExecution ?: (now + 15 * 60 * 1000)
                )
            }
        }

        return Result.success()
    }
}
