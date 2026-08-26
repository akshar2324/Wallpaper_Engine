Yes. I can give you the file content directly so you can save it as **`GEMINI.md`** in the root of the repository.

This is designed specifically for **Antigravity CLI**: it tells the agent to clone/inspect your repo, understand the architecture before changing anything, preserve your existing work, run tests/builds, and work incrementally.

````markdown
# GEMINI.md — Wallpaper Engine Agent Instructions

## Project

Repository:
https://github.com/akshar2324/Wallpaper_Engine

Project: Wallpaper Engine

Platform: Android

Primary technologies:
- Kotlin
- Jetpack Compose
- MVVM / UDF
- Room
- WorkManager
- Coil
- AGSL / RuntimeShader
- Android WallpaperService

You are the primary coding agent for this repository.

Your goal is to understand the existing project completely before making significant changes. Treat this as a real production application.

---

# 1. FIRST: UNDERSTAND THE PROJECT

When you first start working on this repository, DO NOT immediately rewrite or refactor code.

First inspect:

- Complete repository tree
- README
- ARCHITECTURE.md
- REPORT.md
- Gradle configuration
- AndroidManifest.xml
- All Kotlin source files
- Compose UI
- Resources
- Room entities
- DAO interfaces
- Repositories
- ViewModels
- Use cases
- Wallpaper service
- Workers
- Scheduler
- Shader implementation
- Tests

Determine:

- Android compile SDK
- target SDK
- minimum SDK
- Kotlin version
- Gradle version
- All major dependencies
- Application architecture
- Database schema
- Navigation structure
- Background execution flow
- Wallpaper selection logic
- Import/storage logic
- Theme system
- Shader system

Before modifying anything, create/update:

`AGENT_PROJECT_STATUS.md`

It should document:

1. Architecture
2. Important files
3. Data flow
4. Wallpaper rotation flow
5. Database structure
6. Background execution
7. UI structure
8. Existing tests
9. Build status
10. Known bugs
11. Recommended improvements

Do not claim something works unless you actually verified it.

---

# 2. CHECK GIT FIRST

Before modifying anything:

```bash
git status
git branch --show-current
git log -5 --oneline
````

Preserve all existing user changes.

NEVER overwrite unrelated local work.

Do not use destructive commands such as:

```bash
git reset --hard
git clean -fd
rm -rf
```

unless the user explicitly requests it.

Do not push changes to GitHub unless explicitly asked.

---

# 3. BUILD AND TEST FIRST

Before making changes, determine the current project health.

Run appropriate Gradle commands, for example:

```bash
./gradlew test
```

and:

```bash
./gradlew assembleDebug
```

If these fail, record the failures.

Clearly distinguish:

* Existing failures
* Failures caused by your changes

Never hide existing failures.

---

# 4. PRODUCT UNDERSTANDING

Wallpaper Engine is NOT merely a simple wallpaper changer.

It is intended to be a:

> Local-first Android wallpaper library + intelligent wallpaper selection engine + scheduling system + visual customization platform.

Major functionality includes:

* Wallpaper library
* Wallpaper importing
* Collections
* Wallpaper history
* Automatic wallpaper rotation
* Scheduling
* Random selection
* Sequential selection
* LRU selection
* Wallpaper previews
* OLED-focused UI
* Multiple themes
* AGSL shaders
* Settings
* Performance optimization
* Background automation

The product should feel polished and premium.

Do not turn it into a generic CRUD application.

---

# 5. ARCHITECTURE

Respect the existing architecture.

Expected structure:

```text
Compose UI
    ↓
ViewModel
    ↓
Domain / Use Cases
    ↓
Repository
    ↓
Room / Local Storage
```

Background wallpaper flow:

```text
Scheduler
    ↓
WorkManager
    ↓
WallpaperChangeWorker
    ↓
RotateWallpaperUseCase
    ↓
WallpaperSelectionStrategy
    ↓
Repository / Database
    ↓
WallpaperService
```

Keep these responsibilities separated.

### UI

UI should:

* Display state
* Send user actions
* Avoid business logic

### ViewModel

ViewModels should:

* Manage UI state
* Coordinate use cases
* Handle user actions

### Domain

Domain/use cases should contain:

* Business rules
* Wallpaper selection
* Rotation behavior
* Scheduling-related logic

### Repository

Repositories should:

* Abstract data sources
* Coordinate database/storage
* Hide implementation details from domain/UI

### Database

Room should own persistent structured data.

Do not directly access SQLite from UI.

---

# 6. LARGE WALLPAPER LIBRARIES

Assume users may have thousands of wallpapers.

Do NOT unnecessarily load an entire wallpaper library into memory.

Prefer:

* SQL filtering
* SQL ordering
* LIMIT
* Room Flow
* Pagination
* Database-side selection
* Indexed queries

Avoid patterns like:

```kotlin
val allWallpapers = repository.getAll()
```

followed by expensive in-memory processing when the same thing can be done efficiently by SQLite.

Always consider:

* RAM
* database performance
* query performance
* image decoding
* Compose recomposition

---

# 7. WALLPAPER IMPORT

Wallpaper import must work correctly with modern Android storage rules.

Expected flow:

```text
Android Photo Picker
        ↓
