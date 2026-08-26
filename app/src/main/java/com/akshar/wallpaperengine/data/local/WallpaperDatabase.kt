package com.akshar.wallpaperengine.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
    version = 6,
    exportSchema = false
)
abstract class WallpaperDatabase : RoomDatabase() {

    abstract fun wallpaperDao(): WallpaperDao
    abstract fun collectionDao(): CollectionDao
    abstract fun tagDao(): TagDao
    abstract fun scheduleDao(): ScheduleDao
    abstract fun historyDao(): HistoryDao

    companion object {
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE wallpapers ADD COLUMN rating REAL NOT NULL DEFAULT 0.0")
                database.execSQL("ALTER TABLE wallpapers ADD COLUMN dominantColor INTEGER")
                database.execSQL("ALTER TABLE wallpapers ADD COLUMN secondaryColor INTEGER")
                database.execSQL("ALTER TABLE wallpapers ADD COLUMN brightness REAL NOT NULL DEFAULT 0.5")
                database.execSQL("ALTER TABLE wallpapers ADD COLUMN isDark INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE wallpapers ADD COLUMN skipCount INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE wallpapers ADD COLUMN likeCount INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE wallpapers ADD COLUMN viewCount INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE wallpapers ADD COLUMN lastSkipped INTEGER")
                database.execSQL("ALTER TABLE wallpapers ADD COLUMN isPrivate INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE wallpapers ADD COLUMN style TEXT")
                database.execSQL("ALTER TABLE wallpapers ADD COLUMN mood TEXT")

                database.execSQL("CREATE INDEX IF NOT EXISTS index_wallpapers_rating ON wallpapers(rating)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_wallpapers_dominantColor ON wallpapers(dominantColor)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_wallpapers_brightness ON wallpapers(brightness)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_wallpapers_isDark ON wallpapers(isDark)")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE wallpaper_history ADD COLUMN selectionReason TEXT")
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE INDEX IF NOT EXISTS index_wallpapers_contentHash ON wallpapers(contentHash)")
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE schedules ADD COLUMN triggerType TEXT NOT NULL DEFAULT 'TIME'")
                database.execSQL("ALTER TABLE schedules ADD COLUMN priority INTEGER NOT NULL DEFAULT 0")
            }
        }

        @Volatile
        private var INSTANCE: WallpaperDatabase? = null

        fun getInstance(context: Context): WallpaperDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WallpaperDatabase::class.java,
                    "wallpaper_engine_db"
                )
                .addMigrations(MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6)
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
