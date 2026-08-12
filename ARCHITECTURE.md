# Wallpaper Engine Architecture

This document describes the architectural layout and data flow for the Wallpaper Engine application.

## Overview
The application follows standard Android architecture guidelines with a unidirectional data flow (UDF), built primarily on MVVM.

### Layers:
1. **UI Layer (Jetpack Compose & ViewModels)**:
   - Composables listen to `StateFlow` streams exposed by ViewModels.
   - ViewModels encapsulate UI state preparation and directly call the Repository or UseCases.
   - Avoids passing `Entities` deeply into the UI where possible, prioritizing lightweight UI representations.

2. **Domain Layer (UseCases & Strategies)**:
   - Contains business logic that bridges multiple repositories or performs complex actions outside of simple CRUD.
   - Examples: `RotateWallpaperUseCase` (handles the complex logic of checking schedules, selecting the wallpaper, attempting platform application, and updating history).
   - Selection logic (`WallpaperSelectionStrategy`) is backed by efficient SQL generation to avoid loading entire ID lists into memory for operations like Random, Sequential, or LRU selection.

3. **Data Layer (Repositories & Room DAOs)**:
   - `WallpaperRepository`, `CollectionRepository`, `ScheduleRepository`, etc. expose Flow streams.
   - Room DAOs process all local storage.
   - For performance with large datasets, dynamic querying is utilized via SQLite's `SupportSQLiteQuery` alongside Room's `@RawQuery` feature. This enables complex combination filters (Tags + Collections + Sorting) directly in SQL.

## The Wallpaper Import Flow
1. User selects images via Android Photo Picker (`ActivityResultContracts.GetContent` or `GetMultipleContents`).
2. `LibraryViewModel` handles the incoming URIs.
3. It attempts `takePersistableUriPermission`.
4. If a `SecurityException` is thrown (meaning the URI permissions cannot be persisted), it gracefully handles the failure by copying the `InputStream` contents to the app's internal storage (`context.filesDir`).
5. A `WallpaperEntity` with the new permanent URI is created and inserted into the Room Database.

## The Scheduler & Worker System
The system relies on Android's `WorkManager` for guaranteed background execution.

- **WallpaperScheduler**: Handles adding or removing Unique Periodic Work Requests based on user-defined parameters in the UI.
- **WallpaperChangeWorker**: A `CoroutineWorker` triggered by the system.
- **Data Flow**:
  `WallpaperChangeWorker` -> `RotateWallpaperUseCase` -> `WallpaperSelectionStrategy` (SQL) -> `AndroidWallpaperService`
- Process death and reboots are handled naturally by WorkManager restoring the worker constraints.

## Visuals & Shaders
- A specialized `@Composable` `ShaderBackground` intercepts the drawing layer behind the app's main views.
- **Android 13+ (API 33)**: Leverages `RuntimeShader` (AGSL) to compile GPU-bound effects (Nebula, Aurora, Cyber Grid, Void) directly onto the rendering hardware, minimizing CPU impact.
- **Older Versions**: Falls back to static gradients to prevent severe CPU lag, as continuous `Canvas` recomposition kills battery life.
