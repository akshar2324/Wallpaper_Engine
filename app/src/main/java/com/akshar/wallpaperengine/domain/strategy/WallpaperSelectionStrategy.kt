package com.akshar.wallpaperengine.domain.strategy

import androidx.sqlite.db.SimpleSQLiteQuery
import com.akshar.wallpaperengine.data.local.dao.WallpaperDao
import kotlin.random.Random

data class SelectionResult(val wallpaperId: Long, val reason: String)

interface WallpaperSelectionStrategy {
    suspend fun selectWallpaper(
        wallpaperDao: WallpaperDao,
        sourceType: String,
        collectionId: Long?,
        specificWallpaperId: Long?,
        lastSelectedId: Long?
    ): Long? = selectWallpaperWithReason(
        wallpaperDao, sourceType, collectionId, specificWallpaperId, lastSelectedId
    )?.wallpaperId

    suspend fun selectWallpaperWithReason(
        wallpaperDao: WallpaperDao,
        sourceType: String,
        collectionId: Long?,
        specificWallpaperId: Long?,
        lastSelectedId: Long?
    ): SelectionResult?
}

abstract class BaseSqlSelectionStrategy : WallpaperSelectionStrategy {
    protected fun buildBaseQuery(
        sourceType: String,
        collectionId: Long?,
        specificWallpaperId: Long?,
        additionalWhere: String = "",
        orderBy: String = ""
    ): SimpleSQLiteQuery {
        var query = "SELECT w.id FROM wallpapers w"

        if (sourceType.uppercase() == "COLLECTION" && collectionId != null) {
            query += " INNER JOIN wallpaper_collection_cross_ref c ON w.id = c.wallpaperId AND c.collectionId = $collectionId"
        }

        val conditions = mutableListOf<String>()

        when (sourceType.uppercase()) {
            "FAVORITES" -> conditions.add("w.isFavorite = 1")
            "SPECIFIC" -> {
                if (specificWallpaperId != null) {
                    conditions.add("w.id = $specificWallpaperId")
                }
            }
        }

        if (additionalWhere.isNotBlank()) {
            conditions.add(additionalWhere)
        }

        if (conditions.isNotEmpty()) {
            query += " WHERE " + conditions.joinToString(" AND ")
        }

        if (orderBy.isNotBlank()) {
            query += " ORDER BY $orderBy LIMIT 1"
        } else {
            query += " LIMIT 1"
        }

        return SimpleSQLiteQuery(query)
    }
}

open class RandomSelectionStrategy : BaseSqlSelectionStrategy() {
    override suspend fun selectWallpaperWithReason(
        wallpaperDao: WallpaperDao,
        sourceType: String,
        collectionId: Long?,
        specificWallpaperId: Long?,
        lastSelectedId: Long?
    ): SelectionResult? {
        val excludeCondition = if (lastSelectedId != null) "w.id != $lastSelectedId" else ""
        var query = buildBaseQuery(sourceType, collectionId, specificWallpaperId, excludeCondition, "RANDOM()")
        var id = wallpaperDao.getSingleWallpaperId(query)

        // Fallback if exclusion removed the only item
        if (id == null && lastSelectedId != null) {
            query = buildBaseQuery(sourceType, collectionId, specificWallpaperId, "", "RANDOM()")
            id = wallpaperDao.getSingleWallpaperId(query)
        }
        return id?.let { SelectionResult(it, "Random Selection") }
    }
}

class SequentialSelectionStrategy : BaseSqlSelectionStrategy() {
    override suspend fun selectWallpaperWithReason(
        wallpaperDao: WallpaperDao,
        sourceType: String,
        collectionId: Long?,
        specificWallpaperId: Long?,
        lastSelectedId: Long?
    ): SelectionResult? {
        // Try getting the next item with id > lastSelectedId
        if (lastSelectedId != null) {
            val query = buildBaseQuery(sourceType, collectionId, specificWallpaperId, "w.id > $lastSelectedId", "w.id ASC")
            val nextId = wallpaperDao.getSingleWallpaperId(query)
            if (nextId != null) return SelectionResult(nextId, "Sequential Step")
        }

        // Wrap around to the first item (smallest id)
        val wrapQuery = buildBaseQuery(sourceType, collectionId, specificWallpaperId, "", "w.id ASC")
        return wallpaperDao.getSingleWallpaperId(wrapQuery)?.let { SelectionResult(it, "Sequential Step") }
    }
}

