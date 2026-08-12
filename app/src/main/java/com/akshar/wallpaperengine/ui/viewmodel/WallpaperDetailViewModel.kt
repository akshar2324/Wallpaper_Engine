package com.akshar.wallpaperengine.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.akshar.wallpaperengine.data.local.entity.*
import com.akshar.wallpaperengine.data.repository.*
import com.akshar.wallpaperengine.wallpaper.AndroidWallpaperService
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class WallpaperDetailUiState(
    val wallpaper: WallpaperEntity? = null,
    val collections: List<CollectionEntity> = emptyList(),
    val tags: List<TagEntity> = emptyList(),
    val isApplying: Boolean = false,
    val applySuccess: Boolean? = null
)

class WallpaperDetailViewModel(
    private val wallpaperId: Long,
    private val wallpaperRepository: WallpaperRepository,
    private val collectionRepository: CollectionRepository,
    private val tagRepository: TagRepository,
    private val historyRepository: HistoryRepository,
    private val wallpaperService: AndroidWallpaperService
) : ViewModel() {

    private val _uiState = MutableStateFlow(WallpaperDetailUiState())
    val uiState: StateFlow<WallpaperDetailUiState> = _uiState.asStateFlow()

    init {
        loadWallpaper()
    }

    private fun loadWallpaper() {
        viewModelScope.launch {
            val wallpaper = wallpaperRepository.getWallpaperById(wallpaperId)
            _uiState.update { it.copy(wallpaper = wallpaper) }
        }
        viewModelScope.launch {
            collectionRepository.allCollections.collect { cols ->
                _uiState.update { it.copy(collections = cols) }
            }
        }
        viewModelScope.launch {
            tagRepository.getTagsForWallpaper(wallpaperId).collect { tags ->
                _uiState.update { it.copy(tags = tags) }
            }
        }
    }

    fun toggleFavorite() {
        val current = _uiState.value.wallpaper ?: return
        viewModelScope.launch {
            val updatedFavorite = !current.isFavorite
            wallpaperRepository.toggleFavorite(current.id, updatedFavorite)
            _uiState.update { it.copy(wallpaper = current.copy(isFavorite = updatedFavorite)) }
        }
    }

    fun applyWallpaper(targetScreen: String) {
        val wallpaper = _uiState.value.wallpaper ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isApplying = true, applySuccess = null) }
            val success = wallpaperService.applyWallpaper(wallpaper, targetScreen)
            if (success) {
                wallpaperRepository.updateLastUsed(wallpaper.id)
                historyRepository.recordHistory(
                    WallpaperHistoryEntity(
                        wallpaperId = wallpaper.id,
                        wallpaperTitle = wallpaper.title,
                        wallpaperUri = wallpaper.uri,
                        appliedAt = System.currentTimeMillis(),
                        targetScreen = targetScreen,
                        source = "MANUAL"
                    )
                )
            }
            _uiState.update { it.copy(isApplying = false, applySuccess = success) }
        }
    }

    fun addTag(tagName: String) {
        if (tagName.isBlank()) return
        viewModelScope.launch {
            val tagId = tagRepository.createTag(tagName)
            wallpaperRepository.addTagToWallpaper(wallpaperId, tagId)
        }
    }

    fun removeTag(tag: TagEntity) {
        viewModelScope.launch {
            wallpaperRepository.removeTagFromWallpaper(wallpaperId, tag.id)
        }
    }

    fun addToCollection(collectionId: Long) {
        viewModelScope.launch {
            wallpaperRepository.addWallpaperToCollection(wallpaperId, collectionId)
        }
    }

    fun removeFromCollection(collectionId: Long) {
        viewModelScope.launch {
            wallpaperRepository.removeWallpaperFromCollection(wallpaperId, collectionId)
        }
    }

    fun deleteWallpaper(onDeleted: () -> Unit) {
        val wallpaper = _uiState.value.wallpaper ?: return
        viewModelScope.launch {
            wallpaperRepository.deleteWallpaper(wallpaper)
            onDeleted()
        }
    }

    class Factory(
        private val wallpaperId: Long,
        private val wallpaperRepository: WallpaperRepository,
        private val collectionRepository: CollectionRepository,
        private val tagRepository: TagRepository,
        private val historyRepository: HistoryRepository,
        private val wallpaperService: AndroidWallpaperService
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return WallpaperDetailViewModel(
                wallpaperId,
                wallpaperRepository,
                collectionRepository,
                tagRepository,
                historyRepository,
                wallpaperService
            ) as T
        }
    }
}
