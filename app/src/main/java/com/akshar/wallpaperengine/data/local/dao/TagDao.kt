package com.akshar.wallpaperengine.data.local.dao

import androidx.room.*
import com.akshar.wallpaperengine.data.local.entity.TagEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TagDao {

    @Query("SELECT * FROM tags ORDER BY name ASC")
    fun getAllTags(): Flow<List<TagEntity>>

    @Query("SELECT * FROM tags ORDER BY name ASC")
    suspend fun getAllTagsList(): List<TagEntity>

    @Query("SELECT * FROM tags WHERE name = :name")
    suspend fun getTagByName(name: String): TagEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTag(tag: TagEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTags(tags: List<TagEntity>): List<Long>

    @Delete
    suspend fun deleteTag(tag: TagEntity)

    @Query("""
        SELECT t.* FROM tags t
        INNER JOIN wallpaper_tag_cross_ref ref ON t.id = ref.tagId
        WHERE ref.wallpaperId = :wallpaperId
    """)
    fun getTagsForWallpaper(wallpaperId: Long): Flow<List<TagEntity>>

    @Query("""
        SELECT t.* FROM tags t
        INNER JOIN wallpaper_tag_cross_ref ref ON t.id = ref.tagId
        WHERE ref.wallpaperId = :wallpaperId
    """)
    suspend fun getTagsForWallpaperList(wallpaperId: Long): List<TagEntity>

    @Query("DELETE FROM wallpaper_tag_cross_ref WHERE wallpaperId = :wallpaperId")
    suspend fun clearTagsForWallpaper(wallpaperId: Long)
}
