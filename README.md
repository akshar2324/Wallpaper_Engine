# AURA

**AURA is a private, local-first Android wallpaper companion.** It helps you build a personal library, discover the right image for the moment, and rotate wallpapers with intent instead of randomness.

Designed for OLED displays and built with Jetpack Compose, AURA keeps the interface quiet so the artwork stays in focus. Your collection, preferences, and visual analysis remain on your device.

![AURA website preview](docs/assets/chrome-ribbon-wallpaper.png)

## Why AURA

Most wallpaper apps are noisy feeds. AURA is for the collection you already care about.

- **Keep it personal**: import wallpapers from your device and organize them locally.
- **Make every change feel considered**: use schedules, context signals, favourites, history, and visual variety instead of blind shuffle.
- **Respect OLED hardware**: identify darker imagery and make near-black wallpaper choices easy.
- **Stay private by default**: no account is required for your own library.

## Product Experience

### Home
The AURA home screen is intentionally spare: your active wallpaper, one primary action, and a clear view of the next rotation. It is designed to feel calm at a glance.

### Library
Search, filter, import, favourite, and curate an image-first wallpaper gallery. The Library supports orientation, rating, OLED, tag, and favourites filters, with batch selection for collection management.

### Explore
Browse remote wallpaper drops with an editorial, image-led layout. Downloaded wallpapers are imported directly into the local AURA library.

### Rotation
Set up automatic wallpaper changes through a clean current-to-next timeline. AURA supports scheduled, smart, sequential, random, least-recently-used, weighted-favourite, variety, and no-repeat selection strategies.

### Detail
Preview wallpapers fullscreen, set them on home or lock screen, edit metadata, rate them, manage tags and collections, and inspect their visual characteristics.

## Core Capabilities

| Area | What AURA does |
| --- | --- |
| Local library | Imports image URIs, stores metadata in Room, and supports search and dynamic filtering. |
| Visual DNA | Analyses colour, brightness, contrast, darkness, and similarity to help make better selections. |
| Smart rotation | Scores candidates using selection strategy, context, favourites, history, and variety. |
| Context triggers | Supports time, charging, battery saver, do-not-disturb, and other context-aware inputs. |
| Wallpaper controls | Applies wallpapers to home, lock, or both through Android wallpaper APIs. |
| Organisation | Collections, tags, ratings, favourites, history, batch actions, and duplicate maintenance. |
| OLED awareness | Surfaces dark and near-black artwork for a lower-luminance visual experience. |
| Live effects | Includes an optional Android 13+ AGSL live wallpaper surface with safe static fallbacks. |

## Design System

AURA takes its visual direction from premium Android hardware: true black, restrained electric lime, soft charcoal surfaces, and generous negative space.

- **Background:** `#050505`
- **Surface:** `#111114`
- **Accent:** `#C8FF3D`
- **Typography:** compact, readable, sentence-case hierarchy
- **Shape:** 12dp controls, 16dp image tiles, minimal outlines
- **Motion:** short press feedback and image-led transitions; no decorative noise

The source design direction is maintained in the companion [GitHub Pages site](https://akshar2324.github.io/Wallpaper_Engine/).

## Architecture

```text
app/
  data/          Room entities, DAOs, repositories, image analysis, preferences
  domain/        rotation strategies and use cases
  scheduler/     WorkManager scheduling and wallpaper change orchestration
  wallpaper/     Android wallpaper and live wallpaper services
  ui/            Jetpack Compose screens, components, theme, and view models
  widget/        Android home-screen widget integration
```

The app follows a pragmatic MVVM structure:

1. Compose screens render `StateFlow` view states.
2. View models coordinate user actions and asynchronous work.
3. Repositories own local data and domain-facing operations.
4. Room persists wallpapers, tags, collections, schedules, and history.
5. WorkManager handles reliable deferred rotation and maintenance work.

## Performance Principles

AURA is built to stay responsive with large image libraries.

- Lazy Compose grids and rows keep off-screen content out of composition.
- Coil handles image caching and bounded preview decode sizes for hero and detail screens.
- Image import, analysis, persistence, scheduling, and cleanup run off the main thread.
- Room queries are observed through flows so UI updates are incremental.
- Procedural sample wallpapers avoid allocating bitmap assets during development.
- Background tasks use WorkManager rather than long-lived UI work.

## Technology

- **Language:** Kotlin
- **UI:** Jetpack Compose and Material 3
- **Persistence:** Room / SQLite
- **Async:** Kotlin Coroutines and Flow
- **Background work:** WorkManager
- **Images:** Coil
- **Navigation:** Navigation Compose
- **Wallpaper APIs:** `WallpaperManager` and `WallpaperService`
- **Shaders:** AGSL on Android 13+ with compatible fallbacks
- **Tests:** JUnit, Robolectric, Mockito, Compose UI test dependencies, and Roborazzi

## Requirements

- Android Studio with Android SDK 35 installed
- JDK 17
- Android device or emulator running Android 7.0 (API 24) or later

## Build and Test

```bash
# Debug APK
./gradlew :app:assembleDebug

# Unit tests
./gradlew testDebugUnitTest
```

For this workspace, use JDK 17 if your shell is pointing at a newer unsupported Java release:

```bash
export JAVA_HOME="$HOME/.gradle/jdks/eclipse_adoptium-17-aarch64-os_x.2/jdk-17.0.19+10/Contents/Home"
export PATH="$JAVA_HOME/bin:$PATH"
```

The debug APK is generated at:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Privacy

Your wallpaper library is local by design. AURA does not require an account for importing, organising, analysing, or rotating wallpapers. Remote discovery is an explicit Explore action; downloaded items become part of the local library.

## Current Status

The core AURA experience is implemented: branding, Home, Library, Explore, Rotation, Wallpaper Detail, Settings, import, local persistence, wallpaper application, and unit-test coverage are in place.

The next product-quality milestone is real-device profiling across a large image library, followed by screenshot testing and release packaging.

## License

This repository does not currently declare a license. Add one before distributing or accepting outside contributions.
