package com.borayildirim.beautydate.viewmodels.state

/**
 * UI State for authentication screens
 * Contains all form data and UI state flags
 */
data class AuthUiState(
    // Form fields
    val username: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val businessName: String = "",
    val phoneNumber: String = "",
    val address: String = "",
    val taxNumber: String = "",
    
    // UI flags
    val rememberUsername: Boolean = false,
    val acceptedTerms: Boolean = false,
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val isRegistered: Boolean = false,
    val shouldNavigateToLogin: Boolean = false,
    
    // Messages
    val registrationSuccessMessage: String? = null,
    val errorMessage: String? = null,
    val successMessage: String? = null
) 