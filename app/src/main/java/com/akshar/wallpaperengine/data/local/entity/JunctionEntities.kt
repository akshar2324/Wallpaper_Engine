package com.akshar.wallpaperengine.data.local.entity

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "wallpaper_collection_cross_ref",
    primaryKeys = ["wallpaperId", "collectionId"],
    indices = [Index("collectionId"), Index("wallpaperId")]
)
data class WallpaperCollectionCrossRef(
    val wallpaperId: Long,
    val collectionId: Long
)

@Entity(
    tableName = "wallpaper_tag_cross_ref",
    primaryKeys = ["wallpaperId", "tagId"],
    indices = [Index("tagId"), Index("wallpaperId")]
)
data class WallpaperTagCrossRef(
    val wallpaperId: Long,
    val tagId: Long
)
