package com.akshar.wallpaperengine.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wallpaper_history")
data class WallpaperHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val wallpaperId: Long,
    val wallpaperTitle: String,
    val wallpaperUri: String,
    val appliedAt: Long = System.currentTimeMillis(),
    val targetScreen: String = "HOME_AND_LOCK",
    val source: String = "MANUAL", // MANUAL, SCHEDULE
    val scheduleId: Long? = null,
    val selectionReason: String? = null
)
