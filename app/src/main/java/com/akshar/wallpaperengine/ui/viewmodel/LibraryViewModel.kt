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
        viewModelScope.launch {
            uris.forEach { uri ->
                var finalUri = uri.toString()
                try {
                    context.contentResolver.takePersistableUriPermission(
                        uri,
                        android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (e: Exception) {
                    // Gracefully handle if persisted permission is unavailable (e.g. some third party providers)
                    // Copy file to internal storage
                    try {
                        val inputStream = context.contentResolver.openInputStream(uri)
                        if (inputStream != null) {
                            val fileName = "imported_${System.currentTimeMillis()}_${uri.lastPathSegment?.replace("/", "_") ?: "image"}.jpg"
                            val file = java.io.File(context.filesDir, fileName)
                            val outputStream = java.io.FileOutputStream(file)
                            inputStream.use { input ->
                                outputStream.use { output ->
                                    input.copyTo(output)
                                }
                            }
                            finalUri = android.net.Uri.fromFile(file).toString()
                        }
                    } catch (ioException: Exception) {
                        ioException.printStackTrace()
                    }
                }
                
                val title = uri.lastPathSegment?.substringAfterLast('/') ?: "Imported Wallpaper"
                val entity = WallpaperEntity(
                    uri = finalUri,
                    title = title,
                    dateAdded = System.currentTimeMillis()
                )
                wallpaperRepository.insertWallpaper(entity)
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
