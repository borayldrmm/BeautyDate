package com.borayildirim.beautydate.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.borayildirim.beautydate.data.local.ThemeMode
import com.borayildirim.beautydate.domain.usecases.ThemeUseCase
import com.borayildirim.beautydate.viewmodels.actions.ThemeActions
import com.borayildirim.beautydate.viewmodels.state.ThemeState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for theme management
 * Handles theme switching and state management
 * Implements ThemeActions following Single Responsibility Principle
 */
@HiltViewModel
class ThemeViewModel @Inject constructor(
    private val themeUseCase: ThemeUseCase
) : ViewModel(), ThemeActions {
    
    // UI State
    private val _uiState = MutableStateFlow(ThemeState())
    val uiState: StateFlow<ThemeState> = _uiState.asStateFlow()
    
    init {
        // Load current theme on initialization
        loadCurrentTheme()
    }
    
    /**
     * Loads current theme from repository
     */
    private fun loadCurrentTheme() {
        viewModelScope.launch {
            themeUseCase.getCurrentTheme()
                .collect { currentTheme ->
                    _uiState.update { 
                        it.copy(currentTheme = currentTheme) 
                    }
                }
        }
    }
    
    /**
     * Switches to the specified theme mode
     * @param mode ThemeMode to switch to
     */
    override fun switchTheme(mode: ThemeMode) {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        
        viewModelScope.launch {
            themeUseCase.switchTheme(mode).fold(
                onSuccess = {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            currentTheme = mode,
                            successMessage = "Tema değiştirildi"
                        ) 
                    }
                },
                onFailure = { exception ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            errorMessage = exception.message ?: "Tema değiştirilemedi"
                        ) 
                    }
                }
            )
        }
    }
    
    /**
     * Toggles between light and dark theme
     */
    override fun toggleTheme() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        
        viewModelScope.launch {
            themeUseCase.toggleTheme().fold(
                onSuccess = { newTheme ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            currentTheme = newTheme,
                            successMessage = "Tema değiştirildi"
                        ) 
                    }
                },
                onFailure = { exception ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            errorMessage = exception.message ?: "Tema değiştirilemedi"
                        ) 
                    }
                }
            )
        }
    }
    
    /**
     * Clears any error messages
     */
    override fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
    
    /**
     * Clears any success messages
     */
    override fun clearSuccess() {
        _uiState.update { it.copy(successMessage = null) }
    }
    
    /**
     * Gets current theme mode for immediate use
     * @return Current ThemeMode
     */
    fun getCurrentTheme(): ThemeMode {
        return _uiState.value.currentTheme
    }
} 