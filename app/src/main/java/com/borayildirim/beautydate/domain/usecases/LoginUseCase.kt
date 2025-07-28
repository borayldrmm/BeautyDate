package com.borayildirim.beautydate.domain.usecases

import com.borayildirim.beautydate.data.repository.AuthRepository
import com.borayildirim.beautydate.utils.validation.AuthValidator
import com.borayildirim.beautydate.viewmodels.state.AuthUiState
import com.google.firebase.auth.FirebaseUser
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Use case for user login operations
 * Single responsibility: Handle login business logic
 */
@Singleton
class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    
    /**
     * Performs user login with validation
     */
    suspend fun execute(uiState: AuthUiState): LoginResult {
        // Validate input
        val validation = AuthValidator.validateLoginFields(uiState)
        if (!validation.isValid) {
            return LoginResult.ValidationError(validation.errors.first())
        }
        
        return try {
            val result = authRepository.signInWithUsernameAndPassword(
                uiState.username, 
                uiState.password
            )
            
            result.fold(
                onSuccess = { user ->
                    val isEmailVerified = authRepository.isEmailVerified(user)
                    
                    if (!isEmailVerified) {
                        LoginResult.EmailNotVerified
                    } else {
                        // Save remember username settings
                        authRepository.saveRememberUsernameEnabled(uiState.rememberUsername)
                        if (uiState.rememberUsername) {
                            authRepository.saveRememberedUsername(uiState.username)
                        } else {
                            // Clear remembered username if checkbox is unchecked
                            authRepository.saveRememberedUsername("")
                        }
                        LoginResult.Success(user)
                    }
                },
                onFailure = { exception ->
                    LoginResult.Error(exception.message ?: "Giriş başarısız")
                }
            )
        } catch (e: Exception) {
            LoginResult.Error(e.message ?: "Giriş başarısız")
        }
    }
}

/**
 * Login operation result sealed class
 */
sealed class LoginResult {
    data class Success(val user: FirebaseUser) : LoginResult()
    data class Error(val message: String) : LoginResult()
    data class ValidationError(val message: String) : LoginResult()
    object EmailNotVerified : LoginResult()
} 