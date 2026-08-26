package com.akshar.wallpaperengine.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "schedules")
data class ScheduleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val timeHour: Int = 8,
    val timeMinute: Int = 0,
    val activeDaysCsv: String = "MON,TUE,WED,THU,FRI,SAT,SUN", // CSV string
    val isEnabled: Boolean = true,
    val sourceType: String = "FAVORITES", // SPECIFIC, COLLECTION, FAVORITES, ALL, TIME_OF_DAY
    val sourceCollectionId: Long? = null,
    val specificWallpaperId: Long? = null,
    val selectionMode: String = "RANDOM", // RANDOM, SEQUENTIAL, LRU, SMART_SHUFFLE, TIME_OF_DAY, WEIGHTED_FAVORITES, NEVER_REPEAT, VARIETY
    val targetScreen: String = "HOME_AND_LOCK", // HOME, LOCK, HOME_AND_LOCK
    val lastExecution: Long? = null,
    val nextExecution: Long? = null,
    val triggerType: String = "TIME", // TIME, BATTERY_SAVER, CHARGING, DND, SUNRISE_SUNSET, TIME_OF_DAY
    val priority: Int = 0
)
