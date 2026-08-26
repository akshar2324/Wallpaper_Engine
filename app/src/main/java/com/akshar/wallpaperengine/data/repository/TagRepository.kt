package com.akshar.wallpaperengine.data.repository

import com.akshar.wallpaperengine.data.local.dao.TagDao
import com.akshar.wallpaperengine.data.local.dao.WallpaperDao
import com.akshar.wallpaperengine.data.local.entity.TagEntity
import com.akshar.wallpaperengine.data.local.entity.WallpaperTagCrossRef
import kotlinx.coroutines.flow.Flow

class TagRepository(
    private val tagDao: TagDao,
    private val wallpaperDao: WallpaperDao? = null
) {

    val allTags: Flow<List<TagEntity>> = tagDao.getAllTags()

    suspend fun createTag(name: String): Long {
        val normalized = name.trim().lowercase()
        if (normalized.isBlank()) return -1L
        val existing = tagDao.getTagByName(normalized)
        if (existing != null) return existing.id
        return tagDao.insertTag(TagEntity(name = normalized))
    }

    suspend fun deleteTag(tag: TagEntity) = tagDao.deleteTag(tag)

    fun getTagsForWallpaper(wallpaperId: Long): Flow<List<TagEntity>> = tagDao.getTagsForWallpaper(wallpaperId)

    suspend fun getTagsForWallpaperList(wallpaperId: Long): List<TagEntity> =
        tagDao.getTagsForWallpaperList(wallpaperId)

    suspend fun createAndAssignTags(wallpaperId: Long, tagNames: List<String>) {
        if (wallpaperDao == null) return
        for (tagName in tagNames) {
            val normalized = tagName.trim().lowercase()
            if (normalized.isNotBlank()) {
                val tagId = createTag(normalized)
                if (tagId > 0) {
                    wallpaperDao.insertWallpaperTagCrossRef(
                        WallpaperTagCrossRef(wallpaperId = wallpaperId, tagId = tagId)
                    )
                }
            }
        }
    }

    suspend fun clearTagsForWallpaper(wallpaperId: Long) = tagDao.clearTagsForWallpaper(wallpaperId)
}
