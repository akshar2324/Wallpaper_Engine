package com.akshar.wallpaperengine.data.repository

import android.content.Context
import android.net.Uri
import androidx.sqlite.db.SimpleSQLiteQuery
import com.akshar.wallpaperengine.data.ai.ClassificationResult
import com.akshar.wallpaperengine.data.ai.WallpaperTagClassifier
import com.akshar.wallpaperengine.data.dna.PerceptualHashAnalyzer
import com.akshar.wallpaperengine.data.dna.WallpaperDnaAnalyzer
import com.akshar.wallpaperengine.data.local.dao.CollectionDao
import com.akshar.wallpaperengine.data.local.dao.TagDao
import com.akshar.wallpaperengine.data.local.dao.WallpaperDao
import com.akshar.wallpaperengine.data.local.entity.*
import kotlinx.coroutines.flow.Flow

enum class SortOrder {
    RECENTLY_ADDED,
    RECENTLY_USED,
    NAME_ASC,
    NAME_DESC,
    HIGHEST_RESOLUTION,
    LOWEST_RESOLUTION,
    RATING_DESC,
    MOST_LIKED,
    MOST_VIEWED
}

enum class OrientationFilter {
    ALL,
    PORTRAIT,
    LANDSCAPE,
    SQUARE
}

data class FilterOptions(
    val searchQuery: String = "",
    val favoritesOnly: Boolean = false,
    val collectionId: Long? = null,
    val tagId: Long? = null,
    val orientation: OrientationFilter = OrientationFilter.ALL,
    val sortOrder: SortOrder = SortOrder.RECENTLY_ADDED,
    val minRating: Float = 0f,
    val darkOnly: Boolean = false,
    val brightnessMin: Float? = null,
    val brightnessMax: Float? = null,
    val style: String? = null,
    val mood: String? = null
)

