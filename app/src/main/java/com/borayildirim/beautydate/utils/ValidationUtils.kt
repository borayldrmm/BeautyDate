package com.borayildirim.beautydate.utils

import android.util.Patterns

/**
 * Utility class for input validation
 */
object ValidationUtils {
    
    /**
     * Validates email format
     */
    fun isValidEmail(email: String): Boolean {
        return email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
    
    /**
     * Validates password strength
     */
    fun isValidPassword(password: String): Boolean {
        return password.length >= 6
    }
    
    /**
     * Validates age input
     */
    fun isValidAge(age: String): Boolean {
        return try {
            val ageInt = age.toInt()
            ageInt > 0 && ageInt <= 120
        } catch (e: NumberFormatException) {
            false
        }
    }
    
    /**
     * Validates name input
     */
    fun isValidName(name: String): Boolean {
        return name.trim().length >= 2
    }
    
    /**
     * Validates bio input
     */
    fun isValidBio(bio: String): Boolean {
        return bio.length <= 500 // Maximum 500 characters
    }
} 