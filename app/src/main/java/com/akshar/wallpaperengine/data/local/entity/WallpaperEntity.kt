package com.akshar.wallpaperengine.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "wallpapers",
    indices = [
        androidx.room.Index(value = ["isFavorite"]),
        androidx.room.Index(value = ["lastUsed"]),
        androidx.room.Index(value = ["dateAdded"])
    ]
)
data class WallpaperEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val uri: String,
    val title: String,
    val width: Int = 1080,
    val height: Int = 1920,
    val aspectRatio: Float = 9f / 16f,
    val fileSize: Long = 0L,
    val mimeType: String = "image/jpeg",
    val dateAdded: Long = System.currentTimeMillis(),
    val lastUsed: Long? = null,
    val isFavorite: Boolean = false,
    val contentHash: String = "",
    val scaleType: String = "FILL", // FILL, FIT, CROP, CENTER
    val horizontalOffset: Float = 0.5f,
    val verticalOffset: Float = 0.5f,
    val isSample: Boolean = false
)
