package com.akshar.wallpaperengine.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.akshar.wallpaperengine.WallpaperEngineApplication
import com.akshar.wallpaperengine.domain.usecase.RotateWallpaperUseCase

class WallpaperChangeWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val app = applicationContext as WallpaperEngineApplication
        val scheduleId = inputData.getLong("schedule_id", -1L)
        
        val database = app.database
        val rotateWallpaperUseCase = RotateWallpaperUseCase(
            wallpaperDao = database.wallpaperDao(),
            scheduleDao = database.scheduleDao(),
            historyDao = database.historyDao(),
            wallpaperService = app.wallpaperService
        )
        
        rotateWallpaperUseCase(scheduleId)
        
        return Result.success()
    }
}
