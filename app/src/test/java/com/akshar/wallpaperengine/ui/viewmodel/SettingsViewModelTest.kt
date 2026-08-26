package com.akshar.wallpaperengine.ui.viewmodel

import android.app.Application
import com.akshar.wallpaperengine.data.maintenance.LibraryHealthManager
import com.akshar.wallpaperengine.data.maintenance.LibraryHealthReport
import com.akshar.wallpaperengine.data.preferences.UserPreferences
import com.akshar.wallpaperengine.data.preferences.UserPreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.*
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class)
class SettingsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testSettingsViewModelThemeAndPerformanceMode() = runTest(testDispatcher) {
        val userPrefsRepo = mock<UserPreferencesRepository>()
        whenever(userPrefsRepo.userPreferencesFlow).thenReturn(flowOf(UserPreferences()))

        val viewModel = SettingsViewModel(userPrefsRepo)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.selectTheme("CYBERPUNK")
        testDispatcher.scheduler.advanceUntilIdle()
        verify(userPrefsRepo).updateThemeId("CYBERPUNK")

        viewModel.toggleShader(true)
        testDispatcher.scheduler.advanceUntilIdle()
        verify(userPrefsRepo).updateShaderEnabled(true)

        viewModel.selectPerformanceMode("PERFORMANCE")
        testDispatcher.scheduler.advanceUntilIdle()
        verify(userPrefsRepo).updatePerformanceMode("PERFORMANCE")
        verify(userPrefsRepo).updateShaderEnabled(false)
        verify(userPrefsRepo).updateReduceMotion(true)
    }

    @Test
    fun testSettingsViewModelScanHealth() = runTest(testDispatcher) {
        val userPrefsRepo = mock<UserPreferencesRepository>()
        val healthManager = mock<LibraryHealthManager>()
        whenever(userPrefsRepo.userPreferencesFlow).thenReturn(flowOf(UserPreferences()))

        val dummyReport = LibraryHealthReport(
            totalWallpapers = 10,
            totalStorageBytes = 2048L,
            brokenWallpapers = emptyList(),
            lowResWallpapers = emptyList(),
            unusedWallpapers = emptyList(),
            duplicateClusters = emptyList()
        )
        whenever(healthManager.scanLibraryHealth()).thenReturn(dummyReport)

        val viewModel = SettingsViewModel(
            userPreferencesRepository = userPrefsRepo,
            healthManager = healthManager
        )
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.scanLibraryHealth()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(dummyReport, viewModel.healthReport.value)
        verify(healthManager).scanLibraryHealth()
    }
}
