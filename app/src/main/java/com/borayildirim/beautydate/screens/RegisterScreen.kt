package com.borayildirim.beautydate.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.borayildirim.beautydate.R
import com.borayildirim.beautydate.components.*
import com.borayildirim.beautydate.screens.components.ContactFormSection
import com.borayildirim.beautydate.screens.components.RegisterFormSection
import com.borayildirim.beautydate.utils.validation.AuthValidator
import com.borayildirim.beautydate.viewmodels.AuthViewModel



/**
 * Register screen for the BeautyDate app
 * Features enhanced design with language selector
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun RegisterScreen(
    authViewModel: AuthViewModel,
    onNavigateToLogin: () -> Unit
) {
    val uiState by authViewModel.uiState.collectAsState()
    

    
    // City and district state
    var selectedCity by remember { mutableStateOf("") }
    var selectedDistrict by remember { mutableStateOf("") }
    var showTermsSheet by remember { mutableStateOf(false) }
    
    // Clear register form when screen is opened for fresh registration
    LaunchedEffect(Unit) {
        authViewModel.clearRegisterForm()
    }
    
    // Handle successful registration
    LaunchedEffect(uiState.isRegistered) {
        if (uiState.isRegistered) {
            // Success message will be shown in UI
        }
    }
    
    // Handle navigation to login after successful registration
    LaunchedEffect(uiState.shouldNavigateToLogin) {
        if (uiState.shouldNavigateToLogin) {
            authViewModel.clearNavigationFlag()
            onNavigateToLogin()
        }
    }
    
    // Handle error messages
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            // Error will be displayed in UI
        }
    }


    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top App Bar with back button
        TopAppBar(
            title = { Text("Kayıt Ol") },
            navigationIcon = {
                IconButton(onClick = onNavigateToLogin) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Geri"
                    )
                }
            }
        )
        
        // Scrollable content
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // App logo - Compact and proportional
        Image(
            painter = painterResource(id = R.drawable.logo_png),
            contentDescription = stringResource(R.string.app_name),
            modifier = Modifier.size(180.dp)
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Register title with enhanced styling
        Text(
            text = stringResource(R.string.join_beautydate),
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 28.sp
            ),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(6.dp))
        
        // Subtitle
        Text(
            text = stringResource(R.string.create_your_profile),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // Account Details Section
        Text(
            text = stringResource(R.string.account_details),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Register form section
        RegisterFormSection(
            uiState = uiState,
            authActions = authViewModel
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Business Details Section  
        Text(
            text = stringResource(R.string.business_details),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Contact form section
        ContactFormSection(
            uiState = uiState,
            authActions = authViewModel,
            selectedCity = selectedCity,
            onCitySelected = { selectedCity = it },
            selectedDistrict = selectedDistrict,
            onDistrictSelected = { selectedDistrict = it }
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Terms and Privacy Policy Checkbox
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = uiState.acceptedTerms,
                onCheckedChange = { 
                    // When checkbox is clicked, show terms screen
                    showTermsSheet = true
                }
            )
            Text(
                text = "Kullanım koşullarını kabul ediyorum",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .padding(start = 8.dp)
                    .clickable { 
                        showTermsSheet = true 
                    }
            )
        }
        
        // Register button
        AuthButton(
            onClick = {
                authViewModel.register(selectedCity, selectedDistrict)
            },
            text = stringResource(R.string.register),
            enabled = !uiState.isLoading && 
                     uiState.acceptedTerms && 
                     AuthValidator.areRegisterFieldsFilled(uiState, selectedCity, selectedDistrict),
            isLoading = uiState.isLoading
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Error message
        if (uiState.errorMessage != null) {
            Text(
                text = uiState.errorMessage!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        Spacer(modifier = Modifier.weight(1f))

        // Login link (bottom, centered, side by side, perfectly aligned)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.already_have_account),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(4.dp))
            TextButton(
                onClick = onNavigateToLogin,
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(
                    text = stringResource(R.string.login),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        } // Scrollable content Column
    } // Main Column
    
    // Terms and Privacy Policy Bottom Sheet
    if (showTermsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showTermsSheet = false },
            modifier = Modifier.fillMaxHeight(0.9f)
        ) {
            TermsAndPrivacyScreen(
                onAcceptTerms = {
                    authViewModel.updateAcceptedTerms(true)
                    showTermsSheet = false
                },
                onNavigateBack = {
                    showTermsSheet = false
                }
            )
        }
    }
} 