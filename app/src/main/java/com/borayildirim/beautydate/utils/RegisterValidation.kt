package com.borayildirim.beautydate.utils

/**
 * Validation utilities for registration form fields
 */
object RegisterValidation {
    
    /**
     * Validates username input
     */
    fun validateUsername(username: String): String {
        return when {
            username.isBlank() -> "Kullanıcı adı boş olamaz"
            username.length < 3 -> "Kullanıcı adı en az 3 karakter olmalıdır"
            else -> ""
        }
    }
    
    /**
     * Validates business name input
     */
    fun validateBusinessName(businessName: String): String {
        return when {
            businessName.isBlank() -> "İşletme adı boş olamaz"
            businessName.length < 2 -> "İşletme adı en az 2 karakter olmalıdır"
            else -> ""
        }
    }
    
    /**
     * Validates business address input
     */
    fun validateBusinessAddress(businessAddress: String): String {
        return when {
            businessAddress.isBlank() -> "İşletme adresi boş olamaz"
            businessAddress.length < 10 -> "İşletme adresi en az 10 karakter olmalıdır"
            else -> ""
        }
    }
    
    /**
     * Validates city selection
     */
    fun validateCity(city: String): String {
        return if (city.isBlank()) "Lütfen bir il seçiniz" else ""
    }
    
    /**
     * Validates district selection
     */
    fun validateDistrict(district: String): String {
        return if (district.isBlank()) "Lütfen bir ilçe seçiniz" else ""
    }
    
    /**
     * Validates tax number input (optional field)
     * Only validates format if a value is entered
     */
    fun validateTaxNumber(taxNumber: String): String {
        return when {
            taxNumber.isBlank() -> "" // Optional field - no error if empty
            taxNumber.length != 10 -> "Vergi numarası 10 haneli olmalıdır"
            !taxNumber.all { it.isDigit() } -> "Vergi numarası sadece rakam içermelidir"
            else -> ""
        }
    }
} 