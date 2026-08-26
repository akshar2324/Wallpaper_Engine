package com.akshar.wallpaperengine.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.akshar.wallpaperengine.WallpaperEngineApplication

/**
 * Background WorkManager worker for on-device ML metadata tagging,
 * perceptual hash indexing, and visual similarity analysis.
 * Runs on IO dispatcher with battery/idle constraints.
 */
class WallpaperTaggingWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val app = applicationContext as? WallpaperEngineApplication
                ?: return Result.failure()

            app.wallpaperRepository.backfillAiTagsAndHashes()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
