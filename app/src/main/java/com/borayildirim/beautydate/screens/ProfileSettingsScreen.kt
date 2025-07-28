package com.borayildirim.beautydate.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.borayildirim.beautydate.data.models.Customer
import com.borayildirim.beautydate.data.models.CustomerGender
import com.borayildirim.beautydate.screens.components.ProfileFormSection
import com.borayildirim.beautydate.screens.components.SimpleContactFormSection
import com.borayildirim.beautydate.screens.components.PersonalInfoFormSection
import com.borayildirim.beautydate.viewmodels.AuthViewModel
import com.borayildirim.beautydate.viewmodels.CustomerViewModel
import com.borayildirim.beautydate.utils.ToastUtils
import com.google.firebase.Timestamp

/**
 * Profile settings screen for editing business information
 * Allows editing: Business name, address, phone number
 * Read-only: Email, username
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSettingsScreen(
    authViewModel: AuthViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToDeleteAccount: () -> Unit
) {
    val uiState by authViewModel.uiState.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()
    
    var showSaveDialog by remember { mutableStateOf(false) }
    var showDiscardDialog by remember { mutableStateOf(false) }
    
    // Local form state for editing
    var localBusinessName by remember { mutableStateOf(uiState.businessName) }
    var localAddress by remember { mutableStateOf(uiState.address) }
    var localPhoneNumber by remember { mutableStateOf(uiState.phoneNumber) }
    
    // Check if there are changes
    val hasChanges = localBusinessName != uiState.businessName ||
            localAddress != uiState.address ||
            localPhoneNumber != uiState.phoneNumber
    
    // Handle successful profile update
    LaunchedEffect(uiState.successMessage) {
        val successMsg = uiState.successMessage
        if (successMsg != null && successMsg.contains("güncellendi")) {
            // Update local state to match saved state
            localBusinessName = uiState.businessName
            localAddress = uiState.address
            localPhoneNumber = uiState.phoneNumber
            
            // Clear success message after 3 seconds
            kotlinx.coroutines.delay(3000)
            authViewModel.clearSuccess()
        }
    }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top App Bar with back button
        TopAppBar(
            title = {
                Text(
                    text = "Profil Ayarları",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Geri"
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface,
                titleContentColor = MaterialTheme.colorScheme.onSurface
            )
        )
        
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        
        // Form content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile form section
            ProfileFormSection(
                businessName = localBusinessName,
                onBusinessNameChange = { localBusinessName = it },
                address = localAddress,
                onAddressChange = { localAddress = it },
                phoneNumber = localPhoneNumber,
                onPhoneNumberChange = { localPhoneNumber = it },
                email = uiState.email,
                username = uiState.username,
                isLoading = uiState.isLoading
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Save button
            Button(
                onClick = { showSaveDialog = true },
                modifier = Modifier.fillMaxWidth(),
                enabled = hasChanges && !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Değişiklikleri Kaydet")
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Delete Account button
            Button(
                onClick = onNavigateToDeleteAccount,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                ),
                enabled = !uiState.isLoading
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteForever,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Text("Hesabı Sil")
                }
            }
            
            // Error message
            if (uiState.errorMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = uiState.errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            // Success message
            if (uiState.successMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = uiState.successMessage!!,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
    
    // Save confirmation dialog
    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = {
                Text(text = "Değişiklikleri Kaydet")
            },
            text = {
                Text(text = "Profil bilgilerinizi kaydetmek istediğinizden emin misiniz?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSaveDialog = false
                        // Update ViewModel state with local changes
                        authViewModel.updateBusinessName(localBusinessName)
                        authViewModel.updateAddress(localAddress)
                        authViewModel.updatePhoneNumber(localPhoneNumber)
                        // Save to Firestore
                        authViewModel.updateProfile()
                    }
                ) {
                    Text("Kaydet")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showSaveDialog = false }
                ) {
                    Text("İptal")
                }
            }
        )
    }
    
    // Discard changes dialog
    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = {
                Text(text = "Değişiklikleri Kaybet")
            },
            text = {
                Text(text = "Kaydedilmemiş değişiklikleriniz kaybolacak. Devam etmek istediğinizden emin misiniz?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDiscardDialog = false
                        onNavigateBack()
                    }
                ) {
                    Text("Evet")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDiscardDialog = false }
                ) {
                    Text("Hayır")
                }
            }
        )
    }
} 