package com.akshar.wallpaperengine.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.akshar.wallpaperengine.data.local.dao.*
import com.akshar.wallpaperengine.data.local.entity.*

@Database(
    entities = [
        WallpaperEntity::class,
        CollectionEntity::class,
        TagEntity::class,
        WallpaperCollectionCrossRef::class,
        WallpaperTagCrossRef::class,
        ScheduleEntity::class,
        WallpaperHistoryEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class WallpaperDatabase : RoomDatabase() {

    abstract fun wallpaperDao(): WallpaperDao
    abstract fun collectionDao(): CollectionDao
    abstract fun tagDao(): TagDao
    abstract fun scheduleDao(): ScheduleDao
    abstract fun historyDao(): HistoryDao

    companion object {
        @Volatile
        private var INSTANCE: WallpaperDatabase? = null

        fun getInstance(context: Context): WallpaperDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WallpaperDatabase::class.java,
                    "wallpaper_engine_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
