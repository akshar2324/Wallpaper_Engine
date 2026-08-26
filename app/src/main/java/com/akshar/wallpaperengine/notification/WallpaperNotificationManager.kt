package com.akshar.wallpaperengine.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.akshar.wallpaperengine.MainActivity
import com.akshar.wallpaperengine.R
import com.akshar.wallpaperengine.data.local.entity.WallpaperEntity
import com.akshar.wallpaperengine.receiver.WallpaperActionReceiver

object WallpaperNotificationManager {

    const val CHANNEL_ID = "wallpaper_rotation_updates_channel"
    const val NOTIFICATION_ID = 1001

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Wallpaper Rotations"
            val descriptionText = "Notifications for automatic wallpaper rotations with quick controls"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                setShowBadge(false)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            notificationManager?.createNotificationChannel(channel)
        }
    }

    fun postRotationNotification(
        context: Context,
        wallpaper: WallpaperEntity,
        reason: String
    ) {
        createNotificationChannel(context)

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            context,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action: Next Wallpaper
        val nextIntent = Intent(context, WallpaperActionReceiver::class.java).apply {
            action = WallpaperActionReceiver.ACTION_NEXT_WALLPAPER
        }
        val nextPendingIntent = PendingIntent.getBroadcast(
            context,
            1,
            nextIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action: Favorite Wallpaper
        val favIntent = Intent(context, WallpaperActionReceiver::class.java).apply {
            action = WallpaperActionReceiver.ACTION_TOGGLE_FAVORITE
            putExtra(WallpaperActionReceiver.EXTRA_WALLPAPER_ID, wallpaper.id)
        }
        val favPendingIntent = PendingIntent.getBroadcast(
            context,
            2,
            favIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val starLabel = if (wallpaper.isFavorite) "★ Favorited" else "☆ Favorite"

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Wallpaper Applied: ${wallpaper.title}")
            .setContentText(reason)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(openAppPendingIntent)
            .setAutoCancel(true)
            .addAction(0, "SKIP NEXT", nextPendingIntent)
            .addAction(0, starLabel, favPendingIntent)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        notificationManager?.notify(NOTIFICATION_ID, builder.build())
    }
}
