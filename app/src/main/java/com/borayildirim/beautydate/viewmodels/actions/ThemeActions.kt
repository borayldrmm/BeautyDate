package com.borayildirim.beautydate.viewmodels.actions

import com.borayildirim.beautydate.data.local.ThemeMode

/**
 * Interface defining theme-related actions
 * Following Interface Segregation Principle
 */
interface ThemeActions {
    
    /**
     * Switches to the specified theme mode
     * @param mode ThemeMode to switch to
     */
    fun switchTheme(mode: ThemeMode)
    
    /**
     * Toggles between light and dark theme
     */
    fun toggleTheme()
    
    /**
     * Clears any error messages
     */
    fun clearError()
    
    /**
     * Clears any success messages
     */
    fun clearSuccess()
} 