Persist URI permission when possible
        ↓
If persistence fails
        ↓
Copy image into app-controlled storage
        ↓
Store durable reference
        ↓
Room
```

Do not assume temporary content URIs remain valid forever.

When modifying import behavior, consider:

* URI permission revocation
* deleted source files
* unsupported providers
* large files
* EXIF orientation
* duplicate imports
* Android API differences
* storage permissions

Never introduce unnecessary broad storage permissions.

---

# 8. WALLPAPER SELECTION

Current selection concepts include:

## Random

Select a valid wallpaper while avoiding unnecessary repetition.

## Sequential

Move through wallpapers in deterministic order.

## LRU

Prefer wallpapers that have not been displayed recently.

When changing selection logic:

* Preserve history
* Update last-used information
* Ignore deleted wallpapers
* Handle inaccessible wallpapers
* Avoid duplicate simultaneous selections
* Consider concurrent workers

Do not assume WorkManager runs at an exact second.

Android may delay background work.

---

# 9. WALLPAPER SERVICE

Before modifying WallpaperService code, understand:

* Service lifecycle
* Engine lifecycle
* Surface lifecycle
* Bitmap handling
* Threading
* Cleanup
* Error handling

Avoid:

* Main-thread image decoding
* Large persistent Bitmaps
* Context leaks
* Coroutine leaks
* Surface leaks
* Unnecessary rendering

Wallpaper changes must not cause:

* crashes
* ANRs
* excessive memory use
* excessive battery drain

---

# 10. IMAGE LOADING

Coil is used for image loading.

For library/grid views:

* Prefer thumbnails
* Avoid full-resolution decoding
* Use appropriate image sizes
* Use caching
* Avoid unnecessary reloads
* Avoid excessive recomposition

For wallpaper display:

Consider:

* Resolution
* Aspect ratio
* Scaling
* Cropping
* EXIF orientation
* Memory usage

Never decode huge images unnecessarily.

---

# 11. COMPOSE UI

The project uses Jetpack Compose.

Maintain the existing visual identity.

Design goals:

* Premium
* Minimal
* OLED-friendly
* Smooth
* Responsive
* Accessible
* Modern
* Clear hierarchy

Use:

* LazyColumn
* LazyRow
* LazyVerticalGrid
* StateFlow
* Stable state

Avoid expensive work inside composables.

Do not put repository/database operations directly inside composables.

Avoid unnecessary recompositions.

---

# 12. THEMING

The project has multiple visual themes, including concepts such as:

* Abyss
* Midnight
* Crimson Night
* Moonlight

Do not scatter hard-coded colors across the UI.

Use the existing theme system.

If adding a theme:

* Integrate it into the existing theme architecture
* Preserve typography
* Preserve component consistency
* Support dark/OLED behavior

---

# 13. SHADERS

The project uses AGSL / RuntimeShader on supported Android versions.

Existing shader concepts include:

* Nebula
* Aurora
* Cyber Grid
* Void

Important:

* Do not create unnecessary continuous rendering
* Avoid high battery usage
* Respect Android API compatibility
* Provide lightweight fallbacks
* Shader failure must not crash the application

---

# 14. SCHEDULING

The project uses WorkManager for background wallpaper changes.

Remember:

WorkManager provides reliable deferred background execution, NOT exact wall-clock execution.

Do not promise exact execution times unless Android actually guarantees them.

When modifying scheduling:

* Avoid duplicate workers
* Handle cancellation
* Handle rescheduling
* Preserve user settings
* Handle reboot/device restart if applicable
* Avoid excessive background work

---

# 15. DATABASE

Before modifying the Room schema:

1. Inspect existing entities
2. Inspect DAOs
3. Inspect migrations
4. Understand existing data relationships

If a schema change is required:

* Create a proper migration
* Do not casually destroy the database
* Consider existing users
* Test migration behavior

Never use destructive migration merely to make development easier unless explicitly requested.

---

# 16. HISTORY

Wallpaper history is important product data.

When changing wallpaper:

* Record history correctly
* Maintain timestamps
* Preserve rollback behavior
* Avoid duplicate entries where inappropriate
* Handle deleted wallpapers gracefully

Do not break history while modifying selection logic.

---

# 17. COLLECTIONS

Collections should remain separate from the wallpaper storage layer.

When modifying collections:

* Preserve wallpaper relationships
* Handle deletion correctly
* Handle empty collections
* Avoid expensive loading of entire collections
* Keep UI state consistent

---

# 18. PERFORMANCE

Prioritize performance in this order:

1. Correctness
2. Crashes / data safety
3. Battery usage
4. Memory usage
5. UI smoothness
6. Database performance
7. Startup time

Do not optimize based on guesses.

Measure or reason from actual code.

---

# 19. SECURITY

Never commit:

* API keys
* passwords
* private keys
* signing keys
* keystores
* tokens
* credentials
* `.env` secrets

Use `.env.example` only as a template.

Inspect:

```bash
git status
git diff
```

before committing.

Never expose secrets in logs.

---

# 20. WHEN FIXING A BUG

Follow this process:

### Step 1

Reproduce or understand the bug.

### Step 2

Trace the relevant code path.

### Step 3

Find the root cause.

### Step 4

Explain the root cause briefly.

### Step 5

Implement the smallest correct fix.

### Step 6

Run relevant tests.

### Step 7

Run the build.

### Step 8

Inspect the final diff.

Do not rewrite unrelated code.

---

# 21. WHEN ADDING A FEATURE

Before implementation determine:

* Does the feature already partially exist?
* Which layer owns it?
* Does Room need a migration?
* Does WorkManager need modification?
* Does wallpaper history change?
* Does the UI need new state?
* Does Android permission behavior change?
* Does it affect large libraries?
* Does it require a dependency?

Reuse existing abstractions.

Do not introduce dependencies unless they provide meaningful value.

---

# 22. WHEN ASKED TO "IMPROVE THE APP"

Do NOT immediately rewrite it.

First perform an audit.

Rank issues:

```text
P0 — Crashes / data loss
P1 — Incorrect functionality
P2 — Battery / memory / performance
P3 — UX problems
P4 — Maintainability
P5 — Visual polish
```

Then propose improvements in priority order.

Only implement the requested scope.

---

# 23. CODE QUALITY

Prefer:

* Clear names
* Small functions
* Single responsibility
* Kotlin idioms
* Coroutines used correctly
* Structured concurrency
* Immutable UI state where appropriate
* Existing project conventions

Avoid:

* giant functions
* duplicated business logic
* unnecessary abstractions
* unnecessary interfaces
* global mutable state
* magic numbers
* silent exception swallowing

---

# 24. ERROR HANDLING

Never do this:

```kotlin
try {
    ...
} catch (e: Exception) {
}
```

unless there is a very specific reason.

Errors should be:

* handled
* logged appropriately
* exposed to UI when necessary
* recoverable when possible

Do not crash because an optional wallpaper has disappeared.

---

# 25. TESTING

After implementation run relevant tests.

Typical commands:

```bash
./gradlew test
```

```bash
./gradlew assembleDebug
```

If available:

```bash
./gradlew connectedDebugAndroidTest
```

Use the project's actual tasks if different.

When tests fail:

* inspect the failure
* determine whether it is related to your changes
* fix regressions
* report pre-existing failures honestly

---

# 26. GIT

Before work:

```bash
git status
git branch --show-current
git log -5 --oneline
```

After work:

```bash
git diff
git status
```

Do not push unless explicitly asked.

Keep commits focused if the user asks you to commit.

---

# 27. IMPORTANT: USER'S EXISTING WORK

The repository belongs to the user.

Never assume that existing code is disposable.

Before modifying:

* inspect it
* understand it
* preserve useful behavior

If you think a rewrite is better, explain why before performing a major rewrite.

---

# 28. FUTURE PRODUCT DIRECTION

Possible future features include:

* AI wallpaper tagging
* Smart collections
* Intelligent playlists
* Weighted favorites
* Better LRU
* Automatic wallpaper categorization
* Wallpaper metadata
* Advanced scheduling
* Personalization
* Large-library optimization

These are future directions only.

DO NOT implement them unless the user asks.

---

# 29. ANTIGRAVITY WORKING STYLE

Operate as an autonomous coding agent, but stay conservative with destructive actions.

You should:

* inspect first
* reason about architecture
* make focused changes
* run tests
* verify builds
* review diffs
* report results

For large tasks, break work into logical stages.

After each major stage:

1. Build
2. Test
3. Inspect diff

Do not make hundreds of unrelated changes in one pass.

---

# 30. INITIAL TASK

When starting this repository for the first time, do the following:

```text
1. Clone/open the repository.
2. Inspect the complete file tree.
3. Read README.md.
4. Read ARCHITECTURE.md.
5. Read REPORT.md.
6. Inspect Gradle configuration.
7. Inspect AndroidManifest.xml.
8. Inspect all application source.
9. Map the architecture.
10. Map the database.
11. Map wallpaper scheduling.
12. Map wallpaper selection.
13. Map WallpaperService.
14. Map image import/storage.
15. Map Compose navigation/UI.
16. Map themes.
17. Map shaders.
18. Inspect tests.
19. Run tests.
20. Run debug build.
21. Create/update AGENT_PROJECT_STATUS.md.
22. Only then wait for the user's development task.
```

Do not start adding new features automatically.

---

# FINAL PRINCIPLE

Understand before changing.

Measure before optimizing.

Preserve existing behavior.

Protect user data.

Respect Android lifecycle rules.

Keep the architecture clean.

Keep the application local-first.

Verify everything you claim.

When uncertain, inspect the repository and official Android documentation instead of guessing.

````

### How I'd use it with Antigravity CLI

Put the file at the **root**:

```text
Wallpaper_Engine/
├── GEMINI.md
├── README.md
├── ARCHITECTURE.md
├── REPORT.md
├── app/
├── gradle/
└── ...
````
