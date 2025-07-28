package com.borayildirim.beautydate.domain.usecases

import com.borayildirim.beautydate.data.repository.FeedbackRepository
import com.borayildirim.beautydate.domain.models.FeedbackData
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Use case for feedback operations
 * Single responsibility: Handle feedback submission logic
 */
@Singleton
class FeedbackUseCase @Inject constructor(
    private val feedbackRepository: FeedbackRepository
) {
    
    /**
     * Submits feedback with validation
     * @param subject Feedback subject
     * @param message Feedback message
     * @return FeedbackResult indicating success or failure
     */
    suspend fun submitFeedback(subject: String, message: String): FeedbackResult {
        return try {
            // Create feedback data object
            val feedbackData = FeedbackData(
                subject = subject.trim(),
                message = message.trim()
            )
            
            // Validate feedback data
            val validation = feedbackData.validate()
            if (!validation.isValid) {
                return FeedbackResult.ValidationError(
                    validation.errorMessage ?: "Geçersiz veri"
                )
            }
            
            // Submit feedback
            feedbackRepository.sendFeedback(feedbackData).fold(
                onSuccess = {
                    FeedbackResult.Success
                },
                onFailure = { exception ->
                    FeedbackResult.Error(
                        exception.message ?: "Geri bildirim gönderilirken hata oluştu"
                    )
                }
            )
            
        } catch (e: Exception) {
            FeedbackResult.Error(e.message ?: "Beklenmeyen hata oluştu")
        }
    }
    
    /**
     * Validates feedback input without submitting
     * @param subject Feedback subject
     * @param message Feedback message
     * @return ValidationResult with validation status
     */
    fun validateFeedbackInput(subject: String, message: String): com.borayildirim.beautydate.domain.models.ValidationResult {
        val feedbackData = FeedbackData(
            subject = subject.trim(),
            message = message.trim()
        )
        return feedbackData.validate()
    }
}

/**
 * Sealed class representing feedback operation results
 */
sealed class FeedbackResult {
    object Success : FeedbackResult()
    data class Error(val message: String) : FeedbackResult()
    data class ValidationError(val message: String) : FeedbackResult()
} 