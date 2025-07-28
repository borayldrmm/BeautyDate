package com.borayildirim.beautydate.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.borayildirim.beautydate.data.models.Customer
import com.borayildirim.beautydate.data.models.CustomerGender
import com.borayildirim.beautydate.screens.components.SimpleContactFormSection
import com.borayildirim.beautydate.screens.components.PersonalInfoFormSection
import com.borayildirim.beautydate.viewmodels.CustomerViewModel
import com.borayildirim.beautydate.utils.ToastUtils
import com.google.firebase.Timestamp

/**
 * Edit customer screen with pre-filled form
 * Memory efficient: reuses existing form components and customer data
 * Provides update functionality with loading states
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCustomerScreen(
    customer: Customer,
    onNavigateBack: () -> Unit,
    customerViewModel: CustomerViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by customerViewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    
    // Memory efficient: use remember for form state to avoid recomposition issues
    var firstName by remember(customer) { mutableStateOf(customer.firstName) }
    var lastName by remember(customer) { mutableStateOf(customer.lastName) }
    var phoneNumber by remember(customer) { mutableStateOf(customer.phoneNumber) }
    var email by remember(customer) { mutableStateOf(customer.email) }
    var birthDate by remember(customer) { mutableStateOf(customer.birthDate) }
    var gender by remember(customer) { mutableStateOf(customer.gender) }
    var notes by remember(customer) { mutableStateOf(customer.notes) }
    
    // Show Toast messages - Memory efficient: reuse context
    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let { message ->
            ToastUtils.showSuccess(context, message)
            customerViewModel.clearSuccess()
            // Navigate back after successful update
            if (message.contains("güncellendi")) {
                onNavigateBack()
            }
        }
    }
    
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            ToastUtils.showError(context, message)
            customerViewModel.clearError()
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Top App Bar
        TopAppBar(
            title = {
                Text(
                    text = "Müşteri Düzenle",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Geri"
                    )
                }
            }
        )
        
        // Loading overlay
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator()
                    Text(
                        text = "Müşteri güncelleniyor...",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            // Edit form content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Personal Information Section
                PersonalInfoFormSection(
                    firstName = firstName,
                    onFirstNameChange = { firstName = it },
                    lastName = lastName,
                    onLastNameChange = { lastName = it },
                    birthDate = birthDate,
                    onBirthDateChange = { birthDate = it },
                    gender = gender,
                    onGenderChange = { gender = it }
                )
                
                // Contact Information Section
                SimpleContactFormSection(
                    phoneNumber = phoneNumber,
                    onPhoneNumberChange = { phoneNumber = it },
                    email = email,
                    onEmailChange = { email = it }
                )
                
                // Notes Section
                NotesFormSection(
                    notes = notes,
                    onNotesChange = { notes = it }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Update Button
                Button(
                    onClick = {
                        val updatedCustomer = customer.copy(
                            firstName = firstName.trim(),
                            lastName = lastName.trim(),
                            phoneNumber = phoneNumber.trim(),
                            email = email.trim(),
                            birthDate = birthDate.trim(),
                            gender = gender,
                            notes = notes.trim(),
                            updatedAt = Timestamp.now()
                        )
                        customerViewModel.updateCustomer(updatedCustomer)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = !uiState.isLoading && firstName.isNotBlank() && lastName.isNotBlank() && phoneNumber.isNotBlank()
                ) {
                    Text(
                        text = "Güncelle",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

/**
 * Notes form section component - Memory efficient
 */
@Composable
private fun NotesFormSection(
    notes: String,
    onNotesChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Notlar",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            OutlinedTextField(
                value = notes,
                onValueChange = onNotesChange,
                label = { Text("Notlar (Opsiyonel)") },
                placeholder = { Text("Müşteri ile ilgili notlarınızı ekleyin") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5,
                singleLine = false
            )
        }
    }
} 