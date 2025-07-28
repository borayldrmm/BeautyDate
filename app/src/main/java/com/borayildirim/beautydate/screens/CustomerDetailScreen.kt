package com.borayildirim.beautydate.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.borayildirim.beautydate.data.models.Customer
import com.borayildirim.beautydate.data.models.CustomerGender
import com.borayildirim.beautydate.viewmodels.CustomerViewModel
import com.borayildirim.beautydate.viewmodels.AppointmentViewModel
import com.borayildirim.beautydate.utils.ToastUtils
import com.borayildirim.beautydate.components.CustomerAppointmentHistoryBottomSheet
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*

/**
 * Customer detail screen showing customer information
 * Memory efficient: reuses same ViewModel instance and context
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerDetailScreen(
    customer: Customer,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (Customer) -> Unit,
    customerViewModel: CustomerViewModel = hiltViewModel(),
    appointmentViewModel: AppointmentViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by customerViewModel.uiState.collectAsStateWithLifecycle()
    val appointmentUiState by appointmentViewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showAppointmentHistory by remember { mutableStateOf(false) }
    
    // Initialize appointments when screen loads
    LaunchedEffect(customer) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUser?.uid?.let { businessId ->
            appointmentViewModel.initializeAppointments()
        }
    }
    
    // Filter appointments for this customer
    val customerAppointments = remember(appointmentUiState.appointments, customer.id) {
        appointmentUiState.appointments.filter { it.customerId == customer.id }
    }
    
    // Memory efficient: reuse same customer object
    val currentCustomer = uiState.selectedCustomer ?: customer
    
    // Show Toast messages - Memory efficient: reuse context
    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let { message ->
            ToastUtils.showSuccess(context, message)
            customerViewModel.clearSuccess()
            // Navigate back after successful operations
            if (message.contains("silindi") || message.contains("güncellendi")) {
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
    
    // Set selected customer for ViewModel operations
    LaunchedEffect(customer) {
        customerViewModel.selectCustomer(customer)
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Clean Top App Bar - Only back button
        TopAppBar(
            title = {
                Text(
                    text = "Müşteri Detayları",
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
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface,
                titleContentColor = MaterialTheme.colorScheme.onSurface
            )
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
                        text = "İşlem yapılıyor...",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            // General Information Content
            GeneralInfoTab(
                customer = currentCustomer,
                onEditClick = { onNavigateToEdit(currentCustomer) },
                onDeleteClick = { showDeleteDialog = true },
                onAppointmentHistoryClick = { showAppointmentHistory = true },
                isLoading = uiState.isLoading
            )
        }
    }
    
    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Müşteriyi Sil") },
            text = { 
                Text("${currentCustomer.firstName} ${currentCustomer.lastName} adlı müşteriyi silmek istediğinizden emin misiniz? Bu işlem geri alınamaz.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        customerViewModel.deleteCustomer(currentCustomer.id)
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Sil")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showDeleteDialog = false }
                ) {
                    Text("İptal")
                }
            }
        )
    }
    
    // Appointment History Bottom Sheet
    if (showAppointmentHistory) {
        CustomerAppointmentHistoryBottomSheet(
            customer = currentCustomer,
            appointments = customerAppointments,
            onDismiss = { showAppointmentHistory = false },
            onAppointmentClick = { appointment ->
                // Handle appointment click if needed
                ToastUtils.showInfo(context, "Randevu: ${appointment.serviceName}")
            }
        )
    }
}

/**
 * General information tab content
 * Shows customer header, personal info, contact info, and action buttons
 */
@Composable
private fun GeneralInfoTab(
    customer: Customer,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onAppointmentHistoryClick: () -> Unit,
    isLoading: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Customer Header Card
        CustomerHeaderCard(customer = customer)
        
        // Personal Information Card
        CustomerPersonalInfoCard(customer = customer)
        
        // Contact Information Card
        CustomerContactInfoCard(customer = customer)
        
        // Notes Card
        if (customer.notes.isNotBlank()) {
            CustomerNotesCard(customer = customer)
        }
        
        // Action Buttons
        ActionButtonsSection(
            onEditClick = onEditClick,
            onDeleteClick = onDeleteClick,
            onAppointmentHistoryClick = onAppointmentHistoryClick,
            isLoading = isLoading
        )
    }
}

@Composable
private fun CustomerHeaderCard(customer: Customer) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Gender Icon
                Icon(
                    imageVector = when (customer.gender) {
                        CustomerGender.MALE -> Icons.Default.Person
                        CustomerGender.FEMALE -> Icons.Default.Person
                        CustomerGender.OTHER -> Icons.Default.Person
                    },
                    contentDescription = "Gender",
                    modifier = Modifier.size(52.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
                
                // Customer Name
                Text(
                    text = "${customer.firstName} ${customer.lastName}",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    textAlign = TextAlign.Center
                )
                
                // Gender Badge
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = when (customer.gender) {
                            CustomerGender.MALE -> "Erkek"
                            CustomerGender.FEMALE -> "Kadın"
                            CustomerGender.OTHER -> "Diğer"
                        },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun CustomerPersonalInfoCard(customer: Customer) {
    InfoCard(
        title = "Kişisel Bilgiler"
    ) {
        InfoRow(
            icon = Icons.Default.Cake,
            label = "Doğum Tarihi",
            value = customer.birthDate
        )
    }
}

@Composable
private fun CustomerContactInfoCard(customer: Customer) {
    InfoCard(
        title = "İletişim Bilgileri"
    ) {
        InfoRow(
            icon = Icons.Default.Phone,
            label = "Telefon",
            value = customer.phoneNumber
        )
        
        if (customer.email.isNotBlank()) {
            InfoRow(
                icon = Icons.Default.Email,
                label = "E-posta",
                value = customer.email
            )
        }
    }
}

@Composable
private fun CustomerNotesCard(customer: Customer) {
    InfoCard(
        title = "Notlar"
    ) {
        InfoRow(
            icon = Icons.Default.Notes,
            label = "Açıklama",
            value = customer.notes
        )
    }
}

@Composable
private fun InfoCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            content()
        }
    }
}

@Composable
private fun InfoRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ActionButtonsSection(
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onAppointmentHistoryClick: () -> Unit,
    isLoading: Boolean
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Appointment History Button (Primary - Blue)
        Button(
            onClick = onAppointmentHistoryClick,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.History,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Randevu Geçmişi",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Edit Button (Orange/Yellow)
            Button(
                onClick = onEditClick,
                modifier = Modifier.weight(1f),
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF8F00), // Orange/Amber color
                    contentColor = Color.White
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Düzenle",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
            
            // Delete Button (Red)
            Button(
                onClick = onDeleteClick,
                modifier = Modifier.weight(1f),
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFD32F2F), // Red color
                    contentColor = Color.White
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Sil",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
        }
    }
} 