class LeastRecentlyUsedSelectionStrategy : BaseSqlSelectionStrategy() {
    override suspend fun selectWallpaperWithReason(
        wallpaperDao: WallpaperDao,
        sourceType: String,
        collectionId: Long?,
        specificWallpaperId: Long?,
        lastSelectedId: Long?
    ): SelectionResult? {
        val excludeCondition = if (lastSelectedId != null) "w.id != $lastSelectedId" else ""
        var query = buildBaseQuery(sourceType, collectionId, specificWallpaperId, excludeCondition, "IFNULL(w.lastUsed, 0) ASC")
        var id = wallpaperDao.getSingleWallpaperId(query)

        // Fallback
        if (id == null && lastSelectedId != null) {
            query = buildBaseQuery(sourceType, collectionId, specificWallpaperId, "", "IFNULL(w.lastUsed, 0) ASC")
            id = wallpaperDao.getSingleWallpaperId(query)
        }
        return id?.let { SelectionResult(it, "Least Recently Used") }
    }
}

class FavoritesSelectionStrategy : RandomSelectionStrategy()

class SmartShuffleSelectionStrategy : BaseSqlSelectionStrategy() {
    override suspend fun selectWallpaperWithReason(
        wallpaperDao: WallpaperDao,
        sourceType: String,
        collectionId: Long?,
        specificWallpaperId: Long?,
        lastSelectedId: Long?
    ): SelectionResult? {
        val excludeCondition = if (lastSelectedId != null) "w.id != $lastSelectedId" else ""
        val orderBy = "(CASE WHEN w.isFavorite = 1 THEN 30.0 ELSE 0.0 END) + (w.rating * 10.0) + (w.likeCount * 5.0) - (w.skipCount * 8.0) - (CASE WHEN w.lastSkipped IS NOT NULL AND (strftime('%s', 'now') * 1000 - w.lastSkipped) < 86400000 THEN 25.0 ELSE 0.0 END) - (CASE WHEN w.lastUsed IS NOT NULL THEN (10.0 / (1.0 + (strftime('%s', 'now') * 1000 - w.lastUsed) / 86400000.0)) ELSE 0.0 END) + (ABS(RANDOM()) % 15) DESC"
        var query = buildBaseQuery(sourceType, collectionId, specificWallpaperId, excludeCondition, orderBy)
        var id = wallpaperDao.getSingleWallpaperId(query)
        if (id == null && lastSelectedId != null) {
            query = buildBaseQuery(sourceType, collectionId, specificWallpaperId, "", orderBy)
            id = wallpaperDao.getSingleWallpaperId(query)
        }
        return id?.let { SelectionResult(it, "Smart Shuffle (Adaptive Score)") }
    }
}

class WeightedFavoritesSelectionStrategy : BaseSqlSelectionStrategy() {
    override suspend fun selectWallpaperWithReason(
        wallpaperDao: WallpaperDao,
        sourceType: String,
        collectionId: Long?,
        specificWallpaperId: Long?,
        lastSelectedId: Long?
    ): SelectionResult? {
        val excludeCondition = if (lastSelectedId != null) "w.id != $lastSelectedId" else ""
        
        val isFavoritePool = Math.random() < 0.75
        val baseCondition = "(w.rating >= 4.0 OR w.isFavorite = 1)"
        val poolCondition = if (isFavoritePool) {
            if (excludeCondition.isNotEmpty()) "$excludeCondition AND $baseCondition" else baseCondition
        } else {
            if (excludeCondition.isNotEmpty()) "$excludeCondition AND NOT $baseCondition" else "NOT $baseCondition"
        }
        
        var query = buildBaseQuery(sourceType, collectionId, specificWallpaperId, poolCondition, "RANDOM()")
        var id = wallpaperDao.getSingleWallpaperId(query)
        
        val poolType = if (isFavoritePool) "75% pool" else "25% discovery"

        if (id == null) {
            query = buildBaseQuery(sourceType, collectionId, specificWallpaperId, excludeCondition, "RANDOM()")
            id = wallpaperDao.getSingleWallpaperId(query)
            if (id == null && lastSelectedId != null) {
                query = buildBaseQuery(sourceType, collectionId, specificWallpaperId, "", "RANDOM()")
                id = wallpaperDao.getSingleWallpaperId(query)
            }
        }
        return id?.let { SelectionResult(it, "Weighted Favorite ($poolType)") }
    }
}

