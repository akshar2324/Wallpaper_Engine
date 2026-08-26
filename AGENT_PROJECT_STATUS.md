# AGENT_PROJECT_STATUS.md

## 1. Project Overview & Architecture

**Wallpaper Engine** is a local-first Android wallpaper library, rotation engine, and visual customization application built with Jetpack Compose, Room, WorkManager, Coil, and AGSL Shaders.

### Architectural Pattern: MVVM + UDF + Domain UseCases
- **Presentation Layer (UI)**: Jetpack Compose composables observing `StateFlow` from ViewModels.
- **ViewModel Layer**: Manages UI state, handles user interactions, and coordinates with UseCases and Repositories.
- **Domain Layer**:
  - `RotateWallpaperUseCase`: Orchestrates schedule checks, strategy selection, wallpaper application, and history recording.
  - `WallpaperSelectionStrategy`: Concrete implementations (`RandomSelectionStrategy`, `SequentialSelectionStrategy`, `LeastRecentlyUsedSelectionStrategy`) utilizing optimized SQLite queries to select items directly in the database without loading large datasets into RAM.
- **Data Layer**:
  - Repositories: `WallpaperRepository`, `CollectionRepository`, `ScheduleRepository`, `HistoryRepository`, `TagRepository`, `UserPreferencesRepository`.
  - Local Storage: Room Database (`WallpaperDatabase`), SupportSQLite raw queries for multi-parameter dynamic filtering, and AndroidX DataStore for user preferences.
- **System / Background Execution**:
  - `AndroidWallpaperService`: Handles streaming image assets / content URIs to Android's `WallpaperManager`.
  - `WallpaperScheduler`: Configures unique periodic work requests via `WorkManager`.
  - `WallpaperChangeWorker`: `CoroutineWorker` triggered in the background to execute wallpaper rotations.

---

## 2. Important Files

| Category | File Path | Purpose |
|---|---|---|
| **Application & Entry** | `WallpaperEngineApplication.kt` | Application class with manual DI container and initial data seeding |
| **Activity & Nav** | `MainActivity.kt`, `navigation/Screen.kt` | Main activity, Compose navigation host, bottom bar, shader host |
| **Database** | `data/local/WallpaperDatabase.kt` | Room database (Version 6) |
| **Entities** | `data/local/entity/*` | `WallpaperEntity`, `CollectionEntity`, `TagEntity`, `ScheduleEntity`, `WallpaperHistoryEntity`, CrossRefs |
| **DAOs** | `data/local/dao/*` | `WallpaperDao`, `CollectionDao`, `ScheduleDao`, `HistoryDao`, `TagDao` |
| **Repositories** | `data/repository/*`, `data/preferences/*` | Data access abstraction and DataStore preferences repository |
| **Domain** | `domain/strategy/*`, `domain/usecase/*`, `domain/solar/*`, `domain/automation/*` | Selection strategies (Smart Shuffle, Solar, LRU, etc.), `SolarCalculator`, `ContextTriggerManager`, `RotateWallpaperUseCase` |
| **Maintenance & Analytics** | `data/maintenance/*`, `data/analytics/*`, `data/backup/*` | `LibraryHealthManager`, `AnalyticsManager`, `BackupRestoreEngine` |
| **Editor & Remote Discovery** | `data/editor/*`, `data/remote/*` | `WallpaperEditorProcessor`, `RemoteWallpaperManager`, `RemoteWallpaperCatalogue` |
| **Widgets & Notifications** | `widget/*`, `notification/*`, `service/*` | `WallpaperAppWidgetProvider`, `WallpaperNotificationManager`, `WallpaperTileService` |
| **Background & Services** | `scheduler/*`, `workers/*`, `receiver/*`, `wallpaper/*` | `WallpaperScheduler`, `WallpaperTaggingWorker`, `LiveWallpaperEngineService`, `ContextTriggerReceiver`, `WallpaperActionReceiver` |
| **Shaders & Theme** | `theme/*`, `shader/*` | OLED-first color palettes, typography, AGSL `RuntimeShader` + static fallbacks |
| **UI Screens** | `ui/screens/*` | `HomeScreen`, `LibraryScreen`, `CollectionsScreen`, `SchedulesScreen`, `HistoryScreen`, `SettingsScreen`, `WallpaperDetailScreen`, `OnboardingScreen` |

---

## 3. Data Flow

```
Compose UI (HomeScreen / LibraryScreen / DetailScreen)
      │ ▲
      ▼ │ StateFlow<UiState>
  ViewModel (e.g. HomeViewModel, LibraryViewModel)
      │
      ▼
UseCases / Repositories (e.g. WallpaperRepository, RotateWallpaperUseCase)
      │
      ▼
Room DAOs (RawQuery / Flow)  &  AndroidX DataStore
      │
      ▼
SQLite Database (wallpaper_engine_db) / Disk Storage
```

---

## 4. Wallpaper Rotation Flow

