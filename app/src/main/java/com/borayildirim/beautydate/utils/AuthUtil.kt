package com.borayildirim.beautydate.utils

import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Auth utility class for centralized business authentication management
 * Provides helper functions for multi-tenant businessId operations
 * Memory efficient: single instance with minimal object creation
 */
@Singleton
class AuthUtil @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) {
    
    /**
     * Gets current user's business ID (which is their UID)
     * Returns null if user is not authenticated
     * Used for tenant-based data filtering in all repositories
     */
    fun getCurrentBusinessId(): String? {
        return firebaseAuth.currentUser?.uid
    }
    
    /**
     * Gets current user's business ID with fallback
     * Returns empty string if user is not authenticated
     * Safe to use in query operations
     */
    fun getCurrentBusinessIdSafe(): String {
        return getCurrentBusinessId() ?: ""
    }
    
    /**
     * Checks if user is currently authenticated
     * Used for authentication validation before data operations
     */
    fun isUserAuthenticated(): Boolean {
        return firebaseAuth.currentUser != null
    }
    
    /**
     * Validates if the provided businessId matches current user's business
     * Security check to prevent cross-tenant data access
     */
    fun validateBusinessAccess(businessId: String): Boolean {
        val currentBusinessId = getCurrentBusinessId()
        return currentBusinessId != null && currentBusinessId == businessId
    }
    
    /**
     * Gets current user's UID (alias for getCurrentBusinessId for clarity)
     * In our multi-tenant architecture, UID serves as businessId
     */
    fun getCurrentUserId(): String? {
        return getCurrentBusinessId()
    }
    
    /**
     * Creates authentication error message for unauthorized access
     * Consistent error messaging across the application
     */
    fun getAuthErrorMessage(): String {
        return "Kullanıcı oturumu gerekli. Lütfen giriş yapın."
    }
    
    /**
     * Creates tenant validation error message
     * Used when user tries to access data from different business
     */
    fun getTenantErrorMessage(): String {
        return "Bu veriye erişim yetkiniz bulunmamaktadır."
    }
} 