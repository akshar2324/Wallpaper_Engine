package com.akshar.wallpaperengine.data.local.dao

import androidx.room.*
import androidx.sqlite.db.SupportSQLiteQuery
import com.akshar.wallpaperengine.data.local.entity.WallpaperCollectionCrossRef
import com.akshar.wallpaperengine.data.local.entity.WallpaperEntity
import com.akshar.wallpaperengine.data.local.entity.WallpaperTagCrossRef
import kotlinx.coroutines.flow.Flow

@Dao
interface WallpaperDao {

    @RawQuery(observedEntities = [WallpaperEntity::class, WallpaperCollectionCrossRef::class, WallpaperTagCrossRef::class])
    fun getWallpapersFiltered(query: SupportSQLiteQuery): Flow<List<WallpaperEntity>>

    @RawQuery
    suspend fun getSingleWallpaperId(query: SupportSQLiteQuery): Long?

    @RawQuery
    suspend fun getWallpaperIdsFiltered(query: SupportSQLiteQuery): List<Long>


    @Query("SELECT * FROM wallpapers ORDER BY dateAdded DESC")
    fun getAllWallpapers(): Flow<List<WallpaperEntity>>

    @Query("SELECT * FROM wallpapers WHERE id = :id")
    suspend fun getWallpaperById(id: Long): WallpaperEntity?

    @Query("SELECT * FROM wallpapers WHERE isFavorite = 1 ORDER BY dateAdded DESC")
    fun getFavoriteWallpapers(): Flow<List<WallpaperEntity>>

    @Query("SELECT * FROM wallpapers WHERE isFavorite = 1")
    suspend fun getFavoriteWallpapersList(): List<WallpaperEntity>

    @Query("SELECT * FROM wallpapers ORDER BY lastUsed DESC LIMIT :limit")
    fun getRecentlyUsedWallpapers(limit: Int): Flow<List<WallpaperEntity>>

    @Query("SELECT * FROM wallpapers ORDER BY lastUsed DESC LIMIT 1")
    fun getCurrentWallpaperFlow(): Flow<WallpaperEntity?>

    @Query("SELECT * FROM wallpapers ORDER BY lastUsed DESC LIMIT 1")
    suspend fun getCurrentWallpaper(): WallpaperEntity?

