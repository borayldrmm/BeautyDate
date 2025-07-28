package com.borayildirim.beautydate.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.borayildirim.beautydate.data.models.Customer
import com.borayildirim.beautydate.data.models.CustomerGender
import com.borayildirim.beautydate.utils.PhoneNumberTransformation
import com.borayildirim.beautydate.utils.ToastUtils
import com.borayildirim.beautydate.viewmodels.CustomerViewModel
import com.borayildirim.beautydate.components.LoadingWithBreathingLogo
import androidx.compose.foundation.background
import com.borayildirim.beautydate.viewmodels.AuthViewModel
import com.google.firebase.Timestamp
import java.util.*

/**
 * Validates phone number input - allows partial input during typing
 */
private fun isValidPhoneNumber(phone: String): Boolean {
    if (phone.isBlank()) return false
    val digits = phone.filter { it.isDigit() }
    
    // Allow partial input during typing, but require full number for final validation
    return when {
        digits.length == 1 && digits == "0" -> true // Just the initial "0"
        digits.length <= 4 -> true // Don't show error for partial input (0 + up to 3 digits)
        digits.length == 11 && digits.startsWith("0") -> true
        else -> false
    }
}

/**
 * Validates complete phone number for form submission
 */
private fun isCompletePhoneNumber(phone: String): Boolean {
    if (phone.isBlank()) return false
    val digits = phone.filter { it.isDigit() }
    return digits.length == 11 && digits.startsWith("0")
}

/**
 * Validates email format
 */
private fun isValidEmail(email: String): Boolean {
    return email.contains("@") && email.contains(".")
}

/**
 * Validates entire form
 */
private fun isFormValid(
    firstName: String,
    lastName: String,
    phoneNumber: String,
    birthDate: String,
    gender: CustomerGender,
    email: String
): Boolean {
    return firstName.isNotBlank() &&
            lastName.isNotBlank() &&
            isCompletePhoneNumber(phoneNumber) &&
            birthDate.isNotBlank() &&
            gender != CustomerGender.OTHER &&
            (email.isBlank() || isValidEmail(email))
}

