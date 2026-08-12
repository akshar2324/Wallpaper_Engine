package com.akshar.wallpaperengine.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.akshar.wallpaperengine.data.preferences.UserPreferences
import com.akshar.wallpaperengine.data.preferences.UserPreferencesRepository
import com.akshar.wallpaperengine.data.repository.BackupService
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val backupService: BackupService
) : ViewModel() {

    val preferences: StateFlow<UserPreferences> = userPreferencesRepository.userPreferencesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserPreferences())

    fun selectTheme(themeId: String) {
        viewModelScope.launch {
            userPreferencesRepository.updateThemeId(themeId)
        }
    }

    fun toggleShader(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.updateShaderEnabled(enabled)
        }
    }

    fun selectShaderStyle(style: String) {
        viewModelScope.launch {
            userPreferencesRepository.updateShaderStyle(style)
        }
    }

    fun selectShaderIntensity(intensity: String) {
        viewModelScope.launch {
            userPreferencesRepository.updateShaderIntensity(intensity)
        }
    }

    fun toggleReduceMotion(reduce: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.updateReduceMotion(reduce)
        }
    }

    fun selectPerformanceMode(mode: String) {
        viewModelScope.launch {
            userPreferencesRepository.updatePerformanceMode(mode)
            when (mode) {
                "PERFORMANCE" -> {
                    userPreferencesRepository.updateShaderEnabled(false)
                    userPreferencesRepository.updateReduceMotion(true)
                }
                "BALANCED" -> {
                    userPreferencesRepository.updateShaderEnabled(true)
                    userPreferencesRepository.updateShaderIntensity("MEDIUM")
                    userPreferencesRepository.updateReduceMotion(false)
                }
                "QUALITY" -> {
                    userPreferencesRepository.updateShaderEnabled(true)
                    userPreferencesRepository.updateShaderIntensity("HIGH")
                    userPreferencesRepository.updateReduceMotion(false)
                }
            }
        }
    }

    fun exportBackup(context: Context, uri: Uri, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = backupService.exportMetadata(uri)
            onResult(success)
        }
    }

    fun importBackup(context: Context, uri: Uri, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = backupService.importMetadata(uri)
            onResult(success)
        }
    }

    class Factory(
        private val userPreferencesRepository: UserPreferencesRepository,
        private val backupService: BackupService
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SettingsViewModel(userPreferencesRepository, backupService) as T
        }
    }
}
