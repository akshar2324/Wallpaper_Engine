package com.akshar.wallpaperengine.data.repository

import com.akshar.wallpaperengine.data.local.dao.CollectionDao
import com.akshar.wallpaperengine.data.local.entity.CollectionEntity
import kotlinx.coroutines.flow.Flow

class CollectionRepository(private val collectionDao: CollectionDao) {

    val allCollections: Flow<List<CollectionEntity>> = collectionDao.getAllCollections()
    val collectionCount: Flow<Int> = collectionDao.getCollectionCount()

    suspend fun getCollectionById(id: Long): CollectionEntity? = collectionDao.getCollectionById(id)

    suspend fun createCollection(name: String, description: String = "", coverUri: String? = null): Long {
        return collectionDao.insertCollection(
            CollectionEntity(name = name, description = description, coverUri = coverUri)
        )
    }

    suspend fun updateCollection(collection: CollectionEntity) = collectionDao.updateCollection(collection)

    suspend fun deleteCollection(collection: CollectionEntity) = collectionDao.deleteCollection(collection)
}
