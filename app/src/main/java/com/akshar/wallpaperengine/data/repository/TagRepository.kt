package com.akshar.wallpaperengine.data.repository

import com.akshar.wallpaperengine.data.local.dao.TagDao
import com.akshar.wallpaperengine.data.local.entity.TagEntity
import kotlinx.coroutines.flow.Flow

class TagRepository(private val tagDao: TagDao) {

    val allTags: Flow<List<TagEntity>> = tagDao.getAllTags()

    suspend fun createTag(name: String): Long {
        val normalized = name.trim().lowercase()
        val existing = tagDao.getTagByName(normalized)
        if (existing != null) return existing.id
        return tagDao.insertTag(TagEntity(name = normalized))
    }

    suspend fun deleteTag(tag: TagEntity) = tagDao.deleteTag(tag)

    fun getTagsForWallpaper(wallpaperId: Long): Flow<List<TagEntity>> = tagDao.getTagsForWallpaper(wallpaperId)
}
