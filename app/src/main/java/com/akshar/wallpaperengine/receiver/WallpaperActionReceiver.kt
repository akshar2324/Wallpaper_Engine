package com.akshar.wallpaperengine.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.akshar.wallpaperengine.WallpaperEngineApplication
import com.akshar.wallpaperengine.domain.usecase.RotateWallpaperUseCase
import com.akshar.wallpaperengine.notification.WallpaperNotificationManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WallpaperActionReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_NEXT_WALLPAPER = "com.akshar.wallpaperengine.ACTION_NEXT_WALLPAPER"
        const val ACTION_TOGGLE_FAVORITE = "com.akshar.wallpaperengine.ACTION_TOGGLE_FAVORITE"
        const val EXTRA_WALLPAPER_ID = "extra_wallpaper_id"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val app = context.applicationContext as? WallpaperEngineApplication ?: return
        val wallpaperDao = app.database.wallpaperDao()

        when (intent.action) {
            ACTION_NEXT_WALLPAPER -> {
                val rotateUseCase = RotateWallpaperUseCase(
                    wallpaperDao = wallpaperDao,
                    scheduleDao = app.database.scheduleDao(),
                    historyDao = app.database.historyDao(),
                    wallpaperService = app.wallpaperService
                )
                CoroutineScope(Dispatchers.IO).launch {
                    rotateUseCase.rotateWithContextTrigger(
                        triggerType = "NOTIFICATION",
                        reasonOverride = "Quick Action (Skip)"
                    )
                    com.akshar.wallpaperengine.widget.WallpaperAppWidgetProvider.updateAllWidgets(context)
                }
            }
            ACTION_TOGGLE_FAVORITE -> {
                val wallpaperId = intent.getLongExtra(EXTRA_WALLPAPER_ID, -1L)
                if (wallpaperId != -1L) {
                    CoroutineScope(Dispatchers.IO).launch {
                        val wallpaper = wallpaperDao.getWallpaperById(wallpaperId)
                        if (wallpaper != null) {
                            val newFavorite = !wallpaper.isFavorite
                            wallpaperDao.updateFavorite(wallpaperId, newFavorite)
                            val updated = wallpaper.copy(isFavorite = newFavorite)
                            WallpaperNotificationManager.postRotationNotification(
                                context,
                                updated,
                                if (newFavorite) "Starred as Favorite ★" else "Removed from Favorites"
                            )
                            com.akshar.wallpaperengine.widget.WallpaperAppWidgetProvider.updateAllWidgets(context)
                        }
                    }
                }
            }
        }
    }
}
