package com.borayildirim.beautydate.domain.models

/**
 * Domain model for feedback data
 * Contains user feedback information with validation rules
 */
data class FeedbackData(
    val subject: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
) {
    
    companion object {
        const val MAX_MESSAGE_LENGTH = 300
        const val MIN_SUBJECT_LENGTH = 3
        const val MIN_MESSAGE_LENGTH = 20
    }
    
    /**
     * Validates the feedback data
     * @return ValidationResult with success status and error message
     */
    fun validate(): ValidationResult {
        return when {
            subject.isBlank() -> ValidationResult(
                isValid = false,
                errorMessage = "Konu alanı boş bırakılamaz"
            )
            subject.length < MIN_SUBJECT_LENGTH -> ValidationResult(
                isValid = false,
                errorMessage = "Konu en az $MIN_SUBJECT_LENGTH karakter olmalıdır"
            )
            message.isBlank() -> ValidationResult(
                isValid = false,
                errorMessage = "Mesaj alanı boş bırakılamaz"
            )
            message.length < MIN_MESSAGE_LENGTH -> ValidationResult(
                isValid = false,
                errorMessage = "Mesaj en az $MIN_MESSAGE_LENGTH karakter olmalıdır"
            )
            message.length > MAX_MESSAGE_LENGTH -> ValidationResult(
                isValid = false,
                errorMessage = "Mesaj en fazla $MAX_MESSAGE_LENGTH karakter olabilir"
            )
            else -> ValidationResult(isValid = true)
        }
    }
}

/**
 * Validation result for feedback data
 */
data class ValidationResult(
    val isValid: Boolean,
    val errorMessage: String? = null
) 