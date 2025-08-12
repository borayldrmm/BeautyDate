/**
 * Add Appointment Screen - Enhanced appointment creation interface
 * Icon-based customer and service selection with automatic pricing
 * Material Design 3 with clean architecture and memory efficiency
 */
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.borayildirim.beautydate.R
import com.borayildirim.beautydate.data.models.Customer
import com.borayildirim.beautydate.data.models.Service
import com.borayildirim.beautydate.data.models.Appointment
import com.borayildirim.beautydate.data.models.AppointmentStatus
import com.borayildirim.beautydate.domain.usecases.appointment.AddAppointmentUseCase
import com.borayildirim.beautydate.utils.ToastUtils
import com.borayildirim.beautydate.viewmodels.CustomerViewModel
import com.borayildirim.beautydate.viewmodels.ServiceViewModel
import com.borayildirim.beautydate.viewmodels.AppointmentViewModel
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAppointmentScreen(
    preSelectedDate: String? = null,
    preSelectedTime: String? = null,
    customerViewModel: CustomerViewModel = hiltViewModel(),
    serviceViewModel: ServiceViewModel = hiltViewModel(),
    appointmentViewModel: AppointmentViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToAddCustomer: () -> Unit,
    onNavigateToAddService: () -> Unit,
    onAppointmentCreated: () -> Unit
) {
    val customerUiState by customerViewModel.uiState.collectAsStateWithLifecycle()
    val serviceUiState by serviceViewModel.uiState.collectAsStateWithLifecycle()
    val appointmentUiState by appointmentViewModel.uiState.collectAsState()
    val context = LocalContext.current

    // State for appointment creation
    var selectedCustomer by remember { mutableStateOf<Customer?>(null) }
    var selectedService by remember { mutableStateOf<Service?>(null) }
    var selectedDate by remember { mutableStateOf(preSelectedDate ?: "") }
    var selectedTime by remember { mutableStateOf(preSelectedTime ?: "") }
    var appointmentNotes by remember { mutableStateOf("") }
    var showCustomerDialog by remember { mutableStateOf(false) }
    var showServiceDialog by remember { mutableStateOf(false) }
    var isButtonDisabled by remember { mutableStateOf(false) }

    // Initialize data
    LaunchedEffect(Unit) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUser?.uid?.let { businessId ->
            customerViewModel.initializeCustomers()
            serviceViewModel.initializeServices()
            appointmentViewModel.initializeAppointments()
        }
    }

    // Handle appointment creation success
    LaunchedEffect(appointmentUiState.successMessage) {
        appointmentUiState.successMessage?.let { message ->
            ToastUtils.showSuccess(context, "Randevu başarılı bir şekilde oluşturuldu!")
            appointmentViewModel.clearMessages()
            isButtonDisabled = false
            onAppointmentCreated()
        }
    }
    
    // Handle appointment creation error
    LaunchedEffect(appointmentUiState.errorMessage) {
        appointmentUiState.errorMessage?.let { message ->
            ToastUtils.showError(context, message)
            appointmentViewModel.clearMessages()
            isButtonDisabled = false
        }
    }

    // Calculate total price
    val totalPrice = selectedService?.price ?: 0.0

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Randevu Oluştur",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Geri"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Appointment Date & Time Card
            item {
                AppointmentTimeCard(
                    date = selectedDate,
                    time = selectedTime,
                    onDateChanged = { selectedDate = it },
                    onTimeChanged = { selectedTime = it }
                )
            }

            // Customer Selection Card
            item {
                SelectionCard(
                    title = "Müşteri",
                    selectedItem = selectedCustomer?.let { "${it.firstName} ${it.lastName}" },
                    selectedItemDetails = selectedCustomer?.phoneNumber,
                    icon = Icons.Default.Person,
                    onSelectClick = { showCustomerDialog = true },
                    onAddClick = onNavigateToAddCustomer,
                    placeholder = "Müşteri seçin"
                )
            }

            // Service Selection Card
            item {
                SelectionCard(
                    title = "Hizmet",
                    selectedItem = selectedService?.name,
                    selectedItemDetails = selectedService?.let { "${it.formattedPrice}" },
                    icon = Icons.Default.Palette, // Changed from Build to Palette for beauty services
                    onSelectClick = { showServiceDialog = true },
                    onAddClick = onNavigateToAddService,
                    placeholder = "Hizmet seçin"
                )
            }

            // Notes Card
            item {
                NotesCard(
                    notes = appointmentNotes,
                    onNotesChanged = { appointmentNotes = it }
                )
            }

            // Total Price Card
            if (selectedService != null) {
                item {
                    TotalPriceCard(totalPrice = totalPrice)
                }
            }

            // Create Appointment Button
            item {
                CreateAppointmentButton(
                    isEnabled = selectedCustomer != null && selectedService != null && 
                               selectedDate.isNotBlank() && selectedTime.isNotBlank() && !isButtonDisabled,
                    isLoading = appointmentUiState.isLoading,
                    onClick = {
                        if (selectedCustomer != null && selectedService != null && !isButtonDisabled) {
                            isButtonDisabled = true
                            appointmentViewModel.addAppointment(
                                customer = selectedCustomer!!,
                                serviceName = selectedService!!.name,
                                servicePrice = selectedService!!.price,
                                appointmentDate = selectedDate,
                                appointmentTime = selectedTime,
                                notes = appointmentNotes,
                                serviceId = selectedService!!.id
                            )
                        }
                    }
                )
            }

            // Bottom spacing
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    // Customer Selection Dialog
    if (showCustomerDialog) {
        CustomerSelectionDialog(
            customers = customerUiState.customers,
            onCustomerSelected = { customer ->
                selectedCustomer = customer
                showCustomerDialog = false
            },
            onDismiss = { showCustomerDialog = false }
        )
    }

    // Service Selection Dialog
    if (showServiceDialog) {
        ServiceSelectionDialog(
            services = serviceUiState.services,
            onServiceSelected = { service ->
                selectedService = service
                showServiceDialog = false
            },
            onDismiss = { showServiceDialog = false }
        )
    }

    // Error Message handling is now done via LaunchedEffect for appointmentUiState.errorMessage
}

