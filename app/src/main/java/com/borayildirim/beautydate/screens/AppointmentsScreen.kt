package com.borayildirim.beautydate.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.borayildirim.beautydate.data.models.Appointment
import com.borayildirim.beautydate.data.models.AppointmentStatus
import com.borayildirim.beautydate.data.models.PaymentMethod
import com.borayildirim.beautydate.viewmodels.AppointmentViewModel
import com.borayildirim.beautydate.components.AppointmentCard
import com.borayildirim.beautydate.components.AppointmentSearchBar
import com.borayildirim.beautydate.components.LoadingWithBreathingLogo
import com.borayildirim.beautydate.components.AppointmentFilterChips
import com.borayildirim.beautydate.components.AppointmentColorLegend
import com.borayildirim.beautydate.components.AppointmentDetailBottomSheet
import com.borayildirim.beautydate.components.PaymentMethodBottomSheet
import com.borayildirim.beautydate.utils.ToastUtils
import com.google.firebase.auth.FirebaseAuth

/**
 * Main appointments screen with list view and filtering
 * Material Design 3 UI with modern appointment management features
 * Memory efficient: LazyColumn with filtered data and reactive updates
 * Enhanced: Color-coded appointment cards with status legend
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentsScreen(
    onNavigateToAddAppointment: () -> Unit = {},
    onNavigateToEditAppointment: (String) -> Unit = {},  // Updated to pass appointmentId
    appointmentViewModel: AppointmentViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by appointmentViewModel.uiState.collectAsState()
    val context = LocalContext.current
    
    // Local state for payment method bottom sheet
    var showPaymentBottomSheet by remember { mutableStateOf(false) }
    var appointmentForPayment by remember { mutableStateOf<Appointment?>(null) }
    
    // Initialize appointments for current user
    LaunchedEffect(Unit) {
        val businessId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        if (businessId.isNotEmpty()) {
            appointmentViewModel.initializeAppointments()
        }
    }
    
    // Show success message with specific toasts
    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let { message ->
            when {
                message.contains("iptal") -> ToastUtils.showInfo(context, "Randevu iptal edildi")
                message.contains("tamamlandı") -> ToastUtils.showSuccess(context, "Randevu tamamlandı olarak işaretlendi")
                message.contains("gelmedi") || message.contains("no show") -> ToastUtils.showInfo(context, "Randevu 'Gelmedi' olarak işaretlendi")
                message.contains("silindi") -> ToastUtils.showError(context, "Randevu silindi")
                else -> ToastUtils.showSuccess(context, message)
            }
            appointmentViewModel.clearMessages()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Randevular",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    // Sync button
                    IconButton(
                        onClick = {
                            val businessId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                            if (businessId.isNotEmpty()) {
                                appointmentViewModel.syncAppointments()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = "Senkronize Et",
                            tint = if (uiState.isSyncing) MaterialTheme.colorScheme.primary 
                                  else MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            // Search bar
            AppointmentSearchBar(
                searchQuery = uiState.searchQuery,
                onSearchQueryChange = { query ->
                    appointmentViewModel.searchAppointments(query)
                },
                isSearchActive = uiState.isSearchActive,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )
            
            // Filter chips with real-time count updates
            AppointmentFilterChips(
                selectedStatus = uiState.selectedStatus,
                onStatusSelected = { status ->
                    appointmentViewModel.filterAppointments(status)
                },
                appointmentCounts = uiState.statusStatistics,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            // Color Legend
            AppointmentColorLegend(
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            // Content based on state with improved logic
            when {
                uiState.isLoading && !uiState.hasAppointments -> {
                    LoadingContent()
                }
                
                uiState.errorMessage != null -> {
                    ErrorContent(
                        errorMessage = uiState.errorMessage,
                        onRetry = {
                            val businessId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                            appointmentViewModel.initializeAppointments()
                        }
                    )
                }
                
                !uiState.hasAppointments -> {
                    EmptyContent(
                        onAddAppointment = {
                            appointmentViewModel.setShowAddAppointmentSheet(true)
                        }
                    )
                }
                
                uiState.isFilteredEmpty -> {
                    NoResultsContent(
                        isSearchActive = uiState.isSearchActive,
                        isFilterActive = uiState.isFilterActive,
                        onClearFilters = {
                            appointmentViewModel.searchAppointments("")
                            appointmentViewModel.filterAppointments(null)
                        }
                    )
                }
                
                else -> {
                    AppointmentList(
                        appointments = uiState.displayedAppointments,
                        onAppointmentClick = { appointment ->
                            appointmentViewModel.selectAppointment(appointment)
                        },
                        onStatusUpdate = { appointmentId, status ->
                            appointmentViewModel.updateAppointmentStatus(appointmentId, status)
                        },
                        onDeleteAppointment = { appointmentId ->
                            appointmentViewModel.deleteAppointment(appointmentId)
                        },
                        onPaymentMethodRequired = { appointment ->
                            appointmentForPayment = appointment
                            showPaymentBottomSheet = true
                        },
                        isLoading = uiState.isLoading
                    )
                }
            }
        }
    }
    
    // Success message snackbar
    uiState.successMessage?.let { message ->
        LaunchedEffect(message) {
            // You can show a snackbar here if needed
        }
    }
    
    // Appointment Detail BottomSheet
    uiState.selectedAppointment?.let { appointment ->
        AppointmentDetailBottomSheet(
            appointment = appointment,
            onDismiss = {
                appointmentViewModel.selectAppointment(null)
            },
            onEdit = {
                appointmentViewModel.selectAppointment(null) // Clear selection
                onNavigateToEditAppointment(appointment.id)
            }
        )
    }
    
    // Payment Method BottomSheet
    if (showPaymentBottomSheet && appointmentForPayment != null) {
        PaymentMethodBottomSheet(
            appointmentId = appointmentForPayment!!.id,
            customerName = appointmentForPayment!!.customerName,
            serviceName = appointmentForPayment!!.serviceName,
            servicePrice = appointmentForPayment!!.formattedPrice,
            onPaymentMethodSelected = { appointmentId, paymentMethodString ->
                // Convert string to PaymentMethod enum
                val paymentMethod = when (paymentMethodString) {
                    "CASH" -> PaymentMethod.CASH
                    "CREDIT_CARD" -> PaymentMethod.CREDIT_CARD
                    "BANK_TRANSFER" -> PaymentMethod.BANK_TRANSFER
                    else -> PaymentMethod.CASH // Default fallback
                }
                
                // Complete appointment with payment method (creates payment record)
                appointmentViewModel.completeAppointmentWithPayment(appointmentId, paymentMethod)
                showPaymentBottomSheet = false
                appointmentForPayment = null
            },
            onDismiss = {
                showPaymentBottomSheet = false
                appointmentForPayment = null
            }
        )
    }
}

/**
 * Loading content component
 */