```
WallpaperScheduler (WorkManager Unique Periodic Work)
      │
      ▼
WallpaperChangeWorker (CoroutineWorker)
      │
      ▼
RotateWallpaperUseCase.invoke(scheduleId)
      │
      ├─► Validate schedule.isEnabled
      ├─► SelectionStrategyFactory.getStrategy(schedule.selectionMode)
      │        └─► Random / Sequential / LRU (Direct SQL LIMIT 1 query)
      ├─► WallpaperDao.getWallpaperById(selectedId)
      ├─► AndroidWallpaperService.applyWallpaper(wallpaper, targetScreen)
      │        └─► WallpaperManager.setStream(...) (FLAG_SYSTEM / FLAG_LOCK)
      ├─► WallpaperDao.updateLastUsed(id, timestamp)
      ├─► HistoryDao.insertHistoryRecord(...)
      └─► ScheduleDao.updateScheduleExecutionTime(id, last, next)
```

---

## 5. Database Schema & Structure

- **`wallpapers`**: `id` (PK Auto), `uri`, `title`, `width`, `height`, `aspectRatio`, `fileSize`, `mimeType`, `dateAdded`, `lastUsed`, `isFavorite`, `contentHash`, `scaleType`, `horizontalOffset`, `verticalOffset`, `isSample`, `rating`, `dominantColor`, `secondaryColor`, `brightness`, `isDark`, `skipCount`, `likeCount`, `viewCount`, `lastSkipped`, `isPrivate`, `style`, `mood`. Indices on `isFavorite`, `lastUsed`, `dateAdded`, `rating`, `dominantColor`, `brightness`, `isDark`, `contentHash`.
- **`collections`**: `id` (PK Auto), `name`, `description`, `coverUri`, `wallpaperCount`, `createdAt`.
- **`tags`**: `id` (PK Auto), `name`.
- **`wallpaper_collection_cross_ref`**: `wallpaperId`, `collectionId` (Composite PK).
- **`wallpaper_tag_cross_ref`**: `wallpaperId`, `tagId` (Composite PK).
- **`schedules`**: `id` (PK Auto), `name`, `timeHour`, `timeMinute`, `activeDaysCsv`, `isEnabled`, `sourceType`, `sourceCollectionId`, `specificWallpaperId`, `selectionMode`, `targetScreen`, `lastExecution`, `nextExecution`, `triggerType`, `priority`.
- **`wallpaper_history`**: `id` (PK Auto), `wallpaperId`, `wallpaperTitle`, `wallpaperUri`, `appliedAt`, `targetScreen`, `source`, `scheduleId`, `selectionReason`.

Database Migrations: `MIGRATION_2_3` (v2→v3 DNA fields), `MIGRATION_3_4` (v3→v4 history reason), `MIGRATION_4_5` (v4→v5 contentHash index), `MIGRATION_5_6` (v5→v6 schedule triggerType & priority).

---

## 6. Background Execution & WorkManager

- **Triggering Mechanism**: 
  - `WallpaperChangeWorker`: WorkManager `PeriodicWorkRequest` configured with 24-hour interval and initial delay to match schedule hour/minute.
  - `WallpaperTaggingWorker`: Periodic 12-hour WorkManager background worker for on-device AI tagging, perceptual hashing, and duplicate detection.
  - `ContextTriggerReceiver`: BroadcastReceiver reacting to power connect/disconnect, battery saver mode, and DND transitions.
- **Constraints**: `setRequiresBatteryNotLow(true)`.
- **Unique Name**: Tagged per schedule: `wallpaper_schedule_$scheduleId` with `ExistingPeriodicWorkPolicy.UPDATE`.
- **Dead URI Resilience**: Includes a 5-attempt retry loop in `RotateWallpaperUseCase` to skip deleted or unreadable files gracefully.

---

## 7. UI & Theme Structure

- **Navigation**:
  - Main tabs: `Home`, `Library`, `Collections`, `Schedule`, `Settings`.
  - Secondary screens: `History`, `Detail` (`detail/{wallpaperId}`), `Onboarding`.
- **Themes**:
  - 6 distinct visual themes: `Abyss` (default OLED purple), `Neon Violet`, `Midnight`, `Crimson Night`, `Moonlight`, `Light Day`.
- **Shaders**:
  - Android 13+ (API 33+): AGSL GPU `RuntimeShader` with low intensity background effects (`Nebula`, `Aurora`, `Cyber Grid`, `Energy Flow`, `Void`, `Particles`).
  - Below API 33 / Reduced Motion: Static radial gradient fallback to conserve battery and CPU.

---

## 8. Unit & Integration Tests

