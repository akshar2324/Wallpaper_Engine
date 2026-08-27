package com.akshar.wallpaperengine.ui.viewmodel

import android.content.Context
import android.net.Uri
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

    private suspend fun importImage(context: Context, uri: Uri) {
        var finalUri = uri.toString()
        try {
            context.contentResolver.takePersistableUriPermission(uri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
        } catch (_: Exception) {
            try {
                context.contentResolver.openInputStream(uri)?.use { input ->
                    val fileName = "imported_${System.currentTimeMillis()}_${uri.lastPathSegment?.replace("/", "_") ?: "image"}.jpg"
                    val file = java.io.File(context.filesDir, fileName)
                    java.io.FileOutputStream(file).use { output -> input.copyTo(output) }
                    finalUri = android.net.Uri.fromFile(file).toString()
                }
            } catch (_: Exception) {
                return
            }
        }

        val title = uri.lastPathSegment?.substringAfterLast('/') ?: "Imported Wallpaper"
        wallpaperRepository.importWallpaper(WallpaperEntity(uri = finalUri, title = title, dateAdded = System.currentTimeMillis()))
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
                    importImage(context, file.uri)
                    count++
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
                        file.name?.endsWith(".webp", true) == true) -> listOf(file)
                else -> emptyList()
            }
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
