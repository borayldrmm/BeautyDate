package com.borayildirim.beautydate.utils

/**
 * Utility class for password validation
 * Provides password strength checking and validation rules
 */
object PasswordValidator {
    
    /**
     * Password validation rules
     */
    private const val MIN_LENGTH = 8
    private val UPPERCASE_REGEX = Regex("[A-Z]")
    private val LOWERCASE_REGEX = Regex("[a-z]")
    private val NUMBER_REGEX = Regex("\\d")
    
    /**
     * Validates password strength
     * @param password Password to validate
     * @return PasswordValidationResult with validation details
     */
    fun validatePassword(password: String): PasswordValidationResult {
        val hasMinLength = password.length >= MIN_LENGTH
        val hasUppercase = UPPERCASE_REGEX.containsMatchIn(password)
        val hasLowercase = LOWERCASE_REGEX.containsMatchIn(password)
        val hasNumber = NUMBER_REGEX.containsMatchIn(password)
        
        val isValid = hasMinLength && hasUppercase && hasLowercase && hasNumber
        
        return PasswordValidationResult(
            isValid = isValid,
            hasMinLength = hasMinLength,
            hasUppercase = hasUppercase,
            hasLowercase = hasLowercase,
            hasNumber = hasNumber
        )
    }
    
    /**
     * Checks if password meets minimum requirements
     * @param password Password to check
     * @return True if password meets minimum requirements
     */
    fun meetsMinimumRequirements(password: String): Boolean {
        return validatePassword(password).isValid
    }
    
    /**
     * Gets password strength description
     * @param password Password to analyze
     * @return Password strength description
     */
    fun getPasswordStrengthDescription(password: String): String {
        val result = validatePassword(password)
        
        return when {
            result.isValid -> "Strong password"
            password.isEmpty() -> "Enter a password"
            password.length < MIN_LENGTH -> "Too short"
            !result.hasUppercase -> "Missing uppercase letter"
            !result.hasLowercase -> "Missing lowercase letter"
            !result.hasNumber -> "Missing number"
            else -> "Invalid password"
        }
    }
}

/**
 * Result of password validation
 */
data class PasswordValidationResult(
    val isValid: Boolean,
    val hasMinLength: Boolean,
    val hasUppercase: Boolean,
    val hasLowercase: Boolean,
    val hasNumber: Boolean
) 