class WallpaperRepository(
    private val wallpaperDao: WallpaperDao,
    private val collectionDao: CollectionDao,
    private val tagDao: TagDao,
    private val context: Context,
    private val dnaAnalyzer: WallpaperDnaAnalyzer = WallpaperDnaAnalyzer(context),
    private val hashAnalyzer: PerceptualHashAnalyzer = PerceptualHashAnalyzer(context),
    private val classifier: WallpaperTagClassifier = WallpaperTagClassifier(context)
) {

    val allWallpapers: Flow<List<WallpaperEntity>> = wallpaperDao.getAllWallpapers()
    val favoriteWallpapers: Flow<List<WallpaperEntity>> = wallpaperDao.getFavoriteWallpapers()
    val recentlyUsedWallpapers: Flow<List<WallpaperEntity>> = wallpaperDao.getRecentlyUsedWallpapers(10)
    val currentWallpaper: Flow<WallpaperEntity?> = wallpaperDao.getCurrentWallpaperFlow()
    val wallpaperCount: Flow<Int> = wallpaperDao.getWallpaperCount()

    fun getFilteredWallpapers(options: FilterOptions): Flow<List<WallpaperEntity>> {
        var query = "SELECT DISTINCT w.* FROM wallpapers w"

        if (options.collectionId != null) {
            query += " INNER JOIN wallpaper_collection_cross_ref c ON w.id = c.wallpaperId AND c.collectionId = ${options.collectionId}"
        }
        if (options.tagId != null) {
            query += " INNER JOIN wallpaper_tag_cross_ref t ON w.id = t.wallpaperId AND t.tagId = ${options.tagId}"
        }

        val conditions = mutableListOf<String>()
        val bindArgs = mutableListOf<Any>()

        if (options.searchQuery.isNotBlank()) {
            val term = "%${options.searchQuery.trim()}%"
            conditions.add("""
                (
                    w.title LIKE ? 
                    OR w.style LIKE ? 
                    OR w.mood LIKE ? 
                    OR EXISTS (
                        SELECT 1 FROM wallpaper_tag_cross_ref wt 
                        INNER JOIN tags tg ON wt.tagId = tg.id 
                        WHERE wt.wallpaperId = w.id AND tg.name LIKE ?
                    )
                )
            """.trimIndent())
            bindArgs.add(term)
            bindArgs.add(term)
            bindArgs.add(term)
            bindArgs.add(term)
        }

        if (options.favoritesOnly) {
            conditions.add("w.isFavorite = 1")
        }

        if (options.minRating > 0f) {
            conditions.add("w.rating >= ?")
            bindArgs.add(options.minRating)
        }

        if (options.darkOnly) {
            conditions.add("w.isDark = 1")
        }

        if (options.brightnessMin != null) {
            conditions.add("w.brightness >= ?")
            bindArgs.add(options.brightnessMin)
        }

        if (options.brightnessMax != null) {
            conditions.add("w.brightness <= ?")
            bindArgs.add(options.brightnessMax)
        }

        if (options.style != null) {
            conditions.add("w.style = ?")
            bindArgs.add(options.style)
        }

        if (options.mood != null) {
            conditions.add("w.mood = ?")
            bindArgs.add(options.mood)
        }

        if (options.orientation != OrientationFilter.ALL) {
            when (options.orientation) {
                OrientationFilter.PORTRAIT -> conditions.add("CAST(w.width AS FLOAT) / w.height < 0.9")
                OrientationFilter.LANDSCAPE -> conditions.add("CAST(w.width AS FLOAT) / w.height > 1.1")
                OrientationFilter.SQUARE -> conditions.add("CAST(w.width AS FLOAT) / w.height >= 0.9 AND CAST(w.width AS FLOAT) / w.height <= 1.1")
                else -> {}
            }
        }

        if (conditions.isNotEmpty()) {
            query += " WHERE " + conditions.joinToString(" AND ")
        }

        val sortClause = when (options.sortOrder) {
            SortOrder.RECENTLY_ADDED -> " ORDER BY w.dateAdded DESC"
            SortOrder.RECENTLY_USED -> " ORDER BY w.lastUsed DESC"
            SortOrder.NAME_ASC -> " ORDER BY LOWER(w.title) ASC"
            SortOrder.NAME_DESC -> " ORDER BY LOWER(w.title) DESC"
            SortOrder.HIGHEST_RESOLUTION -> " ORDER BY (w.width * w.height) DESC"
            SortOrder.LOWEST_RESOLUTION -> " ORDER BY (w.width * w.height) ASC"
            SortOrder.RATING_DESC -> " ORDER BY w.rating DESC, w.dateAdded DESC"
            SortOrder.MOST_LIKED -> " ORDER BY w.likeCount DESC"
            SortOrder.MOST_VIEWED -> " ORDER BY w.viewCount DESC"
        }

        query += sortClause

        return wallpaperDao.getWallpapersFiltered(SimpleSQLiteQuery(query, bindArgs.toTypedArray()))
    }

    suspend fun getWallpaperById(id: Long): WallpaperEntity? = wallpaperDao.getWallpaperById(id)

    suspend fun insertWallpaper(wallpaper: WallpaperEntity): Long = wallpaperDao.insertWallpaper(wallpaper)

    suspend fun importWallpaper(uri: Uri, title: String? = null): Long {
        val resolvedTitle = title ?: uri.lastPathSegment?.substringAfterLast('/') ?: "Imported Wallpaper"
        val dna = dnaAnalyzer.analyzeUri(uri)
        val hash = hashAnalyzer.analyzeUri(uri)
        val classification = classifier.classifyUri(uri)

        val entity = WallpaperEntity(
            uri = uri.toString(),
            title = resolvedTitle,
            dominantColor = dna.dominantColor,
            secondaryColor = dna.secondaryColor,
            brightness = dna.brightness,
            isDark = dna.isDark,
            contentHash = hash,
            style = classification.style,
            mood = classification.mood,
            dateAdded = System.currentTimeMillis()
        )
        val insertedId = wallpaperDao.insertWallpaper(entity)

        // Assign generated AI tags
        for (tagName in classification.tags) {
            val tagId = tagDao.getTagByName(tagName)?.id ?: tagDao.insertTag(TagEntity(name = tagName))
            if (tagId > 0) {
                wallpaperDao.insertWallpaperTagCrossRef(WallpaperTagCrossRef(wallpaperId = insertedId, tagId = tagId))
            }
        }

        return insertedId
    }

    suspend fun importWallpaper(wallpaper: WallpaperEntity): Long {
        val uri = Uri.parse(wallpaper.uri)
        val dna = if (wallpaper.dominantColor == null) {
            dnaAnalyzer.analyzeUri(uri)
        } else {
            null
        }
        val hash = if (wallpaper.contentHash.isBlank()) {
            hashAnalyzer.analyzeUri(uri)
        } else {
            wallpaper.contentHash
        }
        val classification = if (wallpaper.style == null || wallpaper.mood == null) {
            classifier.classifyUri(uri)
        } else {
            null
        }

        val finalWallpaper = wallpaper.copy(
            dominantColor = dna?.dominantColor ?: wallpaper.dominantColor,
            secondaryColor = dna?.secondaryColor ?: wallpaper.secondaryColor,
            brightness = dna?.brightness ?: wallpaper.brightness,
            isDark = dna?.isDark ?: wallpaper.isDark,
            contentHash = hash,
            style = classification?.style ?: wallpaper.style,
            mood = classification?.mood ?: wallpaper.mood
        )
        val insertedId = wallpaperDao.insertWallpaper(finalWallpaper)

        if (classification != null) {
            for (tagName in classification.tags) {
                val tagId = tagDao.getTagByName(tagName)?.id ?: tagDao.insertTag(TagEntity(name = tagName))
                if (tagId > 0) {
                    wallpaperDao.insertWallpaperTagCrossRef(WallpaperTagCrossRef(wallpaperId = insertedId, tagId = tagId))
                }
            }
        }

        return insertedId
    }

    suspend fun updateWallpaper(wallpaper: WallpaperEntity) = wallpaperDao.updateWallpaper(wallpaper)

    suspend fun deleteWallpaper(wallpaper: WallpaperEntity) = wallpaperDao.deleteWallpaper(wallpaper)

    suspend fun deleteWallpapersByIds(ids: List<Long>) = wallpaperDao.deleteWallpapersByIds(ids)

    suspend fun toggleFavorite(id: Long, isFavorite: Boolean) = wallpaperDao.updateFavorite(id, isFavorite)

    suspend fun updateLastUsed(id: Long) = wallpaperDao.updateLastUsed(id, System.currentTimeMillis())

    suspend fun updateRating(id: Long, rating: Float) = wallpaperDao.updateRating(id, rating)

    suspend fun recordSkip(id: Long) = wallpaperDao.recordSkip(id)

    suspend fun recordLike(id: Long) = wallpaperDao.recordLike(id)

    suspend fun recordView(id: Long) = wallpaperDao.recordView(id)

    suspend fun updatePrivacy(id: Long, isPrivate: Boolean) = wallpaperDao.updatePrivacy(id, isPrivate)

    suspend fun updateStyleAndMood(id: Long, style: String?, mood: String?) =
        wallpaperDao.updateStyleAndMood(id, style, mood)

    suspend fun updateWallpaperDna(
        id: Long,
        dominantColor: Int?,
        secondaryColor: Int?,
        brightness: Float,
        isDark: Boolean
    ) = wallpaperDao.updateWallpaperDna(id, dominantColor, secondaryColor, brightness, isDark)

    suspend fun updateContentHash(id: Long, hash: String) =
        wallpaperDao.updateContentHash(id, hash)

    /**
     * Finds exact and near duplicates for a given wallpaper based on perceptual hash Hamming distance.
     */
    suspend fun findSimilarWallpapers(
        wallpaperId: Long,
        maxHammingDistance: Int = PerceptualHashAnalyzer.NEAR_DUPLICATE_THRESHOLD
    ): List<WallpaperEntity> {
        val target = wallpaperDao.getWallpaperById(wallpaperId) ?: return emptyList()
        if (target.contentHash.isBlank()) return emptyList()

        val allWallpapers = wallpaperDao.getAllWallpapersList()
        val targetHash = hashAnalyzer.parseHashHex(target.contentHash)

        return allWallpapers.filter { other ->
            other.id != wallpaperId && other.contentHash.isNotBlank() &&
                    hashAnalyzer.hammingDistance(targetHash, hashAnalyzer.parseHashHex(other.contentHash)) <= maxHammingDistance
        }
    }

    /**
     * Groups all wallpapers by visual duplicate clusters.
     */
    suspend fun findDuplicateClusters(
        maxHammingDistance: Int = PerceptualHashAnalyzer.NEAR_DUPLICATE_THRESHOLD
    ): List<List<WallpaperEntity>> {
        val allWallpapers = wallpaperDao.getAllWallpapersList().filter { it.contentHash.isNotBlank() }
        val visited = mutableSetOf<Long>()
        val clusters = mutableListOf<List<WallpaperEntity>>()

        for (wallpaper in allWallpapers) {
            if (wallpaper.id in visited) continue
            val hash1 = hashAnalyzer.parseHashHex(wallpaper.contentHash)
            val cluster = mutableListOf(wallpaper)
            visited.add(wallpaper.id)

            for (other in allWallpapers) {
                if (other.id in visited) continue
                val hash2 = hashAnalyzer.parseHashHex(other.contentHash)
                if (hashAnalyzer.hammingDistance(hash1, hash2) <= maxHammingDistance) {
                    cluster.add(other)
                    visited.add(other.id)
                }
            }

            if (cluster.size > 1) {
                clusters.add(cluster)
            }
        }
        return clusters
    }

    /**
     * Automatically analyzes DNA, perceptual hash, style, mood, and tags for a single wallpaper.
     */
    suspend fun analyzeAndTagWallpaper(wallpaperId: Long): ClassificationResult? {
        val wallpaper = wallpaperDao.getWallpaperById(wallpaperId) ?: return null
        val uri = Uri.parse(wallpaper.uri)

        val dna = dnaAnalyzer.analyzeUri(uri)
        val hash = hashAnalyzer.analyzeUri(uri)
        val classification = classifier.classifyUri(uri)

        wallpaperDao.updateWallpaperDna(
            id = wallpaperId,
            dominantColor = dna.dominantColor,
            secondaryColor = dna.secondaryColor,
            brightness = dna.brightness,
            isDark = dna.isDark
        )
        wallpaperDao.updateContentHash(wallpaperId, hash)
        wallpaperDao.updateStyleAndMood(wallpaperId, classification.style, classification.mood)

        // Assign tags
        for (tagName in classification.tags) {
            val tagId = tagDao.getTagByName(tagName)?.id ?: tagDao.insertTag(TagEntity(name = tagName))
            if (tagId > 0) {
                wallpaperDao.insertWallpaperTagCrossRef(WallpaperTagCrossRef(wallpaperId = wallpaperId, tagId = tagId))
            }
        }

        return classification
    }

    /**
     * Backfills DNA, Perceptual Hashes, and AI tags for all unanalyzed wallpapers in background.
     */
    suspend fun backfillAiTagsAndHashes() {
        val unanalyzed = wallpaperDao.getUnanalyzedWallpapers()
        for (wallpaper in unanalyzed) {
            analyzeAndTagWallpaper(wallpaper.id)
        }
    }

    suspend fun backfillDnaIfNeeded() {
        val wallpapersWithoutDna = wallpaperDao.getWallpapersWithoutDna()
        for (wallpaper in wallpapersWithoutDna) {
            val dna = dnaAnalyzer.analyzeUri(Uri.parse(wallpaper.uri))
            wallpaperDao.updateWallpaperDna(
                id = wallpaper.id,
                dominantColor = dna.dominantColor,
                secondaryColor = dna.secondaryColor,
                brightness = dna.brightness,
                isDark = dna.isDark
            )
        }
    }

    suspend fun addWallpaperToCollection(wallpaperId: Long, collectionId: Long) {
        wallpaperDao.insertWallpaperCollectionCrossRef(
            WallpaperCollectionCrossRef(wallpaperId = wallpaperId, collectionId = collectionId)
        )
        collectionDao.updateCollectionCount(collectionId)
    }

    suspend fun removeWallpaperFromCollection(wallpaperId: Long, collectionId: Long) {
        wallpaperDao.removeWallpaperFromCollection(wallpaperId, collectionId)
        collectionDao.updateCollectionCount(collectionId)
    }

    suspend fun addTagToWallpaper(wallpaperId: Long, tagId: Long) {
        wallpaperDao.insertWallpaperTagCrossRef(
            WallpaperTagCrossRef(wallpaperId = wallpaperId, tagId = tagId)
        )
    }

    suspend fun removeTagFromWallpaper(wallpaperId: Long, tagId: Long) {
        wallpaperDao.removeTagFromWallpaper(wallpaperId, tagId)
    }

    suspend fun seedSampleWallpapers() = seedInitialDataIfEmpty()

    suspend fun seedInitialDataIfEmpty() {
        val count = wallpaperDao.getAllWallpapersList().size
        if (count > 0) return

        // Create sample wallpapers with rich DNA, perceptual hashes, styles, and moods
        val sampleList = listOf(
            WallpaperEntity(
                uri = "sample_abyss_nebula",
                title = "Abyss Void Core",
                width = 1080,
                height = 2400,
                aspectRatio = 9f / 20f,
                fileSize = 1024000L,
                isFavorite = true,
                isSample = true,
                dominantColor = 0xFF0D0B18.toInt(),
                secondaryColor = 0xFF7C4DFF.toInt(),
                brightness = 0.15f,
                isDark = true,
                contentHash = hashAnalyzer.analyzeUri(Uri.parse("sample_abyss_nebula")),
                rating = 5.0f,
                style = "Abstract",
                mood = "Mysterious"
            ),
            WallpaperEntity(
                uri = "sample_neon_cyberpunk",
                title = "Neon Tokyo Skyline",
                width = 1080,
                height = 2340,
                aspectRatio = 9f / 19.5f,
                fileSize = 1240000L,
                isFavorite = true,
                isSample = true,
                dominantColor = 0xFF0A0E1A.toInt(),
                secondaryColor = 0xFF00E5FF.toInt(),
                brightness = 0.25f,
                isDark = true,
                contentHash = hashAnalyzer.analyzeUri(Uri.parse("sample_neon_cyberpunk")),
                rating = 4.5f,
                style = "Cyberpunk",
                mood = "Energetic"
            ),
            WallpaperEntity(
                uri = "sample_crimson_ronin",
                title = "Crimson Blade Samurai",
                width = 1440,
                height = 3200,
                aspectRatio = 9f / 20f,
                fileSize = 2048000L,
                isFavorite = false,
                isSample = true,
                dominantColor = 0xFF1A0505.toInt(),
                secondaryColor = 0xFFFF1744.toInt(),
                brightness = 0.20f,
                isDark = true,
                contentHash = hashAnalyzer.analyzeUri(Uri.parse("sample_crimson_ronin")),
                rating = 4.0f,
                style = "Anime",
                mood = "Action"
            ),
            WallpaperEntity(
                uri = "sample_moonlight_horizon",
                title = "Moonlight Astral Gate",
                width = 1080,
                height = 1920,
                aspectRatio = 9f / 16f,
                fileSize = 850000L,
                isFavorite = true,
                isSample = true,
                dominantColor = 0xFF080C14.toInt(),
                secondaryColor = 0xFF80D8FF.toInt(),
                brightness = 0.18f,
                isDark = true,
                contentHash = hashAnalyzer.analyzeUri(Uri.parse("sample_moonlight_horizon")),
                rating = 4.5f,
                style = "Nature",
                mood = "Calm"
            ),
            WallpaperEntity(
                uri = "sample_sakura_cyber",
                title = "Sakura Cyberpunk Alley",
                width = 1080,
                height = 2400,
                aspectRatio = 9f / 20f,
                fileSize = 1500000L,
                isFavorite = false,
                isSample = true,
                dominantColor = 0xFF140810.toInt(),
                secondaryColor = 0xFFFF4081.toInt(),
                brightness = 0.22f,
                isDark = true,
                contentHash = hashAnalyzer.analyzeUri(Uri.parse("sample_sakura_cyber")),
                rating = 3.5f,
                style = "Cyberpunk",
                mood = "Serene"
            ),
            WallpaperEntity(
                uri = "sample_aurora_gateway",
                title = "Aurora Prism Horizon",
                width = 1080,
                height = 2340,
                aspectRatio = 9f / 19.5f,
                fileSize = 1100000L,
                isFavorite = false,
                isSample = true,
                dominantColor = 0xFF051410.toInt(),
                secondaryColor = 0xFF00E676.toInt(),
                brightness = 0.20f,
                isDark = true,
                contentHash = hashAnalyzer.analyzeUri(Uri.parse("sample_aurora_gateway")),
                rating = 4.0f,
                style = "Sci-Fi",
                mood = "Ethereal"
            )
        )

        val wallpaperIds = wallpaperDao.insertWallpapers(sampleList)

        // Seed Collections
        val animeCol = collectionDao.insertCollection(CollectionEntity(name = "Anime Cyberpunk", description = "Futuristic anime cities & cybernetics"))
        val darkCol = collectionDao.insertCollection(CollectionEntity(name = "Dark Void", description = "Obsidian surfaces and electric purple glows"))
        val gamingCol = collectionDao.insertCollection(CollectionEntity(name = "Gaming & Tech", description = "High contrast sci-fi & game visuals"))

        if (wallpaperIds.size >= 4) {
            addWallpaperToCollection(wallpaperIds[0], darkCol)
            addWallpaperToCollection(wallpaperIds[1], animeCol)
            addWallpaperToCollection(wallpaperIds[2], gamingCol)
            addWallpaperToCollection(wallpaperIds[3], darkCol)
            addWallpaperToCollection(wallpaperIds[4], animeCol)
        }

        // Seed Tags
        val tag1 = tagDao.insertTag(TagEntity(name = "anime"))
        val tag2 = tagDao.insertTag(TagEntity(name = "cyberpunk"))
        val tag3 = tagDao.insertTag(TagEntity(name = "purple"))
        val tag4 = tagDao.insertTag(TagEntity(name = "dark"))
        val tag5 = tagDao.insertTag(TagEntity(name = "neon"))
        val tag6 = tagDao.insertTag(TagEntity(name = "nature"))
        val tag7 = tagDao.insertTag(TagEntity(name = "oled"))

        if (wallpaperIds.isNotEmpty()) {
            wallpaperIds.forEach { id ->
                addTagToWallpaper(id, tag1)
                addTagToWallpaper(id, tag2)
                addTagToWallpaper(id, tag3)
                addTagToWallpaper(id, tag4)
                addTagToWallpaper(id, tag5)
                addTagToWallpaper(id, tag7)
            }
        }
    }
}
