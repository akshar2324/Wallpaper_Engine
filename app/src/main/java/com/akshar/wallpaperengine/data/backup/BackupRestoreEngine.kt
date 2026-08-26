package com.akshar.wallpaperengine.data.backup

import com.akshar.wallpaperengine.data.local.dao.*
import com.akshar.wallpaperengine.data.local.entity.*
import org.json.JSONArray
import org.json.JSONObject

data class BackupSummary(
    val wallpaperCount: Int,
    val collectionCount: Int,
    val tagCount: Int,
    val scheduleCount: Int,
    val historyCount: Int,
    val exportTimestamp: Long
)

class BackupRestoreEngine(
    private val wallpaperDao: WallpaperDao,
    private val collectionDao: CollectionDao,
    private val tagDao: TagDao,
    private val scheduleDao: ScheduleDao,
    private val historyDao: HistoryDao
) {

    suspend fun exportBackupJson(): String {
        val root = JSONObject()
        root.put("version", 1)
        val now = System.currentTimeMillis()
        root.put("exportTimestamp", now)

        // 1. Wallpapers
        val wallpapers = wallpaperDao.getAllWallpapersList()
        val wallpapersArray = JSONArray()
        for (w in wallpapers) {
            val obj = JSONObject().apply {
                put("id", w.id)
                put("uri", w.uri)
                put("title", w.title)
                put("width", w.width)
                put("height", w.height)
                put("aspectRatio", w.aspectRatio.toDouble())
                put("fileSize", w.fileSize)
                put("mimeType", w.mimeType)
                put("dateAdded", w.dateAdded)
                put("lastUsed", w.lastUsed ?: JSONObject.NULL)
                put("isFavorite", w.isFavorite)
                put("contentHash", w.contentHash ?: JSONObject.NULL)
                put("scaleType", w.scaleType)
                put("horizontalOffset", w.horizontalOffset.toDouble())
                put("verticalOffset", w.verticalOffset.toDouble())
                put("isSample", w.isSample)
                put("rating", w.rating.toDouble())
                put("dominantColor", w.dominantColor ?: JSONObject.NULL)
                put("secondaryColor", w.secondaryColor ?: JSONObject.NULL)
                put("brightness", w.brightness.toDouble())
                put("isDark", w.isDark)
                put("skipCount", w.skipCount)
                put("likeCount", w.likeCount)
                put("viewCount", w.viewCount)
                put("lastSkipped", w.lastSkipped ?: JSONObject.NULL)
                put("isPrivate", w.isPrivate)
                put("style", w.style ?: JSONObject.NULL)
                put("mood", w.mood ?: JSONObject.NULL)
            }
            wallpapersArray.put(obj)
        }
        root.put("wallpapers", wallpapersArray)

        // 2. Collections
        val collections = collectionDao.getAllCollectionsList()
        val collectionsArray = JSONArray()
        for (c in collections) {
            val obj = JSONObject().apply {
                put("id", c.id)
                put("name", c.name)
                put("description", c.description ?: JSONObject.NULL)
                put("coverUri", c.coverUri ?: JSONObject.NULL)
                put("wallpaperCount", c.wallpaperCount)
                put("createdAt", c.createdAt)
            }
            collectionsArray.put(obj)
        }
        root.put("collections", collectionsArray)

        // 3. Tags
        val tags = tagDao.getAllTagsList()
        val tagsArray = JSONArray()
        for (t in tags) {
            val obj = JSONObject().apply {
                put("id", t.id)
                put("name", t.name)
            }
            tagsArray.put(obj)
        }
        root.put("tags", tagsArray)

        // 4. CrossRefs
        val colRefs = wallpaperDao.getAllCollectionCrossRefs()
        val colRefsArray = JSONArray()
        for (cr in colRefs) {
            val obj = JSONObject().apply {
                put("wallpaperId", cr.wallpaperId)
                put("collectionId", cr.collectionId)
            }
            colRefsArray.put(obj)
        }
        root.put("collectionCrossRefs", colRefsArray)

        val tagRefs = wallpaperDao.getAllTagCrossRefs()
        val tagRefsArray = JSONArray()
        for (tr in tagRefs) {
            val obj = JSONObject().apply {
                put("wallpaperId", tr.wallpaperId)
                put("tagId", tr.tagId)
            }
            tagRefsArray.put(obj)
        }
        root.put("tagCrossRefs", tagRefsArray)

        // 5. Schedules
        val schedules = scheduleDao.getAllSchedulesList()
        val schedulesArray = JSONArray()
        for (s in schedules) {
            val obj = JSONObject().apply {
                put("id", s.id)
                put("name", s.name)
                put("timeHour", s.timeHour)
                put("timeMinute", s.timeMinute)
                put("activeDaysCsv", s.activeDaysCsv)
                put("isEnabled", s.isEnabled)
                put("sourceType", s.sourceType)
                put("sourceCollectionId", s.sourceCollectionId ?: JSONObject.NULL)
                put("specificWallpaperId", s.specificWallpaperId ?: JSONObject.NULL)
                put("selectionMode", s.selectionMode)
                put("targetScreen", s.targetScreen)
                put("triggerType", s.triggerType)
                put("priority", s.priority)
            }
            schedulesArray.put(obj)
        }
        root.put("schedules", schedulesArray)

        // 6. History
        val history = historyDao.getAllHistoryList()
        val historyArray = JSONArray()
        for (h in history) {
            val obj = JSONObject().apply {
                put("id", h.id)
                put("wallpaperId", h.wallpaperId)
                put("wallpaperTitle", h.wallpaperTitle)
                put("wallpaperUri", h.wallpaperUri)
                put("appliedAt", h.appliedAt)
                put("targetScreen", h.targetScreen)
                put("source", h.source)
                put("scheduleId", h.scheduleId ?: JSONObject.NULL)
                put("selectionReason", h.selectionReason ?: JSONObject.NULL)
            }
            historyArray.put(obj)
        }
        root.put("history", historyArray)

        return root.toString(2)
    }

    suspend fun restoreBackupJson(jsonString: String): BackupSummary {
        val root = JSONObject(jsonString)
        val exportTimestamp = root.optLong("exportTimestamp", System.currentTimeMillis())

        // 1. Restore Collections
        val collectionsArray = root.optJSONArray("collections") ?: JSONArray()
        val collectionsList = mutableListOf<CollectionEntity>()
        for (i in 0 until collectionsArray.length()) {
            val obj = collectionsArray.getJSONObject(i)
            collectionsList.add(
                CollectionEntity(
                    id = obj.optLong("id", 0L),
                    name = obj.getString("name"),
                    description = if (obj.isNull("description")) "" else obj.optString("description", ""),
                    coverUri = if (obj.isNull("coverUri")) null else obj.getString("coverUri"),
                    wallpaperCount = obj.optInt("wallpaperCount", 0),
                    createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                )
            )
        }
        if (collectionsList.isNotEmpty()) {
            collectionDao.insertCollections(collectionsList)
        }

        // 2. Restore Tags
        val tagsArray = root.optJSONArray("tags") ?: JSONArray()
        val tagsList = mutableListOf<TagEntity>()
        for (i in 0 until tagsArray.length()) {
            val obj = tagsArray.getJSONObject(i)
            tagsList.add(
                TagEntity(
                    id = obj.optLong("id", 0L),
                    name = obj.getString("name")
                )
            )
        }
        if (tagsList.isNotEmpty()) {
            tagDao.insertTags(tagsList)
        }

        // 3. Restore Wallpapers
        val wallpapersArray = root.optJSONArray("wallpapers") ?: JSONArray()
        val wallpapersList = mutableListOf<WallpaperEntity>()
        for (i in 0 until wallpapersArray.length()) {
            val obj = wallpapersArray.getJSONObject(i)
            val wId = obj.optLong("id", 0L)
            val wUri = obj.getString("uri")
            val wTitle = obj.getString("title")
            val wDesc = if (obj.isNull("contentHash")) "" else obj.optString("contentHash", "")
            wallpapersList.add(
                WallpaperEntity(
                    id = wId,
                    uri = wUri,
                    title = wTitle,
                    width = obj.optInt("width", 1080),
                    height = obj.optInt("height", 1920),
                    aspectRatio = obj.optDouble("aspectRatio", 0.5625).toFloat(),
                    fileSize = obj.optLong("fileSize", 0L),
                    mimeType = obj.optString("mimeType", "image/jpeg"),
                    dateAdded = obj.optLong("dateAdded", System.currentTimeMillis()),
                    lastUsed = if (obj.isNull("lastUsed")) null else obj.getLong("lastUsed"),
                    isFavorite = obj.optBoolean("isFavorite", false),
                    contentHash = wDesc,
                    scaleType = obj.optString("scaleType", "CENTER_CROP"),
                    horizontalOffset = obj.optDouble("horizontalOffset", 0.5).toFloat(),
                    verticalOffset = obj.optDouble("verticalOffset", 0.5).toFloat(),
                    isSample = obj.optBoolean("isSample", false),
                    rating = obj.optDouble("rating", 0.0).toFloat(),
                    dominantColor = if (obj.isNull("dominantColor")) null else obj.getInt("dominantColor"),
                    secondaryColor = if (obj.isNull("secondaryColor")) null else obj.getInt("secondaryColor"),
                    brightness = obj.optDouble("brightness", 0.5).toFloat(),
                    isDark = obj.optBoolean("isDark", false),
                    skipCount = obj.optInt("skipCount", 0),
                    likeCount = obj.optInt("likeCount", 0),
                    viewCount = obj.optInt("viewCount", 0),
                    lastSkipped = if (obj.isNull("lastSkipped")) null else obj.getLong("lastSkipped"),
                    isPrivate = obj.optBoolean("isPrivate", false),
                    style = if (obj.isNull("style")) null else obj.getString("style"),
                    mood = if (obj.isNull("mood")) null else obj.getString("mood")
                )
            )
        }
        if (wallpapersList.isNotEmpty()) {
            wallpaperDao.insertWallpapers(wallpapersList)
        }

        // 4. Restore CrossRefs
        val colRefsArray = root.optJSONArray("collectionCrossRefs") ?: JSONArray()
        val colRefsList = mutableListOf<WallpaperCollectionCrossRef>()
        for (i in 0 until colRefsArray.length()) {
            val obj = colRefsArray.getJSONObject(i)
            colRefsList.add(
                WallpaperCollectionCrossRef(
                    wallpaperId = obj.getLong("wallpaperId"),
                    collectionId = obj.getLong("collectionId")
                )
            )
        }
        if (colRefsList.isNotEmpty()) {
            wallpaperDao.insertWallpaperCollectionCrossRefs(colRefsList)
        }

        val tagRefsArray = root.optJSONArray("tagCrossRefs") ?: JSONArray()
        val tagRefsList = mutableListOf<WallpaperTagCrossRef>()
        for (i in 0 until tagRefsArray.length()) {
            val obj = tagRefsArray.getJSONObject(i)
            tagRefsList.add(
                WallpaperTagCrossRef(
                    wallpaperId = obj.getLong("wallpaperId"),
                    tagId = obj.getLong("tagId")
                )
            )
        }
        if (tagRefsList.isNotEmpty()) {
            wallpaperDao.insertWallpaperTagCrossRefs(tagRefsList)
        }

        // 5. Restore Schedules
        val schedulesArray = root.optJSONArray("schedules") ?: JSONArray()
        val schedulesList = mutableListOf<ScheduleEntity>()
        for (i in 0 until schedulesArray.length()) {
            val obj = schedulesArray.getJSONObject(i)
            schedulesList.add(
                ScheduleEntity(
                    id = obj.optLong("id", 0L),
                    name = obj.getString("name"),
                    timeHour = obj.optInt("timeHour", 8),
                    timeMinute = obj.optInt("timeMinute", 0),
                    activeDaysCsv = obj.optString("activeDaysCsv", "MON,TUE,WED,THU,FRI,SAT,SUN"),
                    isEnabled = obj.optBoolean("isEnabled", true),
                    sourceType = obj.optString("sourceType", "FAVORITES"),
                    sourceCollectionId = if (obj.isNull("sourceCollectionId")) null else obj.getLong("sourceCollectionId"),
                    specificWallpaperId = if (obj.isNull("specificWallpaperId")) null else obj.getLong("specificWallpaperId"),
                    selectionMode = obj.optString("selectionMode", "RANDOM"),
                    targetScreen = obj.optString("targetScreen", "HOME_AND_LOCK"),
                    triggerType = obj.optString("triggerType", "TIME"),
                    priority = obj.optInt("priority", 0)
                )
            )
        }
        if (schedulesList.isNotEmpty()) {
            scheduleDao.insertSchedules(schedulesList)
        }

        // 6. Restore History
        val historyArray = root.optJSONArray("history") ?: JSONArray()
        val historyList = mutableListOf<WallpaperHistoryEntity>()
        for (i in 0 until historyArray.length()) {
            val obj = historyArray.getJSONObject(i)
            historyList.add(
                WallpaperHistoryEntity(
                    id = obj.optLong("id", 0L),
                    wallpaperId = obj.getLong("wallpaperId"),
                    wallpaperTitle = obj.getString("wallpaperTitle"),
                    wallpaperUri = obj.getString("wallpaperUri"),
                    appliedAt = obj.getLong("appliedAt"),
                    targetScreen = obj.optString("targetScreen", "HOME_AND_LOCK"),
                    source = obj.optString("source", "MANUAL"),
                    scheduleId = if (obj.isNull("scheduleId")) null else obj.getLong("scheduleId"),
                    selectionReason = if (obj.isNull("selectionReason")) null else obj.getString("selectionReason")
                )
            )
        }
        if (historyList.isNotEmpty()) {
            historyDao.insertHistoryRecords(historyList)
        }

        return BackupSummary(
            wallpaperCount = wallpapersList.size,
            collectionCount = collectionsList.size,
            tagCount = tagsList.size,
            scheduleCount = schedulesList.size,
            historyCount = historyList.size,
            exportTimestamp = exportTimestamp
        )
    }
}
