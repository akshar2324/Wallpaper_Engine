package com.akshar.wallpaperengine.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "wallpaper_positions",
    foreignKeys = [
        ForeignKey(
            entity = WallpaperEntity::class,
            parentColumns = ["id"],
            childColumns = ["wallpaperId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("wallpaperId"),
        Index("wallpaperId", "targetScreen", unique = true)
    ]
)
data class WallpaperPositionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val wallpaperId: Long,
    val targetScreen: String, // "HOME", "LOCK", "HOME_AND_LOCK"
    val scale: Float = 1.0f,
    val offsetX: Float = 0f,
    val offsetY: Float = 0f,
    val fitMode: String = "FILL" // "FIT", "FILL", "CENTER", "CROP"
)
