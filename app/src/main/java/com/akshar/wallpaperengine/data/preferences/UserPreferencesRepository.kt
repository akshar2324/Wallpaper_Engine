package com.akshar.wallpaperengine.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

data class UserPreferences(
    val themeId: String = "abyss",
    val shaderEnabled: Boolean = true,
    val shaderIntensity: String = "MEDIUM",
    val shaderStyle: String = "THEME_DEFAULT",
    val reduceMotion: Boolean = false,
    val performanceMode: String = "BALANCED",
    val defaultTargetScreen: String = "HOME_AND_LOCK",
    val defaultPositioning: String = "FILL",
    val confirmBeforeApply: Boolean = false,
    val confirmBeforeDelete: Boolean = true,
    val gridDensity: Int = 2,
    val defaultSort: String = "RECENTLY_ADDED",
    val onboardingCompleted: Boolean = false
)

class UserPreferencesRepository(private val context: Context) {

    private object PreferenceKeys {
        val THEME_ID = stringPreferencesKey("theme_id")
        val SHADER_ENABLED = booleanPreferencesKey("shader_enabled")
        val SHADER_INTENSITY = stringPreferencesKey("shader_intensity")
        val SHADER_STYLE = stringPreferencesKey("shader_style")
        val REDUCE_MOTION = booleanPreferencesKey("reduce_motion")
        val PERFORMANCE_MODE = stringPreferencesKey("performance_mode")
        val DEFAULT_TARGET_SCREEN = stringPreferencesKey("default_target_screen")
        val DEFAULT_POSITIONING = stringPreferencesKey("default_positioning")
        val CONFIRM_BEFORE_APPLY = booleanPreferencesKey("confirm_before_apply")
        val CONFIRM_BEFORE_DELETE = booleanPreferencesKey("confirm_before_delete")
        val GRID_DENSITY = intPreferencesKey("grid_density")
        val DEFAULT_SORT = stringPreferencesKey("default_sort")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
    }

    val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data.map { preferences ->
        UserPreferences(
            themeId = preferences[PreferenceKeys.THEME_ID] ?: "abyss",
            shaderEnabled = preferences[PreferenceKeys.SHADER_ENABLED] ?: true,
            shaderIntensity = preferences[PreferenceKeys.SHADER_INTENSITY] ?: "MEDIUM",
            shaderStyle = preferences[PreferenceKeys.SHADER_STYLE] ?: "THEME_DEFAULT",
            reduceMotion = preferences[PreferenceKeys.REDUCE_MOTION] ?: false,
            performanceMode = preferences[PreferenceKeys.PERFORMANCE_MODE] ?: "BALANCED",
            defaultTargetScreen = preferences[PreferenceKeys.DEFAULT_TARGET_SCREEN] ?: "HOME_AND_LOCK",
            defaultPositioning = preferences[PreferenceKeys.DEFAULT_POSITIONING] ?: "FILL",
            confirmBeforeApply = preferences[PreferenceKeys.CONFIRM_BEFORE_APPLY] ?: false,
            confirmBeforeDelete = preferences[PreferenceKeys.CONFIRM_BEFORE_DELETE] ?: true,
            gridDensity = preferences[PreferenceKeys.GRID_DENSITY] ?: 2,
            defaultSort = preferences[PreferenceKeys.DEFAULT_SORT] ?: "RECENTLY_ADDED",
            onboardingCompleted = preferences[PreferenceKeys.ONBOARDING_COMPLETED] ?: false
        )
    }

    suspend fun updateThemeId(themeId: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferenceKeys.THEME_ID] = themeId
        }
    }

    suspend fun updateShaderEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferenceKeys.SHADER_ENABLED] = enabled
        }
    }

    suspend fun updateShaderIntensity(intensity: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferenceKeys.SHADER_INTENSITY] = intensity
        }
    }

    suspend fun updateShaderStyle(style: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferenceKeys.SHADER_STYLE] = style
        }
    }

    suspend fun updateReduceMotion(reduce: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferenceKeys.REDUCE_MOTION] = reduce
        }
    }

    suspend fun updatePerformanceMode(mode: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferenceKeys.PERFORMANCE_MODE] = mode
        }
    }

    suspend fun updateDefaultTargetScreen(target: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferenceKeys.DEFAULT_TARGET_SCREEN] = target
        }
    }

    suspend fun updateDefaultPositioning(positioning: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferenceKeys.DEFAULT_POSITIONING] = positioning
        }
    }

    suspend fun updateGridDensity(density: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferenceKeys.GRID_DENSITY] = density
        }
    }

    suspend fun updateDefaultSort(sort: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferenceKeys.DEFAULT_SORT] = sort
        }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferenceKeys.ONBOARDING_COMPLETED] = completed
        }
    }
}
