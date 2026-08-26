package com.akshar.wallpaperengine.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "wallpapers",
    indices = [
        androidx.room.Index(value = ["isFavorite"]),
        androidx.room.Index(value = ["lastUsed"]),
        androidx.room.Index(value = ["dateAdded"]),
        androidx.room.Index(value = ["rating"]),
        androidx.room.Index(value = ["dominantColor"]),
        androidx.room.Index(value = ["brightness"]),
        androidx.room.Index(value = ["isDark"]),
        androidx.room.Index(value = ["contentHash"])
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
    val isSample: Boolean = false,
    val rating: Float = 0f,
    val dominantColor: Int? = null,
    val secondaryColor: Int? = null,
    val brightness: Float = 0.5f,
    val isDark: Boolean = false,
    val skipCount: Int = 0,
    val likeCount: Int = 0,
    val viewCount: Int = 0,
    val lastSkipped: Long? = null,
    val isPrivate: Boolean = false,
    val style: String? = null,
    val mood: String? = null
)
