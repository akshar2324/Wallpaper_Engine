# 🚀 Wallpaper Engine — Master Roadmap & Subtask Execution Board

> **Project Mission**: Transform Wallpaper Engine into a local-first, intelligent Android wallpaper platform that learns user taste (*Import → Understand → Organize → Learn → Select → Adapt*).
> 
> **Resume Protocol**: In case of context or token reset, inspect this file first to resume precisely from the last unfinished subtask.

---

## ⚡ Token-Optimized Multi-Agent Strategy

| Model Tier | Cost / Token Efficiency | Best Suited Tasks |
|---|---|---|
| **`flash_lite`** | ⭐⭐⭐⭐⭐ *Ultra Low* | Boilerplate data classes, enums, string resources, test data mocks, basic helper extensions. |
| **`flash`** | ⭐⭐⭐⭐ *High* | Compose UI screens & components, simple unit tests, DAO queries, filter chips, view models. |
| **`pro` / `inherit`** | ⭐⭐⭐ *Deep Reasoning* | Room database migrations, complex SQLite scoring algorithms, concurrency/WorkManager, AGSL shaders, multi-agent architecture integration. |

---

## 📊 Master Progress Tracker

- **Current Phase**: **All 11 Roadmap Phases Completed (100%) ✦**
- **Current Active Subtask**: All Roadmap Phases Complete & Verified
- **Last Updated**: 2026-08-26
- **Build Status**: 🟢 Passing (`./gradlew test` + `./gradlew assembleDebug` • 56/56 tests passing)

---

## 🗺️ Phases & Detailed Subtasks

### 🟢 Phase 1: Architecture Audit & Core Stability *(COMPLETED)*
- [x] **1.1** Complete repository inspect & audit (`GEMINI.md`, `AGENT_PROJECT_STATUS.md`). *(Model: `pro`)*
- [x] **1.2** Gradle & Kotlin plugin alignment (JVM 17, Compose dependencies). *(Model: `flash`)*
- [x] **1.3** Compose UI syntax fixes (Sp letter spacing, chip borders, navigation). *(Model: `flash`)*
- [x] **1.4** Room raw query & worker cleanup. *(Model: `flash`)*
- [x] **1.5** Unit test stabilization (Mockito, Robolectric tests passing). *(Model: `flash`)*

---

### 🟢 Phase 2: Wallpaper DNA & Schema Evolution (v3) *(COMPLETED)*
*Goal: Provide every wallpaper with a rich metadata profile (DNA) extracted locally on import.*

- [x] **2.1 Room Migration v2 → v3** *(Model: `pro`)*
  - Add fields to `WallpaperEntity`: `rating` (0–5 Float), `dominantColor` (Int), `secondaryColor` (Int), `brightness` (Float), `isDark` (Boolean), `skipCount` (Int), `likeCount` (Int), `viewCount` (Int), `lastSkipped` (Long), `isPrivate` (Boolean), `style` (String?), `mood` (String?).
  - Create `Migration_2_3` with SQLite indexes on `(rating, lastUsed, isFavorite, brightness, isDark)`.
  - Create Room migration unit tests.
- [x] **2.2 Fast Wallpaper DNA Analyzer** *(Model: `flash`)*
  - Implement `WallpaperDnaAnalyzer` using downsampled bitmap sampling (< 10ms execution).
  - Calculate luminance brightness ($0.0 \dots 1.0$), dominant/secondary color extraction, and dark mode boolean.
  - Integrate DNA extraction into the import pipeline (`WallpaperRepository.importWallpaper`).
- [x] **2.3 DAO & Repository Updates for DNA** *(Model: `flash_lite`)*
  - Add update methods: `updateRating`, `recordSkip`, `recordLike`, `recordView`, `updatePrivacy`, `updateStyleAndMood`, `updateWallpaperDna`, `backfillDnaIfNeeded`.
  - Update `WallpaperDao` and `WallpaperRepository`.
  - Comprehensive unit test suite in `WallpaperDnaAnalyzerTest` and `WallpaperDaoTest`.

---

### 🟢 Phase 3: Intelligent Selection & Scoring Engine *(COMPLETED)*
*Goal: Move from simple random selection to smart taste-based shuffle, weighted favorites, and anti-repetition.*

- [x] **3.1 SQL-First Wallpaper Scoring Engine** *(Model: `pro`)*
  - SQLite dynamic scoring query combining `isFavorite`, `rating`, `likeCount`, `skipCount`, `lastSkipped`, `lastUsed` decay, and jitter.
- [x] **3.2 Smart Shuffle Strategy (`SmartShuffleSelectionStrategy`)** *(Model: `pro`)*
  - Strategy using SQL scoring query with fallback resilience.
- [x] **3.3 Weighted Favorites Strategy (`WeightedFavoritesSelectionStrategy`)** *(Model: `flash`)*
  - Probabilistic 75/25 favorite vs discovery pool selection.
- [x] **3.4 Anti-Repetition & Never-Repeat & Variety Modes** *(Model: `flash`)*
  - `NeverRepeatSelectionStrategy` prioritizing unused/oldest wallpapers.
  - `VarietySelectionStrategy` avoiding consecutive repetition of style, mood, or dominant color.
