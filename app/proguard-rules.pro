# ProGuard & R8 Optimization Rules for Wallpaper Engine

# Preserve line numbers and source files for clean stack traces
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Room Database & Entities
-keep class androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }
-keep class com.akshar.wallpaperengine.data.local.entity.** { *; }
-keep class com.akshar.wallpaperengine.data.local.dao.** { *; }
-keep class com.akshar.wallpaperengine.data.local.WallpaperDatabase_Impl { *; }

# WorkManager Workers
-keep class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}
-keep class com.akshar.wallpaperengine.workers.** { *; }

# Live Wallpaper & System Services
-keep class com.akshar.wallpaperengine.wallpaper.LiveWallpaperEngineService { *; }
-keep class com.akshar.wallpaperengine.service.WallpaperTileService { *; }
-keep class com.akshar.wallpaperengine.receiver.** { *; }

# Coil Image Loader
-keep class coil.** { *; }
-dontwarn coil.**

# Kotlin Coroutines & Flow
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}

# AndroidX DataStore
-keep class androidx.datastore.** { *; }
-keep class com.akshar.wallpaperengine.data.preferences.** { *; }

# JSON Serialization Models
-keep class com.akshar.wallpaperengine.data.backup.** { *; }
-keep class com.akshar.wallpaperengine.data.analytics.** { *; }
-keep class com.akshar.wallpaperengine.data.maintenance.** { *; }
