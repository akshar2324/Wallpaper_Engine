package com.akshar.wallpaperengine.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.akshar.wallpaperengine.MainActivity
import com.akshar.wallpaperengine.R
import com.akshar.wallpaperengine.WallpaperEngineApplication
import com.akshar.wallpaperengine.receiver.WallpaperActionReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WallpaperAppWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val app = context.applicationContext as? WallpaperEngineApplication
        CoroutineScope(Dispatchers.IO).launch {
            val history = app?.database?.historyDao()?.getRecentHistoryList(1)?.firstOrNull()
            val wallpaper = if (history != null) {
                app.database.wallpaperDao().getWallpaperById(history.wallpaperId)
            } else null

            val title = wallpaper?.title ?: history?.wallpaperTitle ?: "Wallpaper Engine"
            val style = wallpaper?.style ?: "Tap Next to rotate"
            val mood = wallpaper?.mood ?: ""
            val isFav = wallpaper?.isFavorite == true
            val wallpaperId = wallpaper?.id ?: history?.wallpaperId ?: -1L

            for (appWidgetId in appWidgetIds) {
                updateAppWidget(context, appWidgetManager, appWidgetId, title, style, mood, isFav, wallpaperId)
            }
        }
    }

    companion object {
        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int,
            title: String,
            style: String,
            mood: String,
            isFavorite: Boolean,
            wallpaperId: Long
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_wallpaper_engine)
            views.setTextViewText(R.id.widget_wallpaper_title, title)
            val subtext = if (mood.isNotBlank()) "$style • $mood" else style
            views.setTextViewText(R.id.widget_wallpaper_style, subtext)
            views.setTextViewText(R.id.widget_btn_favorite, if (isFavorite) "★ SAVED" else "☆ STAR")

            // Next button click
            val nextIntent = Intent(context, WallpaperActionReceiver::class.java).apply {
                action = WallpaperActionReceiver.ACTION_NEXT_WALLPAPER
            }
            val nextPendingIntent = PendingIntent.getBroadcast(
                context,
                101,
                nextIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_btn_next, nextPendingIntent)

            // Favorite button click
            val favIntent = Intent(context, WallpaperActionReceiver::class.java).apply {
                action = WallpaperActionReceiver.ACTION_TOGGLE_FAVORITE
                putExtra(WallpaperActionReceiver.EXTRA_WALLPAPER_ID, wallpaperId)
            }
            val favPendingIntent = PendingIntent.getBroadcast(
                context,
                102,
                favIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_btn_favorite, favPendingIntent)

            // Open app on body click
            val openAppIntent = Intent(context, MainActivity::class.java)
            val openAppPendingIntent = PendingIntent.getActivity(
                context,
                100,
                openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_wallpaper_title, openAppPendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val thisWidget = ComponentName(context, WallpaperAppWidgetProvider::class.java)
            val allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)
            if (allWidgetIds.isNotEmpty()) {
                val intent = Intent(context, WallpaperAppWidgetProvider::class.java).apply {
                    action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, allWidgetIds)
                }
                context.sendBroadcast(intent)
            }
        }
    }
}
