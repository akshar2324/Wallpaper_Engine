package com.akshar.wallpaperengine.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akshar.wallpaperengine.data.remote.RemoteWallpaperCatalogue
import com.akshar.wallpaperengine.data.remote.RemoteWallpaperItem
import com.akshar.wallpaperengine.data.remote.RemoteWallpaperManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ExploreViewModel(
    private val remoteWallpaperManager: RemoteWallpaperManager
) : ViewModel() {

    val categories = RemoteWallpaperCatalogue.getCuratedCategories()

    private val _selectedCategory = MutableStateFlow("ALL")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _wallpapers = MutableStateFlow(RemoteWallpaperCatalogue.getFeaturedWallpapers())
    val wallpapers: StateFlow<List<RemoteWallpaperItem>> = _wallpapers.asStateFlow()

    private val _downloadingIds = MutableStateFlow<Set<String>>(emptySet())
    val downloadingIds: StateFlow<Set<String>> = _downloadingIds.asStateFlow()

    private val _downloadedIds = MutableStateFlow<Set<String>>(emptySet())
    val downloadedIds: StateFlow<Set<String>> = _downloadedIds.asStateFlow()

    fun selectCategory(category: String) {
        _selectedCategory.value = category
        val all = RemoteWallpaperCatalogue.getFeaturedWallpapers()
        _wallpapers.value = if (category == "ALL") {
            all
        } else {
            all.filter { it.category.equals(category, ignoreCase = true) }
        }
    }

    fun downloadAndImport(item: RemoteWallpaperItem, onResult: (Boolean) -> Unit) {
        if (_downloadingIds.value.contains(item.id) || _downloadedIds.value.contains(item.id)) return

        _downloadingIds.value = _downloadingIds.value + item.id

        viewModelScope.launch {
            val result = remoteWallpaperManager.downloadAndImportWallpaper(item)
            _downloadingIds.value = _downloadingIds.value - item.id
            if (result.isSuccess) {
                _downloadedIds.value = _downloadedIds.value + item.id
                onResult(true)
            } else {
                onResult(false)
            }
        }
    }

    class Factory(
        private val remoteWallpaperManager: RemoteWallpaperManager
    ) : androidx.lifecycle.ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ExploreViewModel(remoteWallpaperManager) as T
        }
    }
}
