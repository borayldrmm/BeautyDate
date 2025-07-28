package com.borayildirim.beautydate.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.borayildirim.beautydate.data.repository.AuthRepository
import com.borayildirim.beautydate.domain.models.OtherMenuItemFactory
import com.borayildirim.beautydate.viewmodels.state.OtherMenuState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Other menu screen
 * Handles menu items, user info, and logout functionality
 * Follows Single Responsibility Principle
 */
@HiltViewModel
class OtherMenuViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    
    // UI State
    private val _uiState = MutableStateFlow(OtherMenuState())
    val uiState: StateFlow<OtherMenuState> = _uiState.asStateFlow()
    
    init {
        loadMenuItems()
        loadUserInfo()
    }
    
    /**
     * Loads menu items from factory
     */
    private fun loadMenuItems() {
        val menuItems = OtherMenuItemFactory.createMenuItems()
        _uiState.update { 
            it.copy(menuItems = menuItems) 
        }
    }
    
    /**
     * Loads current user information
     */
    private fun loadUserInfo() {
        viewModelScope.launch {
            val currentUser = authRepository.getCurrentUser()
            currentUser?.let { user ->
                val userData = authRepository.getUserData(user.uid)
                userData?.let { data ->
                    _uiState.update { 
                        it.copy(
                            businessName = data.businessName,
                            username = data.username
                        ) 
                    }
                }
            }
        }
    }
    
    /**
     * Shows logout confirmation dialog
     */
    fun showLogoutDialog() {
        _uiState.update { it.copy(showLogoutDialog = true) }
    }
    
    /**
     * Hides logout confirmation dialog
     */
    fun hideLogoutDialog() {
        _uiState.update { it.copy(showLogoutDialog = false) }
    }
    
    /**
     * NOTE: Logout operation is now handled by AuthViewModel.signOut()
     * This avoids authentication state conflicts and duplicate logout logic.
     * UI screens should use AuthViewModel for logout operations.
     */
    
    /**
     * Clears any error messages
     */
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
    
    /**
     * Refreshes user information
     */
    fun refreshUserInfo() {
        loadUserInfo()
    }
} 