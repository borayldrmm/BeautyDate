/**
 * Edit Appointment Screen - Enhanced appointment editing interface
 * Pre-fills form with existing appointment data
 * Material Design 3 with clean architecture and memory efficiency
 */
package com.borayildirim.beautydate.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.borayildirim.beautydate.R
import com.borayildirim.beautydate.data.models.Customer
import com.borayildirim.beautydate.data.models.Service
import com.borayildirim.beautydate.data.models.Appointment
import com.borayildirim.beautydate.data.models.AppointmentStatus
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
fun EditAppointmentScreen(
    appointmentId: String,
    onNavigateBack: () -> Unit,
    onNavigateToAddCustomer: () -> Unit = {},
    onNavigateToAddService: () -> Unit = {},
    onAppointmentUpdated: () -> Unit = {},
    customerViewModel: CustomerViewModel = hiltViewModel(),
    serviceViewModel: ServiceViewModel = hiltViewModel(),
    appointmentViewModel: AppointmentViewModel = hiltViewModel()
) {
    val customerUiState by customerViewModel.uiState.collectAsStateWithLifecycle()
    val serviceUiState by serviceViewModel.uiState.collectAsStateWithLifecycle()
    val appointmentUiState by appointmentViewModel.uiState.collectAsState()
    val context = LocalContext.current

    // State for current appointment being edited
    var currentAppointment by remember { mutableStateOf<Appointment?>(null) }
    
    // State for appointment editing - will be filled after appointment loads
    var selectedCustomer by remember { mutableStateOf<Customer?>(null) }
    var selectedService by remember { mutableStateOf<Service?>(null) }
    var selectedDate by remember { mutableStateOf("") }
    var selectedTime by remember { mutableStateOf("") }
    var appointmentNotes by remember { mutableStateOf("") }
    var showCustomerDialog by remember { mutableStateOf(false) }
    var showServiceDialog by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var isButtonDisabled by remember { mutableStateOf(false) }

    // Initialize data and load appointment
    LaunchedEffect(appointmentId) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUser?.uid?.let { businessId ->
            customerViewModel.initializeCustomers()
            serviceViewModel.initializeServices()
            appointmentViewModel.initializeAppointments()
            
            // Load the specific appointment directly from repository by ID
            try {
                val appointment = appointmentViewModel.getAppointmentById(appointmentId)
                if (appointment != null) {
                    currentAppointment = appointment
                    selectedDate = appointment.appointmentDate
                    selectedTime = appointment.appointmentTime
                    appointmentNotes = appointment.notes
                } else {
                }
            } catch (e: Exception) {
            }
        }
    }

    // Pre-select current customer and service when data loads
    LaunchedEffect(customerUiState.customers, serviceUiState.services, currentAppointment) {
        currentAppointment?.let { appointment ->
            if (customerUiState.customers.isNotEmpty() && selectedCustomer == null) {
                selectedCustomer = customerUiState.customers.find { it.id == appointment.customerId }
            }
            if (serviceUiState.services.isNotEmpty() && selectedService == null) {
                selectedService = serviceUiState.services.find { it.id == appointment.serviceId }
            }
        }
    }

    // This LaunchedEffect is now handled in the combined one above

    // Handle appointment update success
    LaunchedEffect(appointmentUiState.successMessage) {
        appointmentUiState.successMessage?.let { message ->
            ToastUtils.showSuccess(context, message)
            appointmentViewModel.clearMessages()
            isButtonDisabled = false
            onAppointmentUpdated()
            onNavigateBack()
        }
    }

    // Handle appointment update error
    LaunchedEffect(appointmentUiState.errorMessage) {
        appointmentUiState.errorMessage?.let { message ->
            ToastUtils.showError(context, message)
            appointmentViewModel.clearMessages()
            isButtonDisabled = false
        }
    }

    // Show loading while appointment is being loaded
    if (currentAppointment == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary
            )
        }
        return
    }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Text(
                    text = "Randevu Düzenle",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
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
                containerColor = MaterialTheme.colorScheme.surface
            )
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Customer Selection
            item {
                AppointmentCustomerCard(
                    selectedCustomer = selectedCustomer,
                    onClick = { showCustomerDialog = true },
                    onAddNewCustomer = onNavigateToAddCustomer
                )
            }

            // Service Selection
            item {
                AppointmentServiceCard(
                    selectedService = selectedService,
                    onClick = { showServiceDialog = true },
                    onAddNewService = onNavigateToAddService
                )
            }

            // Date Selection
            item {
                AppointmentDateCard(
                    selectedDate = selectedDate,
                    onClick = { showDatePicker = true }
                )
            }

            // Time Selection
            item {
                AppointmentTimeCard(
                    selectedTime = selectedTime,
                    onClick = { showTimePicker = true }
                )
            }

            // Notes Section
            item {
                AppointmentNotesCard(
                    notes = appointmentNotes,
                    onNotesChanged = { notes -> appointmentNotes = notes }
                )
            }

            // Update Appointment Button
            item {
                UpdateAppointmentButton(
                    isEnabled = selectedCustomer != null && selectedService != null && 
                               selectedDate.isNotBlank() && selectedTime.isNotBlank() && !isButtonDisabled,
                    isLoading = appointmentUiState.isLoading,
                    onClick = {
                        if (selectedCustomer != null && selectedService != null && currentAppointment != null && !isButtonDisabled) {
                            isButtonDisabled = true
                            
                            val updatedAppointment = currentAppointment!!.copy(
                                customerId = selectedCustomer!!.id,
                                customerName = selectedCustomer!!.fullName,
                                customerPhone = selectedCustomer!!.phoneNumber,
                                serviceId = selectedService!!.id,
                                serviceName = selectedService!!.name,
                                servicePrice = selectedService!!.price,
                                appointmentDate = selectedDate,
                                appointmentTime = selectedTime,
                                notes = appointmentNotes,
                                updatedAt = Timestamp.now()
                            )
                            
                            appointmentViewModel.updateAppointment(updatedAppointment)
                        }
                    }
                )
            }
        }
    }

    // Customer Selection Dialog
    if (showCustomerDialog && customerUiState.customers.isNotEmpty()) {
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
    if (showServiceDialog && serviceUiState.services.isNotEmpty()) {
        ServiceSelectionDialog(
            services = serviceUiState.services,
            onServiceSelected = { service ->
                selectedService = service
                showServiceDialog = false
            },
            onDismiss = { showServiceDialog = false }
        )
    }

    // Date Picker Dialog
    if (showDatePicker) {
        DatePickerDialog(
            currentDate = selectedDate,
            onDateSelected = { date ->
                selectedDate = date
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }

    // Time Picker Dialog
    if (showTimePicker) {
        TimePickerDialog(
            currentTime = selectedTime,
            onTimeSelected = { time ->
                selectedTime = time
                showTimePicker = false
            },
            onDismiss = { showTimePicker = false }
        )
    }
}

/**
 * Update appointment button
 */
@Composable
private fun UpdateAppointmentButton(
    isEnabled: Boolean,
    isLoading: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = isEnabled && !isLoading,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp
            )
        } else {
            Icon(
                imageVector = Icons.Default.Save,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Randevuyu Güncelle",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// Reuse existing card components from AddAppointmentScreen
@Composable
private fun AppointmentCustomerCard(
    selectedCustomer: Customer?,
    onClick: () -> Unit,
    onAddNewCustomer: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = if (selectedCustomer != null) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Müşteri Seçin",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Row {
                    IconButton(onClick = onAddNewCustomer) {
                        Icon(
                            imageVector = Icons.Default.PersonAdd,
                            contentDescription = "Yeni Müşteri Ekle",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            if (selectedCustomer != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = selectedCustomer.fullName,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = selectedCustomer.phoneNumber,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun AppointmentServiceCard(
    selectedService: Service?,
    onClick: () -> Unit,
    onAddNewService: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = if (selectedService != null) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Hizmet Seçin",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Row {
                    IconButton(onClick = onAddNewService) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Yeni Hizmet Ekle",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            if (selectedService != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = selectedService.name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = selectedService.formattedPrice,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun AppointmentDateCard(
    selectedDate: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Randevu Tarihi",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = selectedDate.ifEmpty { "Tarih seçiniz" },
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (selectedDate.isEmpty()) 
                        MaterialTheme.colorScheme.onSurfaceVariant 
                    else MaterialTheme.colorScheme.onSurface
                )
            }
            Icon(
                imageVector = Icons.Default.DateRange,
                contentDescription = "Tarih seç",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun AppointmentTimeCard(
    selectedTime: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Randevu Saati",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = selectedTime.ifEmpty { "Saat seçiniz" },
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (selectedTime.isEmpty()) 
                        MaterialTheme.colorScheme.onSurfaceVariant 
                    else MaterialTheme.colorScheme.onSurface
                )
            }
            Icon(
                imageVector = Icons.Default.AccessTime,
                contentDescription = "Saat seç",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun AppointmentNotesCard(
    notes: String,
    onNotesChanged: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Notes,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Notlar (Opsiyonel)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            OutlinedTextField(
                value = notes,
                onValueChange = onNotesChanged,
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text("Randevu ile ilgili notlarınızı yazabilirsiniz...")
                },
                minLines = 3,
                maxLines = 5,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                ),
                shape = RoundedCornerShape(8.dp)
            )
        }
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
                text = "Müşteri Seçin",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(customers) { customer ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(),
                        onClick = { onCustomerSelected(customer) },
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            Text(
                                text = customer.fullName,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = customer.phoneNumber,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
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
                text = "Hizmet Seçin",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(services) { service ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(),
                        onClick = { onServiceSelected(service) },
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            Text(
                                text = service.name,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = service.formattedPrice,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
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

@Composable
private fun TimePickerDialog(
    currentTime: String,
    onTimeSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedTime by remember { mutableStateOf(currentTime) }
    var timeExpanded by remember { mutableStateOf(false) }
    
    // Generate time options from 08:00 to 20:00 with 30-minute intervals
    val timeOptions = remember {
        val times = mutableListOf<String>()
        for (hour in 8..20) {
            for (minute in arrayOf(0, 30)) {
                if (hour == 20 && minute == 30) break // Stop at 20:00
                times.add(String.format("%02d:%02d", hour, minute))
            }
        }
        times
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
                    text = "Saat seçiniz:",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                ExposedDropdownMenuBox(
                    expanded = timeExpanded,
                    onExpandedChange = { timeExpanded = !timeExpanded }
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