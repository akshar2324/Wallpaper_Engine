package com.akshar.wallpaperengine.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import com.akshar.wallpaperengine.WallpaperEngineApplication
import com.akshar.wallpaperengine.domain.automation.ContextTrigger
import com.akshar.wallpaperengine.domain.automation.ContextTriggerManager
import com.akshar.wallpaperengine.domain.usecase.RotateWallpaperUseCase

class ContextTriggerReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val app = context.applicationContext as? WallpaperEngineApplication ?: return
        val rotateUseCase = RotateWallpaperUseCase(
            wallpaperDao = app.database.wallpaperDao(),
            scheduleDao = app.database.scheduleDao(),
            historyDao = app.database.historyDao(),
            wallpaperService = app.wallpaperService
        )
        val manager = ContextTriggerManager(context, rotateUseCase)

        when (intent.action) {
            Intent.ACTION_POWER_CONNECTED -> {
                manager.handleTrigger(ContextTrigger.POWER_CONNECTED)
            }
            Intent.ACTION_POWER_DISCONNECTED -> {
                manager.handleTrigger(ContextTrigger.POWER_DISCONNECTED)
            }
            PowerManager.ACTION_POWER_SAVE_MODE_CHANGED -> {
                val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
                if (powerManager?.isPowerSaveMode == true) {
                    manager.handleTrigger(ContextTrigger.BATTERY_SAVER_ON)
                } else {
                    manager.handleTrigger(ContextTrigger.BATTERY_SAVER_OFF)
                }
            }
        }
    }
}
