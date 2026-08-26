package com.akshar.wallpaperengine.domain.automation

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.PowerManager
import com.akshar.wallpaperengine.domain.usecase.RotateWallpaperUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

enum class ContextTrigger(val key: String, val displayName: String, val priority: Int) {
    BATTERY_SAVER_ON("BATTERY_SAVER_ON", "Battery Saver Enabled (OLED Switch)", 100),
    BATTERY_SAVER_OFF("BATTERY_SAVER_OFF", "Battery Saver Disabled", 50),
    POWER_CONNECTED("POWER_CONNECTED", "Charging Connected (Vibrant Profile)", 60),
    POWER_DISCONNECTED("POWER_DISCONNECTED", "Charging Disconnected", 40),
    DND_ON("DND_ON", "Do Not Disturb (Calm Minimal)", 70),
    TIME_TRANSITION("TIME_TRANSITION", "Time-of-Day Adaptive Shift", 30)
}

class ContextTriggerManager(
    private val context: Context,
    private val rotateWallpaperUseCase: RotateWallpaperUseCase
) {

    fun handleTrigger(trigger: ContextTrigger) {
        CoroutineScope(Dispatchers.IO).launch {
            when (trigger) {
                ContextTrigger.BATTERY_SAVER_ON -> {
                    rotateWallpaperUseCase.rotateWithContextTrigger(
                        triggerType = "BATTERY_SAVER",
                        reasonOverride = "Battery Saver (OLED Dark Mode)"
                    )
                }
                ContextTrigger.POWER_CONNECTED -> {
                    rotateWallpaperUseCase.rotateWithContextTrigger(
                        triggerType = "CHARGING",
                        reasonOverride = "Charging Connected (High Dynamic Profile)"
                    )
                }
                ContextTrigger.DND_ON -> {
                    rotateWallpaperUseCase.rotateWithContextTrigger(
                        triggerType = "DND",
                        reasonOverride = "Do Not Disturb (Calm Minimal)"
                    )
                }
                ContextTrigger.TIME_TRANSITION, ContextTrigger.BATTERY_SAVER_OFF, ContextTrigger.POWER_DISCONNECTED -> {
                    rotateWallpaperUseCase.rotateWithContextTrigger(
                        triggerType = "TIME_OF_DAY",
                        reasonOverride = "Context Adaptive Transition"
                    )
                }
            }
        }
    }

    fun isBatterySaverActive(): Boolean {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        return powerManager?.isPowerSaveMode == true
    }

    fun isDeviceCharging(): Boolean {
        val batteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { filter ->
            context.registerReceiver(null, filter)
        }
        val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        return status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
    }
}
