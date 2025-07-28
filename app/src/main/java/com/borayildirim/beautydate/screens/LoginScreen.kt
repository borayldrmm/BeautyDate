package com.borayildirim.beautydate.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.borayildirim.beautydate.components.LoadingWithBreathingLogo
import com.borayildirim.beautydate.viewmodels.AuthViewModel
import androidx.compose.ui.text.style.TextAlign

/**
 * Main login screen. Handles state, validation, and navigation.
 */
@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,
    onNavigateToRegister: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToForgotPassword: () -> Unit = {}
) {
    val uiState by authViewModel.uiState.collectAsState()
    var showSuccessDialog by remember { mutableStateOf(false) }
    
    // Handle successful login
    LaunchedEffect(uiState.isLoggedIn) {
        if (uiState.isLoggedIn) {
            onNavigateToHome()
        }
    }
    
    // Show success dialog when registration success message is available
    LaunchedEffect(uiState.registrationSuccessMessage) {
        if (uiState.registrationSuccessMessage != null) {
            showSuccessDialog = true
        }
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
        // Header section with logo and title
        LoginHeaderSection()
        
        // Form section with fields and login button
        LoginFormSection(
            username = uiState.username,
            onUsernameChange = { authViewModel.updateUsername(it) },
            password = uiState.password,
            onPasswordChange = { authViewModel.updatePassword(it) },
            rememberUsername = uiState.rememberUsername,
            onRememberUsernameChange = { authViewModel.updateRememberUsername(it) },
            onLoginClick = { authViewModel.signIn() },
            isLoading = uiState.isLoading,
            onForgotPasswordClick = onNavigateToForgotPassword
        )
        
        // Footer section with register link
        LoginFooterSection(
            onNavigateToRegister = onNavigateToRegister
        )



        // Error message
        if (uiState.errorMessage != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = uiState.errorMessage!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
        
        Spacer(modifier = Modifier.weight(1f))
        }
        
        // Breathing Logo Loading Overlay
        if (uiState.isLoading) {
            LoadingWithBreathingLogo(
                message = "Giriş yapılıyor...",
                subMessage = "Lütfen bekleyiniz",
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
            )
        }
    }
    
    // Registration success dialog
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { 
                showSuccessDialog = false
                authViewModel.clearRegistrationSuccess()
            },
            title = {
                Text(text = "Kayıt Başarılı!")
            },
            text = {
                Text(text = "Lütfen e-mail adresinize gönderilen linke tıklayarak doğrulama yapınız.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSuccessDialog = false
                        authViewModel.clearRegistrationSuccess()
                    }
                ) {
                    Text("Tamam")
                }
            }
        )
    }
} 