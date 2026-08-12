package com.akshar.wallpaperengine.data.repository

import android.content.Context
import android.net.Uri
import com.akshar.wallpaperengine.data.local.dao.*
import com.akshar.wallpaperengine.data.local.entity.*
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.OutputStream

@JsonClass(generateAdapter = true)
data class BackupMetadata(
    val version: Int = 1,
    val timestamp: Long = System.currentTimeMillis(),
    val collections: List<CollectionEntity>,
    val tags: List<TagEntity>,
    val playlists: List<PlaylistEntity>,
    val schedules: List<ScheduleEntity>
)

class BackupService(
    private val context: Context,
    private val collectionDao: CollectionDao,
    private val tagDao: TagDao,
    private val playlistDao: PlaylistDao,
    private val scheduleDao: ScheduleDao
) {

    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    private val adapter = moshi.adapter(BackupMetadata::class.java)

    suspend fun exportMetadata(outputUri: Uri): Boolean = withContext(Dispatchers.IO) {
        try {
            // Note: Does not backup the massive raw Wallpaper/History tables initially, just the configuration structures.
            // Wait, we need to export everything to make it a full config backup?
            // Actually, we'll export Collections, Tags, Playlists, Schedules here per prompt instructions.
            // "The backup should contain metadata... version: 1"

            val cols = collectionDao.getAllCollectionsList()
            val tags = tagDao.getAllTagsList()
            val playlists = playlistDao.getAllPlaylistsList()
            val schedules = scheduleDao.getAllSchedulesList()

            val backup = BackupMetadata(
                version = 1,
                collections = cols,
                tags = tags,
                playlists = playlists,
                schedules = schedules
            )

            val json = adapter.toJson(backup)

            context.contentResolver.openOutputStream(outputUri)?.use { stream: OutputStream ->
                stream.write(json.toByteArray())
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun importMetadata(inputUri: Uri): Boolean = withContext(Dispatchers.IO) {
        try {
            val json = context.contentResolver.openInputStream(inputUri)?.use { stream: InputStream ->
                stream.bufferedReader().use { it.readText() }
            } ?: return@withContext false

            val backup = adapter.fromJson(json) ?: return@withContext false

            if (backup.version > 1) {
                // Unsupported newer version
                return@withContext false
            }

            // Safe merge strategy: Just insert/replace configurations without destroying existing user walls
            backup.collections.forEach { collectionDao.insertCollection(it.copy(id = 0)) }
            backup.tags.forEach { tagDao.insertTag(it.copy(id = 0)) }
            backup.playlists.forEach { playlistDao.insertPlaylist(it.copy(id = 0)) }
            backup.schedules.forEach { scheduleDao.insertSchedule(it.copy(id = 0)) }

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
