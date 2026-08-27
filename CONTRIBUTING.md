# Contributing to AURA

Thanks for helping improve AURA. The project values small, focused changes that preserve the local-first and image-led product experience.

## Before You Start

1. Search existing issues and pull requests before opening a new one.
2. Open an issue first for substantial features or visual changes so the direction can be agreed before implementation.
3. Keep unrelated cleanup out of feature and bug-fix pull requests.

## Development Setup

- JDK 17
- Android SDK 35
- Android Studio or the Gradle wrapper

```bash
./gradlew :app:assembleDebug
./gradlew testDebugUnitTest
```

## Code and Design Expectations

- Use Kotlin and existing Compose, Room, Coroutines, and WorkManager patterns.
- Keep disk, image, database, and network work off the main thread.
- Prefer lazy UI containers for wallpaper collections.
- Preserve the AURA design system: OLED black, restrained lime, image-first hierarchy, and quiet controls.
- Add or update tests whenever behavior changes.
- Do not commit local secrets, keystores, generated APKs, or IDE-specific project files.

## Pull Requests

Use a clear title, explain user-visible behavior, list validation performed, and include screenshots for visual changes. A pull request should leave `:app:assembleDebug` and `testDebugUnitTest` passing.
