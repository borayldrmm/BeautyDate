package com.borayildirim.beautydate.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * DataStore preferences for theme management
 * Handles Light/Dark theme persistence
 */
private val Context.themeDataStore: DataStore<Preferences> by preferencesDataStore(name = "theme_preferences")

/**
 * Theme mode enumeration
 * Supports Light and Dark themes
 */
enum class ThemeMode {
    LIGHT, DARK
}

/**
 * Theme preferences manager using DataStore
 * Handles saving and retrieving theme selection
 */
@Singleton
class ThemePreferences @Inject constructor(
    private val context: Context
) {
    
    companion object {
        private val THEME_MODE_KEY = stringPreferencesKey("theme_mode")
    }
    
    /**
     * Gets the current theme mode as a Flow
     * @return Flow of ThemeMode, defaults to LIGHT
     */
    val themeMode: Flow<ThemeMode> = context.themeDataStore.data
        .map { preferences ->
            val themeName = preferences[THEME_MODE_KEY] ?: ThemeMode.LIGHT.name
            try {
                ThemeMode.valueOf(themeName)
            } catch (e: IllegalArgumentException) {
                ThemeMode.LIGHT // Fallback to light theme
            }
        }
    
    /**
     * Saves the selected theme mode
     * @param mode ThemeMode to save
     */
    suspend fun saveThemeMode(mode: ThemeMode) {
        context.themeDataStore.edit { preferences ->
            preferences[THEME_MODE_KEY] = mode.name
        }
    }
    
    /**
     * Gets current theme mode synchronously for immediate use
     * @return Current ThemeMode, defaults to LIGHT
     */
    suspend fun getCurrentThemeMode(): ThemeMode {
        return try {
            val preferences = context.themeDataStore.data.first()
            val themeName = preferences[THEME_MODE_KEY] ?: ThemeMode.LIGHT.name
            try {
                ThemeMode.valueOf(themeName)
            } catch (e: IllegalArgumentException) {
                ThemeMode.LIGHT
            }
        } catch (e: Exception) {
            ThemeMode.LIGHT
        }
    }
} 