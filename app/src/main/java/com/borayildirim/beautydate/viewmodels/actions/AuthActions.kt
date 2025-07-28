package com.borayildirim.beautydate.viewmodels.actions

/**
 * Interface defining all authentication actions
 * Following Interface Segregation Principle
 */
interface AuthActions {
    
    // Form field updates
    fun updateUsername(username: String)
    fun updateEmail(email: String)
    fun updatePassword(password: String)
    fun updateConfirmPassword(confirmPassword: String)
    fun updateBusinessName(businessName: String)
    fun updatePhoneNumber(phoneNumber: String)
    fun updateAddress(address: String)
    fun updateTaxNumber(taxNumber: String)
    fun updateRememberUsername(remember: Boolean)
    fun updateAcceptedTerms(accepted: Boolean)
    
    // Authentication operations
    fun signIn()
    fun register()
    fun signOut()
    fun forgotPassword(email: String)
    fun deleteAccount(email: String, password: String)

    
    // Profile operations
    fun updateProfile()
    
    // Navigation and state management
    fun clearError()
    fun clearSuccess()
    fun clearNavigationFlag()
    fun clearRegistrationSuccess()
    fun clearRegisterForm()
    
    // Utility
    fun isLoggedIn(): Boolean
} 