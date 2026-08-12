package com.akshar.wallpaperengine.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.akshar.wallpaperengine.data.local.entity.WallpaperHistoryEntity
import com.akshar.wallpaperengine.data.repository.HistoryRepository
import com.akshar.wallpaperengine.data.repository.WallpaperRepository
import com.akshar.wallpaperengine.wallpaper.AndroidWallpaperService
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class HistoryUiState(
    val historyRecords: List<WallpaperHistoryEntity> = emptyList()
)

class HistoryViewModel(
    private val historyRepository: HistoryRepository,
    private val wallpaperRepository: WallpaperRepository,
    private val wallpaperService: AndroidWallpaperService
) : ViewModel() {

    val uiState: StateFlow<HistoryUiState> = historyRepository.allHistory.map { records ->
        HistoryUiState(historyRecords = records)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HistoryUiState())

    fun clearHistory() {
        viewModelScope.launch {
            historyRepository.clearHistory()
        }
    }

    fun deleteHistoryRecord(record: WallpaperHistoryEntity) {
        viewModelScope.launch {
            historyRepository.deleteHistoryRecord(record.id)
        }
    }

    fun reapplyHistoryWallpaper(record: WallpaperHistoryEntity) {
        viewModelScope.launch {
            val wallpaper = wallpaperRepository.getWallpaperById(record.wallpaperId)
            if (wallpaper != null) {
                wallpaperService.applyWallpaper(wallpaper, record.targetScreen)
                wallpaperRepository.updateLastUsed(wallpaper.id)
            }
        }
    }

    class Factory(
        private val historyRepository: HistoryRepository,
        private val wallpaperRepository: WallpaperRepository,
        private val wallpaperService: AndroidWallpaperService
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HistoryViewModel(historyRepository, wallpaperRepository, wallpaperService) as T
        }
    }
}