- [x] **3.5 Selection Explanation & History Audit** *(Model: `flash_lite`)*
  - Added `selectionReason` column to `WallpaperHistoryEntity` via `MIGRATION_3_4`.
  - Connected `selectionReason` logging in `RotateWallpaperUseCase`.
- [x] **3.6 Comprehensive Selection Strategy Unit Tests** *(Model: `flash`)*
  - Added unit test suites for all 7 strategies in `SelectionStrategyTest` and `RotateWallpaperUseCaseTest`.

---

### 🟢 Phase 4: Advanced Search, Filtering & Smart Collections *(COMPLETED)*
*Goal: Enable instantaneous filtering by color, brightness, orientation, rating, and rule-based smart collections.*

- [x] **4.1 Dynamic Multi-Filter SQLite Query Builder** *(Model: `pro`)*
  - Support compound queries: `searchQuery (title/style/mood)`, `minRating`, `darkOnly`, `brightness`, `orientation`, `sortOrder` (`RATING_DESC`, `MOST_LIKED`, `MOST_VIEWED`, etc.).
- [x] **4.2 Rule-Based Smart Filtering Integration** *(Model: `flash`)*
  - Integrated dynamic filter clauses into `WallpaperRepository.getFilteredWallpapers`.
- [x] **4.3 UI: Modern Filter Drawer / Bottom Sheet in Library** *(Model: `flash`)*
  - Interactive quick filter chips (All, Favorites, Dark/OLED, Top Rated 4★+, Portrait, Landscape).
  - Modal bottom sheet filter drawer with rating, OLED dark mode, sort orders, and reset options.
- [x] **4.4 UI: Wallpaper Detail DNA Card & Quick Actions** *(Model: `flash`)*
  - Interactive 5-star rating bar, Like/Skip action buttons, dominant/secondary color swatches with hex codes, luminance metrics, and style/mood chips.
- [x] **4.5 UI: Selection Reason Badge on History Screen** *(Model: `flash_lite`)*
  - Styled selection reason badges on rotation history cards.

---

### 🟢 Phase 5: AI & On-Device Metadata Tagging *(COMPLETED)*
*Goal: Local-first categorization, style/mood recognition, and duplicate detection.*

- [x] **5.1 On-Device Visual Feature & Style/Mood Classifier (`WallpaperTagClassifier`)** *(Model: `pro`)*
  - Extracted color distribution, saturation, edge density, luminance contrast, and OLED purity to infer styles (*Cyberpunk, Nature, Minimalist, Sci-Fi, Anime, Monochrome, Abstract, Architecture*) and moods (*Calm, Energetic, Mysterious, Ethereal, Serene*).
  - Automated tag recommendation engine generating structured keyword tags.
- [x] **5.2 Background Idle Tagging Worker (`WallpaperTaggingWorker`)** *(Model: `flash`)*
  - WorkManager background worker scheduled with battery-not-low constraints (`WallpaperScheduler.schedulePeriodicTagging`).
  - Automated backfilling of DNA, perceptual hashes, and AI tags for imported & unanalyzed wallpapers.
- [x] **5.3 Perceptual Hash Visual Similarity & Duplicate Finder Engine (`PerceptualHashAnalyzer`)** *(Model: `pro`)*
  - Implemented 64-bit difference hash (`dHash`) and 64-bit DCT perceptual hash (`pHash`) with scale/compression invariance.
  - Hamming distance calculation, near-duplicate detection ($\le 5$ bits difference), duplicate cluster grouping (`findSimilarWallpapers`, `findDuplicateClusters`).
  - Added Room `MIGRATION_4_5` indexing `contentHash` column.
- [x] **5.4 Semantic Multi-Tag Natural Search & UI Integration** *(Model: `flash`)*
  - Enhanced `WallpaperRepository.getFilteredWallpapers` with multi-tag joined SQLite search.
  - Added "Visually Similar" carousel gallery in `WallpaperDetailScreen`.

---

### 🟢 Phase 6: Context-Aware Automation & Scheduling *(COMPLETED)*
*Goal: Dynamic wallpaper switching triggered by time-of-day, battery levels, and environmental conditions.*

- [x] **6.1 Time-of-Day Profiles & Sunrise/Sunset Engine (`SolarCalculator` & `TimeOfDayProfile`)** *(Model: `flash`)*
  - Dawn, Morning, Afternoon, Golden Hour, Evening, Deep Night astronomical solar position engine.
  - Implemented `TimeOfDaySelectionStrategy` matching optimal brightness/darkness, styles, and moods.
- [x] **6.2 Context-Aware Triggers (`ContextTriggerReceiver` & `ContextTriggerManager`)** *(Model: `flash`)*
  - Broadcast receiver & hardware listener reacting to AC power connect/disconnect, battery saver mode, and DND.
  - Automatic OLED dark shift during battery saver activation and vibrant profile on charging.
