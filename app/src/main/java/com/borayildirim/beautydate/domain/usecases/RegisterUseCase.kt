package com.borayildirim.beautydate.domain.usecases

import com.borayildirim.beautydate.data.repository.AuthRepository
import com.borayildirim.beautydate.utils.validation.AuthValidator
import com.borayildirim.beautydate.viewmodels.state.AuthUiState
import com.google.firebase.auth.FirebaseUser
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Use case for user registration operations
 * Single responsibility: Handle registration business logic
 */
@Singleton
class RegisterUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    
    /**
     * Performs user registration with validation
     */
    suspend fun execute(uiState: AuthUiState, selectedCity: String = "", selectedDistrict: String = ""): RegisterResult {
        // Validate input including city and district
        val validation = AuthValidator.validateRegisterFields(uiState, selectedCity, selectedDistrict)
        if (!validation.isValid) {
            return RegisterResult.ValidationError(validation.errors.first())
        }
        
        return try {
            val result = authRepository.createUserWithEmailAndPassword(
                username = uiState.username,
                email = uiState.email,
                password = uiState.password,
                businessName = uiState.businessName,
                phoneNumber = uiState.phoneNumber,
                address = uiState.address,
                taxNumber = uiState.taxNumber
            )
            
            result.fold(
                onSuccess = { user ->
                    RegisterResult.Success(
                        user = user,
                        registeredUsername = uiState.username
                    )
                },
                onFailure = { exception ->
                    RegisterResult.Error(exception.message ?: "Kayıt başarısız")
                }
            )
        } catch (e: Exception) {
            RegisterResult.Error(e.message ?: "Kayıt başarısız")
        }
    }
}

/**
 * Registration operation result sealed class
 */
sealed class RegisterResult {
    data class Success(val user: FirebaseUser, val registeredUsername: String) : RegisterResult()
    data class Error(val message: String) : RegisterResult()
    data class ValidationError(val message: String) : RegisterResult()
} 