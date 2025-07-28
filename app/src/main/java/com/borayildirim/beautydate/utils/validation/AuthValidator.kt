package com.borayildirim.beautydate.utils.validation

import android.util.Patterns
import com.borayildirim.beautydate.utils.PasswordValidator
import com.borayildirim.beautydate.viewmodels.state.AuthUiState

/**
 * Authentication form validator
 * Single responsibility: Validate authentication forms
 */
object AuthValidator {
    
    /**
     * Validates email format
     */
    fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
    
    /**
     * Validates password strength
     */
    fun isValidPassword(password: String): Boolean {
        return PasswordValidator.validatePassword(password).isValid
    }
    
    /**
     * Validates if passwords match
     */
    fun doPasswordsMatch(password: String, confirmPassword: String): Boolean {
        return password == confirmPassword
    }
    
    /**
     * Validates all register fields including city and district
     */
    fun validateRegisterFields(state: AuthUiState, selectedCity: String = "", selectedDistrict: String = ""): ValidationResult {
        val errors = mutableListOf<String>()
        
        if (state.username.isBlank()) {
            errors.add("Kullanıcı adı gerekli")
        }
        
        if (!isValidEmail(state.email)) {
            errors.add("Geçerli bir e-mail adresi girin")
        }
        
        if (!isValidPassword(state.password)) {
            errors.add("Şifre gereksinimlerini karşılamıyor")
        }
        
        if (!doPasswordsMatch(state.password, state.confirmPassword)) {
            errors.add("Şifreler eşleşmiyor")
        }
        
        if (state.businessName.isBlank()) {
            errors.add("İşletme adı gerekli")
        }
        
        if (state.phoneNumber.isBlank()) {
            errors.add("Telefon numarası gerekli")
        }
        
        if (selectedCity.isBlank()) {
            errors.add("İl seçimi gerekli")
        }
        
        if (selectedDistrict.isBlank()) {
            errors.add("İlçe seçimi gerekli")
        }
        
        if (state.address.isBlank()) {
            errors.add("Adres gerekli")
        }
        
        return ValidationResult(
            isValid = errors.isEmpty(),
            errors = errors
        )
    }
    
    /**
     * Checks if all required register fields are filled for button enablement
     */
    fun areRegisterFieldsFilled(state: AuthUiState, selectedCity: String = "", selectedDistrict: String = ""): Boolean {
        return state.username.isNotBlank() &&
                state.email.isNotBlank() &&
                state.password.isNotBlank() &&
                state.confirmPassword.isNotBlank() &&
                state.businessName.isNotBlank() &&
                state.phoneNumber.isNotBlank() &&
                selectedCity.isNotBlank() &&
                selectedDistrict.isNotBlank() &&
                state.address.isNotBlank()
    }
    
    /**
     * Validates login fields
     */
    fun validateLoginFields(state: AuthUiState): ValidationResult {
        val errors = mutableListOf<String>()
        
        if (state.username.isBlank()) {
            errors.add("Kullanıcı adı gerekli")
        }
        
        if (state.password.isBlank()) {
            errors.add("Şifre gerekli")
        }
        
        return ValidationResult(
            isValid = errors.isEmpty(),
            errors = errors
        )
    }
}

/**
 * Validation result data class
 */
data class ValidationResult(
    val isValid: Boolean,
    val errors: List<String>
) 