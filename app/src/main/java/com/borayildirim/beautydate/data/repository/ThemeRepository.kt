package com.borayildirim.beautydate.data.repository

import com.borayildirim.beautydate.data.local.ThemeMode
import com.borayildirim.beautydate.data.local.ThemePreferences
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository interface for theme management
 * Follows Repository pattern and Interface Segregation Principle
 */
interface ThemeRepository {
    
    /**
     * Gets current theme mode as Flow
     * @return Flow of ThemeMode
     */
    fun getCurrentTheme(): Flow<ThemeMode>
    
    /**
     * Sets new theme mode
     * @param mode ThemeMode to set
     */
    suspend fun setTheme(mode: ThemeMode)
    
    /**
     * Gets current theme mode synchronously
     * @return Current ThemeMode
     */
    suspend fun getCurrentThemeSync(): ThemeMode
}

/**
 * Implementation of ThemeRepository
 * Handles theme persistence using ThemePreferences
 */
@Singleton
class ThemeRepositoryImpl @Inject constructor(
    private val themePreferences: ThemePreferences
) : ThemeRepository {
    
    /**
     * Gets current theme mode as Flow
     * @return Flow of ThemeMode from DataStore
     */
    override fun getCurrentTheme(): Flow<ThemeMode> {
        return themePreferences.themeMode
    }
    
    /**
     * Sets new theme mode
     * @param mode ThemeMode to persist
     */
    override suspend fun setTheme(mode: ThemeMode) {
        themePreferences.saveThemeMode(mode)
    }
    
    /**
     * Gets current theme mode synchronously
     * @return Current ThemeMode for immediate use
     */
    override suspend fun getCurrentThemeSync(): ThemeMode {
        return themePreferences.getCurrentThemeMode()
    }
} 