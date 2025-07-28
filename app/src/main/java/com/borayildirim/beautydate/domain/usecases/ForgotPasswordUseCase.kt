package com.borayildirim.beautydate.domain.usecases

import com.borayildirim.beautydate.data.repository.AuthRepository
import com.borayildirim.beautydate.utils.validation.AuthValidator
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Use case for forgot password operations
 * Single responsibility: Handle password reset business logic
 */
@Singleton
class ForgotPasswordUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    
    /**
     * Sends password reset email with validation
     * @param email Email address for password reset
     * @return Result of the password reset operation
     */
    suspend fun execute(email: String): ForgotPasswordResult {
        // Validate email format
        if (!AuthValidator.isValidEmail(email)) {
            return ForgotPasswordResult.ValidationError("Geçerli bir e-mail adresi giriniz.")
        }
        
        return try {
            val result = authRepository.sendPasswordResetEmail(email)
            
            result.fold(
                onSuccess = {
                    ForgotPasswordResult.Success
                },
                onFailure = { exception ->
                    ForgotPasswordResult.Error(exception.message ?: "Şifre sıfırlama e-postası gönderilemedi.")
                }
            )
        } catch (e: Exception) {
            ForgotPasswordResult.Error(e.message ?: "Şifre sıfırlama e-postası gönderilemedi.")
        }
    }
}

/**
 * Forgot password operation result sealed class
 */
sealed class ForgotPasswordResult {
    object Success : ForgotPasswordResult()
    data class Error(val message: String) : ForgotPasswordResult()
    data class ValidationError(val message: String) : ForgotPasswordResult()
} 