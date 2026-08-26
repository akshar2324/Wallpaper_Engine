package com.akshar.wallpaperengine.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.akshar.wallpaperengine.data.analytics.AnalyticsManager
import com.akshar.wallpaperengine.data.analytics.EngineAnalytics
import com.akshar.wallpaperengine.data.backup.BackupRestoreEngine
import com.akshar.wallpaperengine.data.backup.BackupSummary
import com.akshar.wallpaperengine.data.maintenance.LibraryHealthManager
import com.akshar.wallpaperengine.data.maintenance.LibraryHealthReport
import com.akshar.wallpaperengine.data.preferences.UserPreferences
import com.akshar.wallpaperengine.data.preferences.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val healthManager: LibraryHealthManager? = null,
    private val analyticsManager: AnalyticsManager? = null,
    private val backupRestoreEngine: BackupRestoreEngine? = null
) : ViewModel() {

    val preferences: StateFlow<UserPreferences> = userPreferencesRepository.userPreferencesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserPreferences())

    private val _healthReport = MutableStateFlow<LibraryHealthReport?>(null)
    val healthReport: StateFlow<LibraryHealthReport?> = _healthReport.asStateFlow()

    private val _analytics = MutableStateFlow<EngineAnalytics?>(null)
    val analytics: StateFlow<EngineAnalytics?> = _analytics.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    init {
        loadAnalytics()
    }

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

    fun updateGridDensity(density: Int) {
        viewModelScope.launch {
            userPreferencesRepository.updateGridDensity(density)
        }
    }

    fun setOnboardingCompleted(completed: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setOnboardingCompleted(completed)
        }
    }

    fun scanLibraryHealth() {
        if (healthManager == null) return
        viewModelScope.launch {
            _isScanning.value = true
            try {
                val report = healthManager.scanLibraryHealth()
                _healthReport.value = report
            } finally {
                _isScanning.value = false
            }
        }
    }

    fun cleanBrokenWallpapers(onFinished: (Int) -> Unit = {}) {
        if (healthManager == null) return
        viewModelScope.launch {
            val count = healthManager.cleanBrokenWallpapers()
            scanLibraryHealth()
            loadAnalytics()
            onFinished(count)
        }
    }

    fun cleanDuplicates(onFinished: (Int) -> Unit = {}) {
        if (healthManager == null) return
        viewModelScope.launch {
            val count = healthManager.cleanDuplicates()
            scanLibraryHealth()
            loadAnalytics()
            onFinished(count)
        }
    }

    fun cleanUnusedWallpapers(onFinished: (Int) -> Unit = {}) {
        if (healthManager == null) return
        viewModelScope.launch {
            val count = healthManager.cleanUnusedWallpapers()
            scanLibraryHealth()
            loadAnalytics()
            onFinished(count)
        }
    }

    fun loadAnalytics() {
        if (analyticsManager == null) return
        viewModelScope.launch {
            try {
                _analytics.value = analyticsManager.getAnalytics()
            } catch (e: Exception) {
                // Ignore in minimal context
            }
        }
    }

    fun exportBackup(onResult: (String) -> Unit) {
        if (backupRestoreEngine == null) return
        viewModelScope.launch {
            val json = backupRestoreEngine.exportBackupJson()
            onResult(json)
        }
    }

    fun restoreBackup(json: String, onResult: (BackupSummary?) -> Unit) {
        if (backupRestoreEngine == null) return
        viewModelScope.launch {
            try {
                val summary = backupRestoreEngine.restoreBackupJson(json)
                scanLibraryHealth()
                loadAnalytics()
                onResult(summary)
            } catch (e: Exception) {
                onResult(null)
            }
        }
    }

    class Factory(
        private val userPreferencesRepository: UserPreferencesRepository,
        private val healthManager: LibraryHealthManager? = null,
        private val analyticsManager: AnalyticsManager? = null,
        private val backupRestoreEngine: BackupRestoreEngine? = null
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SettingsViewModel(userPreferencesRepository, healthManager, analyticsManager, backupRestoreEngine) as T
        }
    }
}
