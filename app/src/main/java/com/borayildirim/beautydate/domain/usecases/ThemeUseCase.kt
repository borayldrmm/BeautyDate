package com.borayildirim.beautydate.domain.usecases

import com.borayildirim.beautydate.data.local.ThemeMode
import com.borayildirim.beautydate.data.repository.ThemeRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Use case for theme operations
 * Single responsibility: Handle theme switching logic
 */
@Singleton
class ThemeUseCase @Inject constructor(
    private val themeRepository: ThemeRepository
) {
    
    /**
     * Gets current theme mode as Flow
     * @return Flow of ThemeMode for reactive UI updates
     */
    fun getCurrentTheme(): Flow<ThemeMode> {
        return themeRepository.getCurrentTheme()
    }
    
    /**
     * Switches to the specified theme mode
     * @param mode ThemeMode to switch to
     * @return Result indicating success or failure
     */
    suspend fun switchTheme(mode: ThemeMode): Result<Unit> {
        return try {
            themeRepository.setTheme(mode)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Toggles between light and dark theme
     * @return Result with new ThemeMode or error
     */
    suspend fun toggleTheme(): Result<ThemeMode> {
        return try {
            val currentTheme = themeRepository.getCurrentThemeSync()
            val newTheme = when (currentTheme) {
                ThemeMode.LIGHT -> ThemeMode.DARK
                ThemeMode.DARK -> ThemeMode.LIGHT
            }
            themeRepository.setTheme(newTheme)
            Result.success(newTheme)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Gets current theme mode synchronously for immediate use
     * @return Current ThemeMode
     */
    suspend fun getCurrentThemeSync(): ThemeMode {
        return themeRepository.getCurrentThemeSync()
    }
} 