    @Query("SELECT COUNT(*) FROM wallpapers")
    fun getWallpaperCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWallpaper(wallpaper: WallpaperEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWallpapers(wallpapers: List<WallpaperEntity>): List<Long>

    @Update
    suspend fun updateWallpaper(wallpaper: WallpaperEntity)

    @Delete
    suspend fun deleteWallpaper(wallpaper: WallpaperEntity)

    @Query("DELETE FROM wallpapers WHERE id IN (:ids)")
    suspend fun deleteWallpapersByIds(ids: List<Long>)

    @Query("UPDATE wallpapers SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavorite(id: Long, isFavorite: Boolean)

    @Query("UPDATE wallpapers SET lastUsed = :lastUsed WHERE id = :id")
    suspend fun updateLastUsed(id: Long, lastUsed: Long = System.currentTimeMillis())

    @Query("UPDATE wallpapers SET rating = :rating WHERE id = :id")
    suspend fun updateRating(id: Long, rating: Float)

    @Query("UPDATE wallpapers SET skipCount = skipCount + 1, lastSkipped = :timestamp WHERE id = :id")
    suspend fun recordSkip(id: Long, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE wallpapers SET likeCount = likeCount + 1 WHERE id = :id")
    suspend fun recordLike(id: Long)

    @Query("UPDATE wallpapers SET viewCount = viewCount + 1 WHERE id = :id")
    suspend fun recordView(id: Long)

    @Query("UPDATE wallpapers SET dominantColor = :dominantColor, secondaryColor = :secondaryColor, brightness = :brightness, isDark = :isDark WHERE id = :id")
    suspend fun updateWallpaperDna(id: Long, dominantColor: Int?, secondaryColor: Int?, brightness: Float, isDark: Boolean)

    @Query("UPDATE wallpapers SET style = :style, mood = :mood WHERE id = :id")
    suspend fun updateStyleAndMood(id: Long, style: String?, mood: String?)

    @Query("UPDATE wallpapers SET isPrivate = :isPrivate WHERE id = :id")
    suspend fun updatePrivacy(id: Long, isPrivate: Boolean)

    @Query("UPDATE wallpapers SET contentHash = :hash WHERE id = :id")
    suspend fun updateContentHash(id: Long, hash: String)

    @Query("SELECT * FROM wallpapers WHERE contentHash = :hash AND id != :excludeId")
    suspend fun getDuplicatesByHash(hash: String, excludeId: Long): List<WallpaperEntity>

    @Query("SELECT * FROM wallpapers WHERE contentHash = '' OR contentHash IS NULL OR dominantColor IS NULL OR style IS NULL")
    suspend fun getUnanalyzedWallpapers(): List<WallpaperEntity>

    @Query("SELECT * FROM wallpapers WHERE dominantColor IS NULL OR brightness IS NULL")
    suspend fun getWallpapersWithoutDna(): List<WallpaperEntity>

    // Junction queries
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertWallpaperCollectionCrossRef(crossRef: WallpaperCollectionCrossRef)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertWallpaperCollectionCrossRefs(crossRefs: List<WallpaperCollectionCrossRef>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertWallpaperTagCrossRef(crossRef: WallpaperTagCrossRef)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertWallpaperTagCrossRefs(crossRefs: List<WallpaperTagCrossRef>)

    @Query("SELECT * FROM wallpaper_collection_cross_ref")
    suspend fun getAllCollectionCrossRefs(): List<WallpaperCollectionCrossRef>

    @Query("SELECT * FROM wallpaper_tag_cross_ref")
    suspend fun getAllTagCrossRefs(): List<WallpaperTagCrossRef>

    @Query("DELETE FROM wallpaper_collection_cross_ref WHERE wallpaperId = :wallpaperId AND collectionId = :collectionId")
    suspend fun removeWallpaperFromCollection(wallpaperId: Long, collectionId: Long)

    @Query("DELETE FROM wallpaper_tag_cross_ref WHERE wallpaperId = :wallpaperId AND tagId = :tagId")
    suspend fun removeTagFromWallpaper(wallpaperId: Long, tagId: Long)

    @Query("""
        SELECT w.* FROM wallpapers w 
        INNER JOIN wallpaper_collection_cross_ref ref ON w.id = ref.wallpaperId 
        WHERE ref.collectionId = :collectionId
    """)
    fun getWallpapersForCollection(collectionId: Long): Flow<List<WallpaperEntity>>

    @Query("""
        SELECT w.* FROM wallpapers w 
        INNER JOIN wallpaper_collection_cross_ref ref ON w.id = ref.wallpaperId 
        WHERE ref.collectionId = :collectionId
    """)
    suspend fun getWallpapersForCollectionList(collectionId: Long): List<WallpaperEntity>

    @Query("""
        SELECT w.* FROM wallpapers w 
        INNER JOIN wallpaper_tag_cross_ref ref ON w.id = ref.wallpaperId 
        WHERE ref.tagId = :tagId
    """)
    fun getWallpapersForTag(tagId: Long): Flow<List<WallpaperEntity>>

    @Query("SELECT * FROM wallpapers")
    suspend fun getAllWallpapersList(): List<WallpaperEntity>

    @Query("SELECT id FROM wallpapers")
    suspend fun getAllWallpaperIds(): List<Long>

    @Query("SELECT id FROM wallpapers WHERE isFavorite = 1")
    suspend fun getFavoriteWallpaperIds(): List<Long>

    @Query("SELECT w.id FROM wallpapers w INNER JOIN wallpaper_collection_cross_ref ref ON w.id = ref.wallpaperId WHERE ref.collectionId = :collectionId")
    suspend fun getWallpaperIdsForCollection(collectionId: Long): List<Long>
}
