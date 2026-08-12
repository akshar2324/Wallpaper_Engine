package com.akshar.wallpaperengine.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.akshar.wallpaperengine.data.local.entity.ScheduleEntity
import com.akshar.wallpaperengine.data.local.entity.WallpaperEntity
import com.akshar.wallpaperengine.data.repository.*
import com.akshar.wallpaperengine.wallpaper.AndroidWallpaperService
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class HomeUiState(
    val currentWallpaper: WallpaperEntity? = null,
    val totalWallpapers: Int = 0,
    val totalFavorites: Int = 0,
    val totalSchedules: Int = 0,
    val recentlyUsed: List<WallpaperEntity> = emptyList(),
    val nextUpcomingSchedule: ScheduleEntity? = null
)

class HomeViewModel(
    private val wallpaperRepository: WallpaperRepository,
    private val scheduleRepository: ScheduleRepository,
    private val wallpaperService: AndroidWallpaperService
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = combine(
        wallpaperRepository.currentWallpaper,
        wallpaperRepository.wallpaperCount,
        wallpaperRepository.favoriteWallpapers,
        scheduleRepository.allSchedules,
        wallpaperRepository.recentlyUsedWallpapers
    ) { current, count, favorites, schedules, recentlyUsed ->
        val enabledSchedules = schedules.filter { it.isEnabled }
        val nextSchedule = enabledSchedules.minByOrNull { it.nextExecution ?: Long.MAX_VALUE }

        HomeUiState(
            currentWallpaper = current,
            totalWallpapers = count,
            totalFavorites = favorites.size,
            totalSchedules = enabledSchedules.size,
            recentlyUsed = recentlyUsed,
            nextUpcomingSchedule = nextSchedule
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUiState())

    fun toggleFavorite(wallpaper: WallpaperEntity) {
        viewModelScope.launch {
            wallpaperRepository.toggleFavorite(wallpaper.id, !wallpaper.isFavorite)
        }
    }

    fun applyWallpaperQuick(wallpaper: WallpaperEntity, targetScreen: String) {
        viewModelScope.launch {
            val success = wallpaperService.applyWallpaper(wallpaper, targetScreen)
            if (success) {
                wallpaperRepository.updateLastUsed(wallpaper.id)
            }
        }
    }

    class Factory(
        private val wallpaperRepository: WallpaperRepository,
        private val scheduleRepository: ScheduleRepository,
        private val wallpaperService: AndroidWallpaperService
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HomeViewModel(wallpaperRepository, scheduleRepository, wallpaperService) as T
        }
    }
}
