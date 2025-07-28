package com.borayildirim.beautydate.screens.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.borayildirim.beautydate.components.PasswordField
import com.borayildirim.beautydate.components.PasswordFieldWithInfo
import com.borayildirim.beautydate.utils.PhoneNumberTransformation
import com.borayildirim.beautydate.viewmodels.state.AuthUiState
import com.borayildirim.beautydate.viewmodels.actions.AuthActions

/**
 * Basic registration form section
 * Handles username, email, password, business info
 */
@Composable
fun RegisterFormSection(
    uiState: AuthUiState,
    authActions: AuthActions,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Username field
        OutlinedTextField(
            value = uiState.username,
            onValueChange = { authActions.updateUsername(it) },
            label = { Text("Kullanıcı Adı *") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Email field
        OutlinedTextField(
            value = uiState.email,
            onValueChange = { authActions.updateEmail(it) },
            label = { Text("E-mail *") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Password field with info icon
        PasswordFieldWithInfo(
            value = uiState.password,
            onValueChange = { authActions.updatePassword(it) },
            label = "Şifre *",
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Confirm password field  
        PasswordField(
            value = uiState.confirmPassword,
            onValueChange = { authActions.updateConfirmPassword(it) },
            label = "Şifre Tekrar *",
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Phone number field with Turkish formatting
        OutlinedTextField(
            value = uiState.phoneNumber,
            onValueChange = { newValue ->
                // Only keep digits and limit to 10 digits
                val digits = newValue.filter { it.isDigit() }.take(10)
                authActions.updatePhoneNumber(digits)
            },
            label = { Text("Telefon Numarası *") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            placeholder = { Text("0 (5--) --- -- --") },
            visualTransformation = PhoneNumberTransformation.phoneTransformation
        )
    }
} 