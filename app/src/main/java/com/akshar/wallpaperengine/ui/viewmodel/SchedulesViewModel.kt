package com.akshar.wallpaperengine.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.akshar.wallpaperengine.data.local.entity.CollectionEntity
import com.akshar.wallpaperengine.data.local.entity.ScheduleEntity
import com.akshar.wallpaperengine.data.repository.CollectionRepository
import com.akshar.wallpaperengine.data.repository.ScheduleRepository
import com.akshar.wallpaperengine.scheduler.WallpaperScheduler
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class SchedulesUiState(
    val schedules: List<ScheduleEntity> = emptyList(),
    val conflictingSchedules: List<ScheduleEntity> = emptyList(),
    val collections: List<CollectionEntity> = emptyList()
)

class SchedulesViewModel(
    private val scheduleRepository: ScheduleRepository,
    private val collectionRepository: CollectionRepository,
    private val wallpaperScheduler: WallpaperScheduler
) : ViewModel() {

    val uiState: StateFlow<SchedulesUiState> = combine(
        scheduleRepository.allSchedules,
        scheduleRepository.conflictingSchedules,
        collectionRepository.allCollections
    ) { schedules, conflicts, collections ->
        SchedulesUiState(
            schedules = schedules,
            conflictingSchedules = conflicts,
            collections = collections
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SchedulesUiState())

    fun toggleSchedule(schedule: ScheduleEntity) {
        viewModelScope.launch {
            val newEnabled = !schedule.isEnabled
            scheduleRepository.toggleScheduleEnabled(schedule.id, newEnabled)
            wallpaperScheduler.scheduleRotation(schedule.copy(isEnabled = newEnabled))
        }
    }

    fun saveSchedule(schedule: ScheduleEntity) {
        viewModelScope.launch {
            val id = scheduleRepository.saveSchedule(schedule)
            val updatedSchedule = if (schedule.id == 0L) schedule.copy(id = id) else schedule
            wallpaperScheduler.scheduleRotation(updatedSchedule)
        }
    }

    fun deleteSchedule(schedule: ScheduleEntity) {
        viewModelScope.launch {
            scheduleRepository.deleteSchedule(schedule)
            wallpaperScheduler.cancelSchedule(schedule.id)
        }
    }

    fun triggerNow(schedule: ScheduleEntity) {
        wallpaperScheduler.triggerNow(schedule.id)
    }

    class Factory(
        private val scheduleRepository: ScheduleRepository,
        private val collectionRepository: CollectionRepository,
        private val wallpaperScheduler: WallpaperScheduler
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SchedulesViewModel(scheduleRepository, collectionRepository, wallpaperScheduler) as T
        }
    }
}