- [x] **6.3 Multi-Schedule Priority & Conflict Resolution** *(Model: `pro`)*
  - Added Room `MIGRATION_5_6` adding `triggerType` and `priority` to `ScheduleEntity`.
  - Added priority-ranked execution and anti-thrashing in `RotateWallpaperUseCase.rotateWithContextTrigger`.
  - Updated `SchedulesScreen` with `TIME_OF_DAY`, `SMART_SHUFFLE`, `VARIETY`, and trigger type selectors.

---

### 🟢 Phase 7: Library Maintenance, Analytics & Backup/Restore *(COMPLETED)*
*Goal: Data health, usage insights, and complete portability.*

- [x] **7.1 Library Maintenance & Health Wizard (`LibraryHealthManager`)** *(Model: `flash`)*
  - Broken file/URI detector, low-res identifier, unused wallpaper cleaner, storage visualizer, and duplicate cluster resolution.
- [x] **7.2 Usage Statistics & Insights Dashboard (`AnalyticsManager`)** *(Model: `flash`)*
  - Rotation frequency breakdown (Scheduled, Context Triggers, Manual), OLED dark percentage, top styles, top moods, and rating averages.
- [x] **7.3 JSON Portable Backup / Restore Engine (`BackupRestoreEngine`)** *(Model: `pro`)*
  - Portable full-library export and restore with relations (collections, tags, schedules, history, preferences) and verification.

---

### 🟢 Phase 8: Live Shaders, Wallpaper Editor & Android Ecosystem *(COMPLETED)*
*Goal: Deep Android integration, live rendering, and interactive controls.*

- [x] **8.1 Live Wallpaper Service with AGSL Shaders (`LiveWallpaperEngineService`)** *(Model: `pro`)*
  - Android `WallpaperService` engine with GL / AGSL RuntimeShader rendering, touch wave ripples, and battery-friendly frame pacing.
- [x] **8.2 On-Device Wallpaper Editor (`WallpaperEditorProcessor`)** *(Model: `flash`)*
  - Non-destructive crop, brightness/contrast, vignette, saturation, and OLED pure black floor crushed adjustments.
- [x] **8.3 Android Quick Settings Tile (`WallpaperTileService`)** *(Model: `flash_lite`)*
  - Fast-action Quick Settings tile for immediate wallpaper rotation.
- [x] **8.4 Notification Controls with Skip & Favorite (`WallpaperNotificationManager` & `WallpaperActionReceiver`)** *(Model: `flash_lite`)*
  - Direct notification actions for "SKIP NEXT" and "★ Favorite" with broadcast receivers.

---

### 🟢 Phase 9: Production Polish, Release Engineering & Performance Optimization *(COMPLETED)*
*Goal: Production hardening, R8/ProGuard rules, release build validation, and UI test coverage.*

- [x] **9.1 ProGuard & R8 Optimization Rules** *(Model: `flash`)*
  - `proguard-rules.pro` keeping Room entities, DAOs, Kotlin reflection, Coil, WorkManager, AGSL shaders.
- [x] **9.2 Compose UI Flow & Screen Component Tests (`SettingsViewModelTest`)** *(Model: `flash`)*
  - Robolectric/Compose tests for theme switches, performance profiles, and health scans.
- [x] **9.3 Production Release Build Verification** *(Model: `pro`)*
  - ProGuard rules validation and release-ready build verification.

---

### 🟢 Phase 10: Jetpack Glance Interactive Home Screen Widgets *(COMPLETED)*
*Goal: Home screen control and interactive wallpaper preview.*

- [x] **10.1 Glance App Widget Layout & Actions (`widget_wallpaper_engine.xml` & `WallpaperAppWidgetProvider`)** *(Model: `flash`)*
  - Current wallpaper thumbnail preview, Next Wallpaper shuffle button, and Star favorite button.
- [x] **10.2 Glance App Widget Receiver & Sync Engine (`WallpaperActionReceiver.updateAllWidgets`)** *(Model: `flash_lite`)*
  - Auto-updates widget state on rotation.

---

### 🟢 Phase 11: Remote Wallpaper Sources & Online Discovery *(COMPLETED)*
*Goal: Curated cloud feeds, category drops, and one-tap imports.*

- [x] **11.1 Remote Wallpaper Feed Client & Category Metadata (`RemoteWallpaperCatalogue`)** *(Model: `flash`)*
  - Remote wallpaper model, curated catalogue provider with category drops (AMOLED, Cyberpunk, Nature, Anime, Sci-Fi).
- [x] **11.2 Online Feed Downloader & Local Importer (`RemoteWallpaperManager`)** *(Model: `pro`)*
  - Safe network download with byte stream progress, DNA generation on download, and library insertion.

---

## 📌 How to Resume Next Session

When starting a new session or resuming after tokens renew:
1. Open [`ROADMAP_PROGRESS.md`](file:///Users/akshar/Documents/projects/wallEngine/ROADMAP_PROGRESS.md).
2. Check the **Current Phase** and **Current Active Subtask** at the top.
3. Prompt the agent:
   > *"Continue with Subtask [X.X] from ROADMAP_PROGRESS.md using [Model Tier]"*
