package com.akshar.wallpaperengine.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.akshar.wallpaperengine.data.local.entity.CollectionEntity
import com.akshar.wallpaperengine.data.local.entity.WallpaperEntity
import com.akshar.wallpaperengine.data.repository.CollectionRepository
import com.akshar.wallpaperengine.data.repository.WallpaperRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class CollectionsUiState(
    val collections: List<CollectionEntity> = emptyList(),
    val selectedCollection: CollectionEntity? = null,
    val collectionWallpapers: List<WallpaperEntity> = emptyList()
)

class CollectionsViewModel(
    private val collectionRepository: CollectionRepository,
    private val wallpaperRepository: WallpaperRepository
) : ViewModel() {

    private val _selectedCollection = MutableStateFlow<CollectionEntity?>(null)

    val uiState: StateFlow<CollectionsUiState> = combine(
        collectionRepository.allCollections,
        _selectedCollection,
        _selectedCollection.flatMapLatest { col ->
            if (col != null) wallpaperRepository.getFilteredWallpapers(
                com.akshar.wallpaperengine.data.repository.FilterOptions(collectionId = col.id)
            ) else flowOf(emptyList())
        }
    ) { collections, selected, wallpapers ->
        CollectionsUiState(
            collections = collections,
            selectedCollection = selected,
            collectionWallpapers = wallpapers
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CollectionsUiState())

    fun selectCollection(collection: CollectionEntity?) {
        _selectedCollection.value = collection
    }

    fun createCollection(name: String, description: String = "") {
        if (name.isBlank()) return
        viewModelScope.launch {
            collectionRepository.createCollection(name, description)
        }
    }

    fun updateCollection(collection: CollectionEntity) {
        viewModelScope.launch {
            collectionRepository.updateCollection(collection)
        }
    }

    fun deleteCollection(collection: CollectionEntity) {
        viewModelScope.launch {
            collectionRepository.deleteCollection(collection)
            if (_selectedCollection.value?.id == collection.id) {
                _selectedCollection.value = null
            }
        }
    }

    class Factory(
        private val collectionRepository: CollectionRepository,
        private val wallpaperRepository: WallpaperRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CollectionsViewModel(collectionRepository, wallpaperRepository) as T
        }
    }
}