class NeverRepeatSelectionStrategy : BaseSqlSelectionStrategy() {
    override suspend fun selectWallpaperWithReason(
        wallpaperDao: WallpaperDao,
        sourceType: String,
        collectionId: Long?,
        specificWallpaperId: Long?,
        lastSelectedId: Long?
    ): SelectionResult? {
        val excludeCondition = if (lastSelectedId != null) "w.id != $lastSelectedId" else ""
        val unusedCondition = if (excludeCondition.isNotEmpty()) "$excludeCondition AND w.lastUsed IS NULL" else "w.lastUsed IS NULL"
        
        var query = buildBaseQuery(sourceType, collectionId, specificWallpaperId, unusedCondition, "w.dateAdded DESC")
        var id = wallpaperDao.getSingleWallpaperId(query)

        if (id != null) {
            return SelectionResult(id, "Never Repeat (Unused/Oldest)")
        }

        query = buildBaseQuery(sourceType, collectionId, specificWallpaperId, excludeCondition, "w.lastUsed ASC")
        id = wallpaperDao.getSingleWallpaperId(query)
        
        if (id == null && lastSelectedId != null) {
            query = buildBaseQuery(sourceType, collectionId, specificWallpaperId, "", "w.lastUsed ASC")
            id = wallpaperDao.getSingleWallpaperId(query)
        }
        return id?.let { SelectionResult(it, "Never Repeat (Unused/Oldest)") }
    }
}

open class VarietySelectionStrategy : BaseSqlSelectionStrategy() {
    override suspend fun selectWallpaperWithReason(
        wallpaperDao: WallpaperDao,
        sourceType: String,
        collectionId: Long?,
        specificWallpaperId: Long?,
        lastSelectedId: Long?
    ): SelectionResult? {
        val excludeCondition = if (lastSelectedId != null) "w.id != $lastSelectedId" else ""
        
        var varietyCondition = excludeCondition
        if (lastSelectedId != null) {
            val lastWallpaper = wallpaperDao.getWallpaperById(lastSelectedId)
            if (lastWallpaper != null) {
                val conditions = mutableListOf<String>()
                if (lastWallpaper.style != null) conditions.add("w.style != '${lastWallpaper.style}'")
                if (lastWallpaper.mood != null) conditions.add("w.mood != '${lastWallpaper.mood}'")
                if (lastWallpaper.dominantColor != null) conditions.add("w.dominantColor != ${lastWallpaper.dominantColor}")
                
                if (conditions.isNotEmpty()) {
                    val subCondition = conditions.joinToString(" OR ")
                    varietyCondition = if (varietyCondition.isNotEmpty()) "$varietyCondition AND ($subCondition)" else subCondition
                }
            }
        }
        
        var query = buildBaseQuery(sourceType, collectionId, specificWallpaperId, varietyCondition, "RANDOM()")
        var id = wallpaperDao.getSingleWallpaperId(query)
        
        if (id == null) {
            query = buildBaseQuery(sourceType, collectionId, specificWallpaperId, excludeCondition, "RANDOM()")
            id = wallpaperDao.getSingleWallpaperId(query)
            if (id == null && lastSelectedId != null) {
                query = buildBaseQuery(sourceType, collectionId, specificWallpaperId, "", "RANDOM()")
                id = wallpaperDao.getSingleWallpaperId(query)
            }
        }
        return id?.let { SelectionResult(it, "Variety Mode") }
    }
}

