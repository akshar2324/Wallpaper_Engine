package com.akshar.wallpaperengine.scheduler

import android.content.Context
import androidx.work.*
import com.akshar.wallpaperengine.data.local.entity.ScheduleEntity
import com.akshar.wallpaperengine.workers.WallpaperChangeWorker
import java.util.Calendar
import java.util.concurrent.TimeUnit

class WallpaperScheduler(private val context: Context) {

    private val workManager = WorkManager.getInstance(context)

    fun scheduleRotation(schedule: ScheduleEntity) {
        val workTag = getWorkTag(schedule.id)
        workManager.cancelAllWorkByTag(workTag)

        if (!schedule.isEnabled) return

        val initialDelay = calculateInitialDelayMs(schedule.timeHour, schedule.timeMinute)

        val data = workDataOf("schedule_id" to schedule.id)

        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .build()

        val periodicWorkRequest = PeriodicWorkRequestBuilder<WallpaperChangeWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .setConstraints(constraints)
            .setInputData(data)
            .addTag(workTag)
            .build()

        workManager.enqueueUniquePeriodicWork(
            workTag,
            ExistingPeriodicWorkPolicy.UPDATE,
            periodicWorkRequest
        )
    }

    fun cancelSchedule(scheduleId: Long) {
        workManager.cancelAllWorkByTag(getWorkTag(scheduleId))
    }

    fun triggerNow(scheduleId: Long) {
        val data = workDataOf("schedule_id" to scheduleId)
        val oneTimeWork = OneTimeWorkRequestBuilder<WallpaperChangeWorker>()
            .setInputData(data)
            .build()
        workManager.enqueue(oneTimeWork)
    }

    private fun getWorkTag(scheduleId: Long): String = "wallpaper_schedule_$scheduleId"

    private fun calculateInitialDelayMs(hour: Int, minute: Int): Long {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (target.before(now)) {
            target.add(Calendar.DAY_OF_YEAR, 1)
        }
        return target.timeInMillis - now.timeInMillis
    }
}