/**
 * Full-screen page for adding new customers
 * Provides a professional form experience with proper validation and navigation
 * Uses authenticated user's ID as business ID for proper data organization
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCustomerScreen(
    onNavigateBack: () -> Unit,
    onCustomerAdded: (() -> Unit)? = null,
    customerViewModel: CustomerViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    // Form state variables
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("0") }
    var email by remember { mutableStateOf("") }
    var birthDate by remember { mutableStateOf("") }
    var selectedGender by remember { mutableStateOf(CustomerGender.OTHER) }
    var notes by remember { mutableStateOf("") }
    var showErrors by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    // Date picker state
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    val focusManager = LocalFocusManager.current
    val context = LocalContext.current
    val uiState by customerViewModel.uiState.collectAsStateWithLifecycle()
    val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()

    // Handle success/error messages
    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let { message ->

            // Reset loading state immediately
            isLoading = false

            // Show toast message
            ToastUtils.showSuccess(context, message)

            // Clear the success message
            customerViewModel.clearSuccess()

            // Navigate back immediately - no delay needed
            onNavigateBack()

            // Trigger automatic sync after navigation
            currentUser?.let { user ->
                customerViewModel.syncCustomers()
            }
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            isLoading = false
            // Error will be shown in UI
        }
    }

    // Show loading state if no user is authenticated
    if (currentUser == null) {
        LoadingWithBreathingLogo(
            message = "Kullanıcı bilgileri yükleniyor...",
            subMessage = "Lütfen bekleyiniz",
            modifier = modifier.fillMaxSize()
        )
        return
    }

    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Yeni Müşteri Ekle",
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
            },
            bottomBar = {
                // Bottom action bar with cancel and save buttons
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onNavigateBack,
                            modifier = Modifier.weight(1f),
                            enabled = !isLoading
                        ) {
                            Text("İptal")
                        }

                        Button(
                            onClick = {
                                if (!isLoading) { // Prevent multiple submissions
                                    showErrors = true
                                    isLoading = true


                                    if (isFormValid(
                                            firstName,
                                            lastName,
                                            phoneNumber,
                                            birthDate,
                                            selectedGender,
                                            email
                                        )
                                    ) {
                                        isLoading = true

                                        customerViewModel.addCustomer(
                                            firstName = firstName.trim(),
                                            lastName = lastName.trim(),
                                            phoneNumber = phoneNumber,
                                            email = email.trim(),
                                            birthDate = birthDate,
                                            gender = selectedGender,
                                            notes = notes.trim()
                                        )
                                    } else {
                                        isLoading = false
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f),
                            enabled = !isLoading
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Save,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Kaydet")
                            }
                        }
                    }
                }
            }
        ) { paddingValues ->

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {

                // Error message display
                uiState.errorMessage?.let { error ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = error,
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                // Personal Information Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Kişisel Bilgiler",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // First Name
                        OutlinedTextField(
                            value = firstName,
                            onValueChange = { firstName = it },
                            label = { Text("Ad *") },
                            isError = showErrors && firstName.isBlank(),
                            supportingText = if (showErrors && firstName.isBlank()) {
                                { Text("Ad alanı zorunludur") }
                            } else null,
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) }
                            )
                        )

                        // Last Name
                        OutlinedTextField(
                            value = lastName,
                            onValueChange = { lastName = it },
                            label = { Text("Soyad *") },
                            isError = showErrors && lastName.isBlank(),
                            supportingText = if (showErrors && lastName.isBlank()) {
                                { Text("Soyad alanı zorunludur") }
                            } else null,
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) }
                            )
                        )

                        // Birth Date with Date Picker
                        OutlinedTextField(
                            value = birthDate,
                            onValueChange = { /* Read only, handled by date picker */ },
                            label = { Text("Doğum Tarihi *") },
                            isError = showErrors && birthDate.isBlank(),
                            supportingText = if (showErrors && birthDate.isBlank()) {
                                { Text("Doğum tarihi seçiniz") }
                            } else null,
                            modifier = Modifier.fillMaxWidth(),
                            readOnly = true,
                            trailingIcon = {
                                IconButton(onClick = { showDatePicker = true }) {
                                    Icon(
                                        imageVector = Icons.Default.DateRange,
                                        contentDescription = "Tarih Seç"
                                    )
                                }
                            }
                        )

                        // Gender Selection
                        Column {
                            Text(
                                text = "Cinsiyet *",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (showErrors && selectedGender == CustomerGender.OTHER) {
                                    MaterialTheme.colorScheme.error
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .selectableGroup(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                CustomerGender.values()
                                    .filter { it != CustomerGender.OTHER } // Hide OTHER option
                                    .forEach { gender ->
                                        Row(
                                            modifier = Modifier
                                                .selectable(
                                                    selected = selectedGender == gender,
                                                    onClick = { selectedGender = gender },
                                                    role = Role.RadioButton
                                                ),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            RadioButton(
                                                selected = selectedGender == gender,
                                                onClick = { selectedGender = gender }
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = gender.getDisplayName(),
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                        }
                                    }
                            }

                            if (showErrors && selectedGender == CustomerGender.OTHER) {
                                Text(
                                    text = "Cinsiyet seçimi zorunludur",
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                                )
                            }
                        }
                    }
                }

                // Contact Information Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "İletişim Bilgileri",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Phone Number with Masking
                        OutlinedTextField(
                            value = phoneNumber,
                            onValueChange = { newValue ->
                                // Handle phone number input with "0" prefix
                                val digits = newValue.filter { it.isDigit() }

                                when {
                                    // If user tries to clear everything, keep "0"
                                    digits.isEmpty() -> phoneNumber = "0"
                                    // If starts with 0 and has reasonable length
                                    digits.startsWith("0") && digits.length <= 11 -> {
                                        phoneNumber = digits
                                    }
                                    // If doesn't start with 0, add 0 prefix
                                    !digits.startsWith("0") && digits.length <= 10 -> {
                                        phoneNumber = "0$digits"
                                    }
                                    // Don't update if too long
                                    else -> { /* Keep existing value */
                                    }
                                }
                            },
                            label = { Text("Telefon Numarası *") },
                            visualTransformation = PhoneNumberTransformation.phoneTransformation,
                            isError = showErrors && !isCompletePhoneNumber(phoneNumber),
                            supportingText = if (showErrors && !isCompletePhoneNumber(phoneNumber)) {
                                { Text("10 haneli telefon numarası giriniz (532xxxxxxx)") }
                            } else null,
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Phone,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) }
                            )
                        )

                        // Email (Optional)
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("E-posta (Opsiyonel)") },
                            isError = showErrors && email.isNotBlank() && !isValidEmail(email),
                            supportingText = if (showErrors && email.isNotBlank() && !isValidEmail(
                                    email
                                )
                            ) {
                                { Text("Geçerli bir e-posta adresi giriniz") }
                            } else null,
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) }
                            )
                        )
                    }
                }

                // Additional Information Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Ek Bilgiler",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Notes
                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = { Text("Notlar (Opsiyonel)") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            maxLines = 4,
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = { focusManager.clearFocus() }
                            )
                        )
                    }

                    // Breathing Logo Loading Overlay for customer save
                    if (isLoading) {
                        LoadingWithBreathingLogo(
                            message = "Müşteri kaydediliyor...",
                            subMessage = "Lütfen bekleyiniz",
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
                        )
                    }
                }
            }
        }

        // Date Picker Dialog
        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            datePickerState.selectedDateMillis?.let { millis ->
                                val calendar = Calendar.getInstance()
                                calendar.timeInMillis = millis
                                birthDate = String.format(
                                    "%02d/%02d/%04d",
                                    calendar.get(Calendar.DAY_OF_MONTH),
                                    calendar.get(Calendar.MONTH) + 1,
                                    calendar.get(Calendar.YEAR)
                                )
                            }
                            showDatePicker = false
                        }
                    ) {
                        Text("Tamam")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text("İptal")
                    }
                }
            ) {
                DatePicker(
                    state = datePickerState,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}