class TimeOfDaySelectionStrategy(
    private val calendarProvider: () -> java.util.Calendar = { java.util.Calendar.getInstance() }
) : BaseSqlSelectionStrategy() {
    override suspend fun selectWallpaperWithReason(
        wallpaperDao: WallpaperDao,
        sourceType: String,
        collectionId: Long?,
        specificWallpaperId: Long?,
        lastSelectedId: Long?
    ): SelectionResult? {
        val calendar = calendarProvider()
        val solarTimes = try {
            com.akshar.wallpaperengine.domain.solar.SolarCalculator.calculateSolarTimes(calendar)
        } catch (e: Exception) {
            null
        }
        val profile = com.akshar.wallpaperengine.domain.model.TimeOfDayProfile.fromCurrentTime(calendar, solarTimes)

        val conditions = mutableListOf<String>()
        if (lastSelectedId != null) {
            conditions.add("w.id != $lastSelectedId")
        }

        if (profile.darkOnly) {
            conditions.add("w.isDark = 1")
        }

        conditions.add("w.brightness >= ${profile.minBrightness} AND w.brightness <= ${profile.maxBrightness}")

        val stylesFormatted = profile.preferredStyles.joinToString("','")
        val moodsFormatted = profile.preferredMoods.joinToString("','")
        conditions.add("(w.style IN ('$stylesFormatted') OR w.mood IN ('$moodsFormatted'))")

        val timeWhere = conditions.joinToString(" AND ")
        var query = buildBaseQuery(sourceType, collectionId, specificWallpaperId, timeWhere, "RANDOM()")
        var id = wallpaperDao.getSingleWallpaperId(query)

        if (id != null) {
            return SelectionResult(id, "Time of Day (${profile.displayName})")
        }

        // Secondary fallback: relaxing style/mood, keeping brightness & dark profile
        val relaxedConditions = mutableListOf<String>()
        if (lastSelectedId != null) relaxedConditions.add("w.id != $lastSelectedId")
        if (profile.darkOnly) relaxedConditions.add("w.isDark = 1")
        relaxedConditions.add("w.brightness >= ${profile.minBrightness} AND w.brightness <= ${profile.maxBrightness}")

        query = buildBaseQuery(sourceType, collectionId, specificWallpaperId, relaxedConditions.joinToString(" AND "), "RANDOM()")
        id = wallpaperDao.getSingleWallpaperId(query)

        if (id != null) {
            return SelectionResult(id, "Time of Day (${profile.displayName})")
        }

        // Ultimate fallback: Smart Shuffle
        val fallback = SmartShuffleSelectionStrategy().selectWallpaperWithReason(
            wallpaperDao, sourceType, collectionId, specificWallpaperId, lastSelectedId
        )
        return fallback?.copy(reason = "Time of Day Fallback (${profile.displayName})")
    }
}

object SelectionStrategyFactory {
    fun getStrategy(modeName: String): WallpaperSelectionStrategy {
        return when (modeName.uppercase()) {
            "TIME_OF_DAY", "TIME", "SOLAR", "ADAPTIVE_TIME" -> TimeOfDaySelectionStrategy()
            "SMART_SHUFFLE", "SMART" -> SmartShuffleSelectionStrategy()
            "WEIGHTED_FAVORITES", "FAVORITES_WEIGHTED" -> WeightedFavoritesSelectionStrategy()
            "NEVER_REPEAT" -> NeverRepeatSelectionStrategy()
            "VARIETY" -> VarietySelectionStrategy()
            "SEQUENTIAL" -> SequentialSelectionStrategy()
            "LRU", "LEAST_RECENTLY_USED" -> LeastRecentlyUsedSelectionStrategy()
            else -> RandomSelectionStrategy() // Default to Random
        }
    }
}
