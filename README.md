# Wallpaper Engine

A high-performance OLED wallpaper rotation engine built with Jetpack Compose, Room, and WorkManager.

## Features
* **Wallpaper Library Management**: Efficiently store, sort, filter, and search through thousands of high-resolution wallpapers.
* **Collections & Tags**: Organize your favorite wallpapers into targeted collections and tag them for quick access.
* **Intelligent Scheduling Engine**: Set up background rotations with specific selection strategies (Random, Sequential, Least Recently Used) tied to customized schedules.
* **AGSL Shaders**: Hardware-accelerated GPU shader backgrounds for devices on Android 13 (Tiramisu)+, with battery-friendly static fallbacks for older devices.
* **Favorites & History Tracking**: View the timeline of previously applied wallpapers with single-tap rollback capabilities.

## Tech Stack
* **Language**: Kotlin
* **UI Toolkit**: Jetpack Compose
* **Local Database**: Room (SQLite) with `@RawQuery` dynamic filtering
* **Background Processing**: WorkManager (Coroutines)
* **Image Loading**: Coil
* **Dependency Injection**: Manual DI via `WallpaperEngineApplication`
* **Architecture**: MVVM with UseCases for domain logic

## Build Instructions
This project requires Gradle 8.7+ and Java 17+.

1. Clone the repository.
2. Ensure you have the `debug.keystore` file in the root directory for debug builds, or let Gradle generate one via `keytool`.
3. Set your internal `STORE_PASSWORD` and `KEY_PASSWORD` as environment variables if doing a release build.
4. Run the app:
   ```bash
   ./gradlew assembleDebug
   ```

## Limitations
* Exact-time scheduling is not supported by default due to Android's aggressive background battery restrictions. WorkManager is used to reliably perform daily or periodic background changes instead.
