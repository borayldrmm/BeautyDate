package com.borayildirim.beautydate.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.borayildirim.beautydate.domain.usecases.FeedbackResult
import com.borayildirim.beautydate.domain.usecases.FeedbackUseCase
import com.borayildirim.beautydate.viewmodels.actions.FeedbackActions
import com.borayildirim.beautydate.viewmodels.state.FeedbackUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for feedback form management
 * Handles form validation, submission, and state management
 * Implements FeedbackActions following Single Responsibility Principle
 */
@HiltViewModel
class FeedbackViewModel @Inject constructor(
    private val feedbackUseCase: FeedbackUseCase
) : ViewModel(), FeedbackActions {
    
    // UI State
    private val _uiState = MutableStateFlow(FeedbackUiState())
    val uiState: StateFlow<FeedbackUiState> = _uiState.asStateFlow()
    
    /**
     * Updates the subject field and validates it
     * @param subject New subject text
     */
    override fun updateSubject(subject: String) {
        _uiState.update { 
            it.copy(
                subject = subject,
                subjectError = null
            ) 
        }
        validateForm()
    }
    
    /**
     * Updates the message field and validates it
     * @param message New message text
     */
    override fun updateMessage(message: String) {
        // Limit message to 300 characters
        val trimmedMessage = if (message.length > 300) {
            message.substring(0, 300)
        } else {
            message
        }
        
        _uiState.update { 
            it.copy(
                message = trimmedMessage,
                messageError = null
            ) 
        }
        validateForm()
    }
    
    /**
     * Shows confirmation dialog for feedback submission
     */
    override fun showConfirmDialog() {
        _uiState.update { it.copy(showConfirmDialog = true) }
    }
    
    /**
     * Hides confirmation dialog
     */
    override fun hideConfirmDialog() {
        _uiState.update { it.copy(showConfirmDialog = false) }
    }
    
    /**
     * Submits the feedback form
     */
    override fun submitFeedback() {
        val currentState = _uiState.value
        
        if (!currentState.canSubmit) {
            validateForm()
            return
        }
        
        _uiState.update { 
            it.copy(
                isLoading = true, 
                errorMessage = null,
                showConfirmDialog = false
            ) 
        }
        
        viewModelScope.launch {
            when (val result = feedbackUseCase.submitFeedback(
                subject = currentState.subject,
                message = currentState.message
            )) {
                is FeedbackResult.Success -> {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            successMessage = "Gönderildi, geri bildiriminiz için teşekkür ederiz!",
                            subject = "",
                            message = ""
                        ) 
                    }
                }
                is FeedbackResult.Error -> {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            errorMessage = result.message
                        ) 
                    }
                }
                is FeedbackResult.ValidationError -> {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            errorMessage = result.message
                        ) 
                    }
                }
            }
        }
    }
    
    /**
     * Clears the form fields
     */
    override fun clearForm() {
        _uiState.update { 
            FeedbackUiState()
        }
    }
    
    /**
     * Clears any error messages
     */
    override fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
    
    /**
     * Clears any success messages
     */
    override fun clearSuccess() {
        _uiState.update { it.copy(successMessage = null) }
    }
    
    /**
     * Validates the current form input
     */
    override fun validateForm() {
        val currentState = _uiState.value
        val validation = feedbackUseCase.validateFeedbackInput(
            subject = currentState.subject,
            message = currentState.message
        )
        
        _uiState.update { 
            it.copy(
                isFormValid = validation.isValid,
                subjectError = if (currentState.subject.isBlank()) "Konu gerekli" else null,
                messageError = if (currentState.message.isBlank()) "Mesaj gerekli" else null,
                isSubjectValid = currentState.subject.isNotBlank(),
                isMessageValid = currentState.message.isNotBlank()
            ) 
        }
    }
} 