@Composable
private fun AppointmentTimeCard(
    date: String,
    time: String,
    onDateChanged: (String) -> Unit,
    onTimeChanged: (String) -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "Randevu Zamanı",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Date Selector - Clickable
                OutlinedCard(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.outlinedCardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (date.isNotBlank()) date else "Tarih Seç",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = if (date.isNotBlank()) 
                                MaterialTheme.colorScheme.onSurface 
                            else 
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }

                // Time Selector - Clickable
                OutlinedCard(
                    onClick = { showTimePicker = true },
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.outlinedCardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (time.isNotBlank()) time else "Saat Seç",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = if (time.isNotBlank()) 
                                MaterialTheme.colorScheme.onSurface 
                            else 
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
    
    // Date Picker Dialog
    if (showDatePicker) {
        DatePickerDialog(
            currentDate = date,
            onDateSelected = { selectedDate ->
                onDateChanged(selectedDate)
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }
    
    // Time Picker Dialog
    if (showTimePicker) {
        TimePickerDialog(
            currentTime = time,
            onTimeSelected = { selectedTime ->
                onTimeChanged(selectedTime)
                showTimePicker = false
            },
            onDismiss = { showTimePicker = false }
        )
    }
}

@Composable
private fun SelectionCard(
    title: String,
    selectedItem: String?,
    selectedItemDetails: String?,
    icon: ImageVector,
    onSelectClick: () -> Unit,
    onAddClick: () -> Unit,
    placeholder: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (selectedItem != null) {
                // Selected item display
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        Text(
                            text = selectedItem,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        selectedItemDetails?.let { details ->
                            Text(
                                text = details,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            } else {
                // Placeholder
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Text(
                        text = placeholder,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onSelectClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Seç")
                }

                OutlinedButton(
                    onClick = onAddClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Ekle")
                }
            }
        }
    }
}

@Composable
private fun NotesCard(
    notes: String,
    onNotesChanged: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Notes,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Notlar (İsteğe Bağlı)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            OutlinedTextField(
                value = notes,
                onValueChange = onNotesChanged,
                placeholder = {
                    Text("Randevu ile ilgili notlarınızı ekleyin...")
                },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5,
                shape = RoundedCornerShape(8.dp)
            )
        }
    }
}

@Composable
private fun TotalPriceCard(totalPrice: Double) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MonetizationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onTertiaryContainer
                )
                Text(
                    text = "Toplam Ücret",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }

            Text(
                text = "${totalPrice.toInt()} ₺",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
        }
    }
}

