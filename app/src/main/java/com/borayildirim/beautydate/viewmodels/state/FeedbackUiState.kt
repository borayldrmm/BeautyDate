package com.borayildirim.beautydate.viewmodels.state

/**
 * UI State for feedback screen
 * Contains form data and submission state
 */
data class FeedbackUiState(
    // Form fields
    val subject: String = "",
    val message: String = "",
    
    // Validation state
    val isSubjectValid: Boolean = true,
    val isMessageValid: Boolean = true,
    val subjectError: String? = null,
    val messageError: String? = null,
    
    // UI state
    val isLoading: Boolean = false,
    val isFormValid: Boolean = false,
    val showConfirmDialog: Boolean = false,
    
    // Messages
    val errorMessage: String? = null,
    val successMessage: String? = null
) {
    
    /**
     * Computed property to check if form can be submitted
     */
    val canSubmit: Boolean
        get() = subject.isNotBlank() && 
                message.isNotBlank() && 
                isSubjectValid && 
                isMessageValid && 
                !isLoading
    
    /**
     * Character count for message field
     */
    val messageCharacterCount: Int
        get() = message.length
    
    /**
     * Remaining characters for message field
     */
    val remainingCharacters: Int
        get() = 300 - messageCharacterCount
} 