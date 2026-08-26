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
    val similarWallpapers: List<WallpaperEntity> = emptyList(),
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
            loadSimilarWallpapers()
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

    fun loadSimilarWallpapers() {
        viewModelScope.launch {
            val similar = wallpaperRepository.findSimilarWallpapers(wallpaperId)
            _uiState.update { it.copy(similarWallpapers = similar) }
        }
    }

    fun autoTagWallpaper() {
        viewModelScope.launch {
            wallpaperRepository.analyzeAndTagWallpaper(wallpaperId)
            val updated = wallpaperRepository.getWallpaperById(wallpaperId)
            _uiState.update { it.copy(wallpaper = updated) }
            loadSimilarWallpapers()
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

    fun updateRating(rating: Float) {
        val current = _uiState.value.wallpaper ?: return
        viewModelScope.launch {
            wallpaperRepository.updateRating(wallpaperId, rating)
            _uiState.update { it.copy(wallpaper = current.copy(rating = rating)) }
        }
    }

    fun recordLike() {
        val current = _uiState.value.wallpaper ?: return
        viewModelScope.launch {
            wallpaperRepository.recordLike(wallpaperId)
            _uiState.update { it.copy(wallpaper = current.copy(likeCount = current.likeCount + 1)) }
        }
    }

    fun recordSkip() {
        val current = _uiState.value.wallpaper ?: return
        viewModelScope.launch {
            wallpaperRepository.recordSkip(wallpaperId)
            _uiState.update { it.copy(wallpaper = current.copy(skipCount = current.skipCount + 1, lastSkipped = System.currentTimeMillis())) }
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

    fun saveEditedWallpaper(
        context: android.content.Context,
        params: com.akshar.wallpaperengine.data.editor.EditorParameters,
        onComplete: (Boolean) -> Unit
    ) {
        val current = _uiState.value.wallpaper ?: return
        viewModelScope.launch {
            try {
                val uri = android.net.Uri.parse(current.uri)
                val stream = if (current.uri.startsWith("asset:///")) {
                    context.assets.open(current.uri.removePrefix("asset:///"))
                } else if (current.uri.startsWith("file://")) {
                    java.io.File(uri.path ?: "").inputStream()
                } else {
                    context.contentResolver.openInputStream(uri)
                }

                if (stream == null) {
                    onComplete(false)
                    return@launch
                }

                val bitmap = android.graphics.BitmapFactory.decodeStream(stream)
                stream.close()

                if (bitmap == null) {
                    onComplete(false)
                    return@launch
                }

                val edited = com.akshar.wallpaperengine.data.editor.WallpaperEditorProcessor.applyEditsToBitmap(bitmap, params)
                val (savedUri, fileSize) = com.akshar.wallpaperengine.data.editor.WallpaperEditorProcessor.saveEditedWallpaper(context, edited, current.title)

                val editedEntity = WallpaperEntity(
                    uri = savedUri.toString(),
                    title = "${current.title} (Edited)",
                    fileSize = fileSize,
                    mimeType = "image/jpeg",
                    width = edited.width,
                    height = edited.height
                )
                wallpaperRepository.importWallpaper(editedEntity)
                onComplete(true)
            } catch (e: Exception) {
                onComplete(false)
            }
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
