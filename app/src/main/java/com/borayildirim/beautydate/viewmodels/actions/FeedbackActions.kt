package com.borayildirim.beautydate.viewmodels.actions

/**
 * Interface defining feedback-related actions
 * Following Interface Segregation Principle
 */
interface FeedbackActions {
    
    /**
     * Updates the subject field
     * @param subject New subject text
     */
    fun updateSubject(subject: String)
    
    /**
     * Updates the message field
     * @param message New message text
     */
    fun updateMessage(message: String)
    
    /**
     * Shows confirmation dialog for feedback submission
     */
    fun showConfirmDialog()
    
    /**
     * Hides confirmation dialog
     */
    fun hideConfirmDialog()
    
    /**
     * Submits the feedback form
     */
    fun submitFeedback()
    
    /**
     * Clears the form fields
     */
    fun clearForm()
    
    /**
     * Clears any error messages
     */
    fun clearError()
    
    /**
     * Clears any success messages
     */
    fun clearSuccess()
    
    /**
     * Validates the current form input
     */
    fun validateForm()
} 