- `WallpaperEngineTest.kt`: Tests shader style resolution, shader intensity resolution, default filter options, and entity instantiation.
- `WallpaperDaoTest.kt`: Robolectric test validating filtered SQLite query construction, content hash lookups, and multi-tag search.
- `RotateWallpaperUseCaseTest.kt`: Mockito-based tests verifying disabled schedule handling, rotation workflow, and context trigger execution.
- `SelectionStrategyTest.kt`: Mockito-based tests for all 8 selection strategies (Random, Sequential, LRU, Smart Shuffle, Weighted Favorites, Never Repeat, Variety, Time of Day).
- `WallpaperDnaAnalyzerTest.kt`: Unit tests for dominant color, brightness luminance, and dark mode detection.
- `WallpaperTagClassifierTest.kt`: Unit tests for style/mood inference and keyword tag generation.
- `PerceptualHashAnalyzerTest.kt`: Unit tests for 64-bit dHash, DCT pHash, Hamming distance, and scale invariance.
- `SolarCalculatorTest.kt`: Unit tests for NOAA solar equations, sunrise/sunset, golden hour, and time-of-day profile mapping.
- `LibraryHealthManagerTest.kt`: Unit tests for broken file detection, unused wallpaper scanning, and duplicate resolution.
- `AnalyticsManagerTest.kt`: Unit tests for rotation statistics, dark OLED ratio, and style aggregation.
- `BackupRestoreEngineTest.kt`: Unit tests for full JSON backup export and restore serialization.
- `WallpaperEditorProcessorTest.kt`: Unit tests for color matrix filtering, vignette, crop, and OLED black floor crushed adjustments.
- `SettingsViewModelTest.kt`: Robolectric tests for theme selection, performance profiles, and health wizard scanning.
- `WallpaperAppWidgetProviderTest.kt`: Robolectric tests for home screen widget layout and button action wiring.
- `RemoteWallpaperManagerTest.kt`: Unit tests for curated cloud catalogue categories and online feed item validation.

---

## 9. Build Status & Environment

- **Gradle Version**: 8.7
- **Android Gradle Plugin**: 8.4.2
- **Kotlin Version**: 2.2.10
- **Compile SDK**: 35 | **Target SDK**: 35 | **Min SDK**: 24
- **JDK Requirement**: Java 17+ (Configured with OpenJDK 17.0.20.1).
- **Build Verification**:
  - `./gradlew assembleDebug`: **PASSED (0 errors)**
  - `./gradlew test`: **PASSED (56/56 unit tests passing)**

---

## 10. Master Roadmap Completion (All 11 Phases 100% Complete)

1. **Phase 1: Architecture Audit & Core Stability**: Clean architecture audit, Compose syntax fixes, dependency alignment, Robolectric setup.
2. **Phase 2: Wallpaper DNA & Schema Evolution v3**: Room DB v3 migration (`MIGRATION_2_3`), low-RAM `WallpaperDnaAnalyzer` extracting luminance, color histograms, and dark mode boolean flags.
3. **Phase 3: Intelligent Selection & Scoring Engine**: Room DB v4 migration (`MIGRATION_3_4`), dynamic SQLite scoring query, 7 selection strategies with selection explanations.
4. **Phase 4: Advanced Search, Filtering & Smart Collections**: Compound SQLite query engine, Library quick filter chips, ModalBottomSheet filter drawer, Wallpaper Detail DNA card, History reason badges.
5. **Phase 5: AI & On-Device Metadata Tagging**: Room DB v5 migration (`MIGRATION_4_5`), on-device style/mood classifier (`WallpaperTagClassifier`), 64-bit DCT perceptual hash duplicate engine (`PerceptualHashAnalyzer`), background idle tagging worker (`WallpaperTaggingWorker`).
6. **Phase 6: Context-Aware Automation & Scheduling**: Room DB v6 migration (`MIGRATION_5_6`), NOAA solar engine (`SolarCalculator`), 6 time-of-day profiles (`TimeOfDayProfile`), `TimeOfDaySelectionStrategy`, hardware broadcast triggers (`ContextTriggerReceiver` & `ContextTriggerManager`).
7. **Phase 7: Library Maintenance, Analytics & Backup/Restore**: Broken URI scanner & cleaner, low-res filter, unused wallpaper cleaner, duplicate cluster resolver (`LibraryHealthManager`), usage stats dashboard (`AnalyticsManager`), full JSON backup & restore engine (`BackupRestoreEngine`).
8. **Phase 8: Live Shaders, Wallpaper Editor & Android Ecosystem**: Live Wallpaper engine (`LiveWallpaperEngineService`) with AGSL RuntimeShader & touch wave ripples, on-device image editor (`WallpaperEditorProcessor`), Quick Settings rotation tile (`WallpaperTileService`), and interactive notification controls (`WallpaperNotificationManager` & `WallpaperActionReceiver`).
9. **Phase 9: Production Polish, Release Engineering & Performance Optimization**: R8/ProGuard rules (`proguard-rules.pro`), ViewModel testing (`SettingsViewModelTest`), and minification readiness.
10. **Phase 10: Jetpack Glance Interactive Home Screen Widgets**: Home screen widget provider (`WallpaperAppWidgetProvider`), XML layout (`widget_wallpaper_engine.xml`), and real-time broadcast sync on rotation.
11. **Phase 11: Remote Wallpaper Sources & Online Discovery**: Curated online feed catalogue (`RemoteWallpaperCatalogue`), category drops (AMOLED, Cyberpunk, Nature, Anime), and streaming network downloader with instant DNA profiling (`RemoteWallpaperManager`).