@Composable
private fun LoadingContent() {
    LoadingWithBreathingLogo(
        message = "Randevular yükleniyor...",
        subMessage = "Lütfen bekleyiniz",
        modifier = Modifier.fillMaxSize()
    )
}

/**
 * Error content component
 */
@Composable
private fun ErrorContent(
    errorMessage: String?,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Hata Oluştu",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = errorMessage ?: "Bilinmeyen hata oluştu",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Tekrar Dene")
            }
        }
    }
}

/**
 * Empty content component
 */
@Composable
private fun EmptyContent(
    onAddAppointment: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CalendarToday,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Henüz Randevu Yok",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Randevular Takvim ekranından oluşturulabilir.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * No search/filter results content
 */
@Composable
private fun NoResultsContent(
    isSearchActive: Boolean,
    isFilterActive: Boolean,
    onClearFilters: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.SearchOff,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Sonuç Bulunamadı",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = when {
                    isSearchActive && isFilterActive -> "Arama ve filtre kriterlerinize uygun randevu bulunamadı."
                    isSearchActive -> "Arama kriterinize uygun randevu bulunamadı."
                    isFilterActive -> "Seçilen filtre kriterine uygun randevu bulunamadı."
                    else -> "Randevu bulunamadı."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            OutlinedButton(
                onClick = onClearFilters
            ) {
                Icon(
                    imageVector = Icons.Default.FilterAltOff,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Filtreleri Temizle")
            }
        }
    }
}

/**
 * Appointment list component
 */
@Composable
private fun AppointmentList(
    appointments: List<Appointment>,
    onAppointmentClick: (Appointment) -> Unit,
    onStatusUpdate: (String, AppointmentStatus) -> Unit,
    onDeleteAppointment: (String) -> Unit,
    onPaymentMethodRequired: (Appointment) -> Unit = {},
    isLoading: Boolean
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(bottom = 16.dp) // Normal bottom padding
    ) {
        items(
            items = appointments,
            key = { appointment -> appointment.id }
        ) { appointment ->
            AppointmentCard(
                appointment = appointment,
                onClick = { onAppointmentClick(appointment) },
                onStatusUpdate = { status -> onStatusUpdate(appointment.id, status) },
                onDelete = { onDeleteAppointment(appointment.id) },
                onPaymentMethodRequired = onPaymentMethodRequired,
                isLoading = isLoading,
                modifier = Modifier
                    .fillMaxWidth()
            )
        }
    }
} 