@Composable
private fun CreateAppointmentButton(
    isEnabled: Boolean,
    isLoading: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = isEnabled && !isLoading,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.onPrimary
            )
        } else {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = if (isLoading) "Oluşturuluyor..." else "Randevu Oluştur",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun CustomerSelectionDialog(
    customers: List<Customer>,
    onCustomerSelected: (Customer) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Müşteri Seç",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            if (customers.isEmpty()) {
                // Empty state
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PersonOff,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Kaydedilmiş herhangi bir müşteri yok",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Randevu oluşturmak için önce müşteri ekleyin",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.height(300.dp)
                ) {
                    items(customers) { customer ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth(),
                            onClick = { onCustomerSelected(customer) }
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = "${customer.firstName} ${customer.lastName}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = customer.phoneNumber,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("İptal")
            }
        }
    )
}

@Composable
private fun ServiceSelectionDialog(
    services: List<Service>,
    onServiceSelected: (Service) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Hizmet Seç",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            if (services.isEmpty()) {
                // Empty state
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MedicalServices,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Şu anda işletmenizde herhangi bir hizmet vermiyorsunuz",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Randevu oluşturmak için önce hizmet ekleyin",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.height(300.dp)
                ) {
                    items(
                        items = services,
                        key = { service -> service.id }
                    ) { service ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth(),
                            onClick = { onServiceSelected(service) }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = service.name,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = service.category.getDisplayName(),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                    )
                                }
                                Text(
                                    text = service.formattedPrice,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("İptal")
            }
        }
    )
} 

/**
 * Date picker dialog with Material3 DatePicker
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerDialog(
    currentDate: String,
    onDateSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState()
    
    // Parse current date if available
    LaunchedEffect(currentDate) {
        if (currentDate.isNotBlank()) {
            try {
                val parts = currentDate.split("/")
                if (parts.size == 3) {
                    val day = parts[0].toInt()
                    val month = parts[1].toInt()
                    val year = parts[2].toInt()
                    val calendar = java.util.Calendar.getInstance()
                    calendar.set(year, month - 1, day) // Month is 0-based in Calendar
                    datePickerState.selectedDateMillis = calendar.timeInMillis
                }
            } catch (e: Exception) {
                // If parsing fails, use current date
            }
        }
    }
    
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val calendar = java.util.Calendar.getInstance()
                        calendar.timeInMillis = millis
                        val formattedDate = String.format(
                            "%02d/%02d/%04d",
                            calendar.get(java.util.Calendar.DAY_OF_MONTH),
                            calendar.get(java.util.Calendar.MONTH) + 1,
                            calendar.get(java.util.Calendar.YEAR)
                        )
                        onDateSelected(formattedDate)
                    }
                }
            ) {
                Text("Tamam")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("İptal")
            }
        }
    ) {
        DatePicker(
            state = datePickerState,
            title = {
                Text(
                    text = "Randevu Tarihi Seçin",
                    modifier = Modifier.padding(16.dp)
                )
            },
            headline = {
                Text(
                    text = "Tarih seçiniz",
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        )
    }
}

/**
 * Time picker dialog with hourly intervals
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(
    currentTime: String,
    onTimeSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedTime by remember { mutableStateOf(currentTime.ifBlank { "09:00" }) }
    var timeExpanded by remember { mutableStateOf(false) }
    
    // Generate hourly time options from 8:00 to 20:00
    val timeOptions = remember {
        (8..20).map { hour ->
            String.format("%02d:00", hour)
        }
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Randevu Saati Seçin",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = "Randevu saatini seçiniz (saat başı aralıklarda)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                // Time dropdown
                ExposedDropdownMenuBox(
                    expanded = timeExpanded,
                    onExpandedChange = { timeExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedTime,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Saat") },
                        trailingIcon = { 
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = timeExpanded) 
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    )
                    
                    ExposedDropdownMenu(
                        expanded = timeExpanded,
                        onDismissRequest = { timeExpanded = false }
                    ) {
                        timeOptions.forEach { time ->
                            DropdownMenuItem(
                                text = { Text(time) },
                                onClick = {
                                    selectedTime = time
                                    timeExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onTimeSelected(selectedTime)
                }
            ) {
                Text("Tamam")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("İptal")
            }
        }
    )
} 