package com.akshar.wallpaperengine

import android.app.Application
import com.akshar.wallpaperengine.data.local.WallpaperDatabase
import com.akshar.wallpaperengine.data.preferences.UserPreferencesRepository
import com.akshar.wallpaperengine.data.repository.*
import com.akshar.wallpaperengine.scheduler.WallpaperScheduler
import com.akshar.wallpaperengine.wallpaper.AndroidWallpaperService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WallpaperEngineApplication : Application() {

    lateinit var database: WallpaperDatabase
        private set

    lateinit var userPreferencesRepository: UserPreferencesRepository
        private set

    lateinit var wallpaperRepository: WallpaperRepository
        private set

    lateinit var collectionRepository: CollectionRepository
        private set

    lateinit var tagRepository: TagRepository
        private set

    lateinit var scheduleRepository: ScheduleRepository
        private set

    lateinit var historyRepository: HistoryRepository
        private set

    lateinit var wallpaperService: AndroidWallpaperService
        private set

    lateinit var wallpaperScheduler: WallpaperScheduler
        private set

    lateinit var libraryHealthManager: com.akshar.wallpaperengine.data.maintenance.LibraryHealthManager
        private set

    lateinit var analyticsManager: com.akshar.wallpaperengine.data.analytics.AnalyticsManager
        private set

    lateinit var backupRestoreEngine: com.akshar.wallpaperengine.data.backup.BackupRestoreEngine
        private set

    override fun onCreate() {
        super.onCreate()

        database = WallpaperDatabase.getInstance(this)
        userPreferencesRepository = UserPreferencesRepository(this)

        wallpaperRepository = WallpaperRepository(
            wallpaperDao = database.wallpaperDao(),
            collectionDao = database.collectionDao(),
            tagDao = database.tagDao(),
            context = this
        )

        collectionRepository = CollectionRepository(database.collectionDao())
        tagRepository = TagRepository(database.tagDao(), database.wallpaperDao())
        scheduleRepository = ScheduleRepository(database.scheduleDao())
        historyRepository = HistoryRepository(database.historyDao())

        wallpaperService = AndroidWallpaperService(this)
        libraryHealthManager = com.akshar.wallpaperengine.data.maintenance.LibraryHealthManager(this, database.wallpaperDao(), wallpaperRepository)
        analyticsManager = com.akshar.wallpaperengine.data.analytics.AnalyticsManager(database.wallpaperDao(), database.historyDao())
        backupRestoreEngine = com.akshar.wallpaperengine.data.backup.BackupRestoreEngine(
            database.wallpaperDao(),
            database.collectionDao(),
            database.tagDao(),
            database.scheduleDao(),
            database.historyDao()
        )

        try {
            wallpaperScheduler = WallpaperScheduler(this)
            wallpaperScheduler.schedulePeriodicTagging()
        } catch (e: Exception) {
            // WorkManager may not be initialized in test environments
        }

        CoroutineScope(Dispatchers.IO).launch {
            wallpaperRepository.seedInitialDataIfEmpty()
        }
    }
}
