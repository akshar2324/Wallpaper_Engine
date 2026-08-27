package com.akshar.wallpaperengine.ui.viewmodel

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.akshar.wallpaperengine.data.local.entity.CollectionEntity
import com.akshar.wallpaperengine.data.local.entity.TagEntity
import com.akshar.wallpaperengine.data.local.entity.WallpaperEntity
import com.akshar.wallpaperengine.data.repository.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class LibraryUiState(
    val wallpapers: List<WallpaperEntity> = emptyList(),
    val collections: List<CollectionEntity> = emptyList(),
    val tags: List<TagEntity> = emptyList(),
    val filterOptions: FilterOptions = FilterOptions(),
    val selectedWallpaperIds: Set<Long> = emptySet(),
    val isMultiSelectMode: Boolean = false,
    val gridDensity: Int = 2
)

class LibraryViewModel(
    private val wallpaperRepository: WallpaperRepository,
    private val collectionRepository: CollectionRepository,
    private val tagRepository: TagRepository
) : ViewModel() {

    private val _filterOptions = MutableStateFlow(FilterOptions())
    private val _selectedIds = MutableStateFlow<Set<Long>>(emptySet())
    private val _gridDensity = MutableStateFlow(2)

    val uiState: StateFlow<LibraryUiState> = combine(
        _filterOptions.flatMapLatest { options -> wallpaperRepository.getFilteredWallpapers(options) },
        collectionRepository.allCollections,
        tagRepository.allTags,
        _filterOptions,
        _selectedIds,
        _gridDensity
    ) { flows ->
        @Suppress("UNCHECKED_CAST")
        val wallpapers = flows[0] as List<WallpaperEntity>
        @Suppress("UNCHECKED_CAST")
        val collections = flows[1] as List<CollectionEntity>
        @Suppress("UNCHECKED_CAST")
        val tags = flows[2] as List<TagEntity>
        val filter = flows[3] as FilterOptions
        @Suppress("UNCHECKED_CAST")
        val selectedIds = flows[4] as Set<Long>
        val gridDensity = flows[5] as Int

        LibraryUiState(
            wallpapers = wallpapers,
            collections = collections,
            tags = tags,
            filterOptions = filter,
            selectedWallpaperIds = selectedIds,
            isMultiSelectMode = selectedIds.isNotEmpty(),
            gridDensity = gridDensity
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), LibraryUiState())

    fun updateSearchQuery(query: String) {
        _filterOptions.update { it.copy(searchQuery = query) }
    }

    fun updateSortOrder(order: SortOrder) {
        _filterOptions.update { it.copy(sortOrder = order) }
    }

    fun updateOrientation(orientation: OrientationFilter) {
        _filterOptions.update { it.copy(orientation = orientation) }
    }

    fun toggleFavoritesOnly() {
        _filterOptions.update { it.copy(favoritesOnly = !it.favoritesOnly) }
    }

    fun updateMinRating(rating: Float) {
        _filterOptions.update { it.copy(minRating = rating) }
    }

    fun toggleDarkOnly() {
        _filterOptions.update { it.copy(darkOnly = !it.darkOnly) }
    }

    fun updateStyle(style: String?) {
        _filterOptions.update { it.copy(style = style) }
    }

    fun updateMood(mood: String?) {
        _filterOptions.update { it.copy(mood = mood) }
    }

    fun clearAllFilters() {
        _filterOptions.value = FilterOptions()
    }

    fun selectCollection(collectionId: Long?) {
        _filterOptions.update { it.copy(collectionId = collectionId) }
    }

    fun selectTag(tagId: Long?) {
        _filterOptions.update { it.copy(tagId = tagId) }
    }

    fun setGridDensity(density: Int) {
        _gridDensity.value = density
    }

    fun toggleSelection(wallpaperId: Long) {
        _selectedIds.update { set ->
            if (set.contains(wallpaperId)) set - wallpaperId else set + wallpaperId
        }
    }

    fun clearSelection() {
        _selectedIds.value = emptySet()
    }

    fun selectAll() {
        _selectedIds.value = uiState.value.wallpapers.map { it.id }.toSet()
    }

    fun toggleFavorite(wallpaper: WallpaperEntity) {
        viewModelScope.launch {
            wallpaperRepository.toggleFavorite(wallpaper.id, !wallpaper.isFavorite)
        }
    }

    fun deleteSelectedWallpapers() {
        viewModelScope.launch {
            val ids = _selectedIds.value.toList()
            if (ids.isNotEmpty()) {
                wallpaperRepository.deleteWallpapersByIds(ids)
                clearSelection()
            }
        }
    }

    fun addSelectedToCollection(collectionId: Long) {
        viewModelScope.launch {
            _selectedIds.value.forEach { id ->
                wallpaperRepository.addWallpaperToCollection(id, collectionId)
            }
            clearSelection()
        }
    }

    fun importImagesFromUris(context: Context, uris: List<Uri>) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            uris.forEach { importImage(context, it) }
        }
    }

    private suspend fun importImage(
        context: Context,
        uri: Uri,
        useTreePermission: Boolean = false
    ): Boolean {
        var finalUri = uri.toString()
        val resolver = context.contentResolver
        if (!useTreePermission) {
            try {
                resolver.takePersistableUriPermission(uri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (_: Exception) {
                try {
                    resolver.openInputStream(uri)?.use { input ->
                        val fileName = "imported_${System.currentTimeMillis()}_${uri.lastPathSegment?.replace("/", "_") ?: "image"}.jpg"
                        val file = java.io.File(context.filesDir, fileName)
                        java.io.FileOutputStream(file).use { output -> input.copyTo(output) }
                        finalUri = android.net.Uri.fromFile(file).toString()
                    } ?: return false
                } catch (_: Exception) {
                    return false
                }
            }
        }

        val (displayName, fileSize) = readImageInfo(context, uri)
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
        val width = bounds.outWidth.takeIf { it > 0 } ?: 1080
        val height = bounds.outHeight.takeIf { it > 0 } ?: 1920
        val mimeType = resolver.getType(uri) ?: mimeTypeFromName(displayName)

        wallpaperRepository.importWallpaper(
            WallpaperEntity(
                uri = finalUri,
                title = displayTitle(displayName),
                width = width,
                height = height,
                aspectRatio = width.toFloat() / height,
                fileSize = fileSize,
                mimeType = mimeType,
                dateAdded = System.currentTimeMillis()
            )
        )
        return true
    }

    fun importFolderTree(
        context: Context,
        treeUri: Uri,
        onProgress: (Int, Int) -> Unit = { _, _ -> },
        onComplete: (Int) -> Unit = {}
    ) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val docFile = androidx.documentfile.provider.DocumentFile.fromTreeUri(context, treeUri)
                val files = docFile?.let(::findImagesRecursively) ?: emptyList()

                var count = 0
                files.forEachIndexed { index, file ->
                    if (importImage(context, file.uri, useTreePermission = true)) count++
                    onProgress(index + 1, files.size)
                }
                onComplete(count)
            } catch (e: Exception) {
                onComplete(0)
            }
        }
    }

    private fun findImagesRecursively(directory: androidx.documentfile.provider.DocumentFile): List<androidx.documentfile.provider.DocumentFile> {
        return directory.listFiles().flatMap { file ->
            when {
                file.isDirectory -> findImagesRecursively(file)
                file.isFile && (file.type?.startsWith("image/") == true ||
                        file.name?.endsWith(".jpg", true) == true ||
                        file.name?.endsWith(".jpeg", true) == true ||
                        file.name?.endsWith(".png", true) == true ||
                        file.name?.endsWith(".webp", true) == true ||
                        file.name?.endsWith(".heic", true) == true ||
                        file.name?.endsWith(".heif", true) == true) -> listOf(file)
                else -> emptyList()
            }
        }
    }

    private fun readImageInfo(context: Context, uri: Uri): Pair<String, Long> {
        var name = uri.lastPathSegment?.substringAfterLast('/') ?: "Imported Wallpaper"
        var size = 0L
        context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE), null, null, null)
            ?.use { cursor ->
                if (cursor.moveToFirst()) {
                    cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME).takeIf { it >= 0 }?.let { name = cursor.getString(it) ?: name }
                    cursor.getColumnIndex(OpenableColumns.SIZE).takeIf { it >= 0 }?.let { size = cursor.getLong(it) }
                }
            }
        return name to size
    }

    private fun mimeTypeFromName(name: String): String = when (name.substringAfterLast('.', "").lowercase()) {
        "png" -> "image/png"
        "webp" -> "image/webp"
        "heic", "heif" -> "image/heic"
        else -> "image/jpeg"
    }

    private fun displayTitle(name: String): String {
        val baseName = name.substringBeforeLast('.').replace('_', ' ').replace('-', ' ').trim()
        return when {
            baseName.isBlank() -> "Imported Wallpaper"
            baseName.all(Char::isDigit) -> "Wallpaper ${baseName.takeLast(4)}"
            else -> baseName
        }
    }

    class Factory(
        private val wallpaperRepository: WallpaperRepository,
        private val collectionRepository: CollectionRepository,
        private val tagRepository: TagRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return LibraryViewModel(wallpaperRepository, collectionRepository, tagRepository) as T
        }
    }
}
