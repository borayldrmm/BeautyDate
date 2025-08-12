package com.borayildirim.beautydate.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.borayildirim.beautydate.data.models.Appointment
import com.borayildirim.beautydate.data.models.AppointmentStatus
import com.borayildirim.beautydate.viewmodels.AppointmentViewModel

/**
 * Appointment detail screen with comprehensive information display
 * Shows all appointment details with status management and actions
 * Material3 design with modern card layouts
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentDetailScreen(
    appointment: Appointment,
    onNavigateBack: () -> Unit,
    appointmentViewModel: AppointmentViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by appointmentViewModel.uiState.collectAsState()
    
    var showStatusMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    // Handle success and navigate back
    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage != null) {
            onNavigateBack()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Randevu Detayları",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
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
                actions = {
                    // Status menu button
                    IconButton(onClick = { showStatusMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Diğer İşlemler"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Status card
            StatusCard(
                status = appointment.status,
                onStatusMenuClick = { showStatusMenu = true }
            )
            
            // Customer information
            CustomerInfoCard(appointment = appointment)
            
            // Service information
            ServiceInfoCard(appointment = appointment)
            
            // Date and time information
            DateTimeCard(appointment = appointment)
            
            // Notes if available
            if (appointment.notes.isNotBlank()) {
                NotesCard(notes = appointment.notes)
            }
            
            // Action buttons for scheduled appointments
            if (appointment.status == AppointmentStatus.SCHEDULED) {
                ActionButtonsCard(
                    onComplete = {
                        appointmentViewModel.updateAppointmentStatus(
                            appointment.id,
                            AppointmentStatus.COMPLETED
                        )
                    },
                    onCancel = {
                        appointmentViewModel.updateAppointmentStatus(
                            appointment.id,
                            AppointmentStatus.CANCELLED
                        )
                    },
                    onNoShow = {
                        appointmentViewModel.updateAppointmentStatus(
                            appointment.id,
                            AppointmentStatus.NO_SHOW
                        )
                    },
                    onDelete = { showDeleteDialog = true },
                    isLoading = uiState.isLoading
                )
            }
            
            // Error message
            uiState.errorMessage?.let { message ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = message,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
    
    // Status menu dropdown
    if (showStatusMenu) {
        DropdownMenu(
            expanded = showStatusMenu,
            onDismissRequest = { showStatusMenu = false }
        ) {
            AppointmentStatus.values().forEach { status ->
                if (status != appointment.status) {
                    DropdownMenuItem(
                        text = { Text(status.getDisplayName()) },
                        onClick = {
                            appointmentViewModel.updateAppointmentStatus(appointment.id, status)
                            showStatusMenu = false
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = when (status) {
                                    AppointmentStatus.SCHEDULED -> Icons.Default.Schedule
                                    AppointmentStatus.COMPLETED -> Icons.Default.CheckCircle
                                    AppointmentStatus.CANCELLED -> Icons.Default.Cancel
                                    AppointmentStatus.NO_SHOW -> Icons.Default.PersonOff
                                },
                                contentDescription = null,
                                tint = Color(status.getColor())
                            )
                        }
                    )
                }
            }
            
            if (appointment.canBeCancelled()) {
                HorizontalDivider()
                DropdownMenuItem(
                    text = { Text("Sil", color = MaterialTheme.colorScheme.error) },
                    onClick = {
                        showDeleteDialog = true
                        showStatusMenu = false
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                )
            }
        }
    }
    
    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Randevuyu Sil") },
            text = { Text("Bu randevuyu silmek istediğinizden emin misiniz? Bu işlem geri alınamaz.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        appointmentViewModel.deleteAppointment(appointment.id)
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Sil")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("İptal")
                }
            }
        )
    }
}

/**
 * Status display card
 */
@Composable
private fun StatusCard(
    status: AppointmentStatus,
    onStatusMenuClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color(status.getColor()).copy(alpha = 0.1f)
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
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = when (status) {
                        AppointmentStatus.SCHEDULED -> Icons.Default.Schedule
                        AppointmentStatus.COMPLETED -> Icons.Default.CheckCircle
                        AppointmentStatus.CANCELLED -> Icons.Default.Cancel
                        AppointmentStatus.NO_SHOW -> Icons.Default.PersonOff
                    },
                    contentDescription = null,
                    tint = Color(status.getColor()),
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Durum",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = status.getDisplayName(),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color(status.getColor())
                    )
                }
            }
            
            IconButton(onClick = onStatusMenuClick) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Durumu Değiştir",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

/**
 * Customer information card
 */
@Composable
private fun CustomerInfoCard(appointment: Appointment) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Müşteri Bilgileri",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            InfoRow(
                icon = Icons.Default.Person,
                label = "Ad Soyad",
                value = appointment.customerName
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            InfoRow(
                icon = Icons.Default.Phone,
                label = "Telefon",
                value = appointment.customerPhone
            )
        }
    }
}

/**
 * Service information card
 */
@Composable
private fun ServiceInfoCard(appointment: Appointment) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Hizmet Bilgileri",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            InfoRow(
                icon = Icons.Default.MedicalServices,
                label = "Hizmet",
                value = appointment.serviceName
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            InfoRow(
                icon = Icons.Default.AttachMoney,
                label = "Fiyat",
                value = appointment.formattedPrice
            )
        }
    }
}

/**
 * Date and time card
 */
@Composable
private fun DateTimeCard(appointment: Appointment) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Tarih ve Saat",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            InfoRow(
                icon = Icons.Default.CalendarToday,
                label = "Tarih",
                value = appointment.appointmentDate
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            InfoRow(
                icon = Icons.Default.Schedule,
                label = "Saat",
                value = appointment.appointmentTime
            )
        }
    }
}

/**
 * Notes card
 */
@Composable
private fun NotesCard(notes: String) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Notlar",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = notes,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/**
 * Action buttons card for scheduled appointments
 */
@Composable
private fun ActionButtonsCard(
    onComplete: () -> Unit,
    onCancel: () -> Unit,
    onNoShow: () -> Unit,
    onDelete: () -> Unit,
    isLoading: Boolean
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "İşlemler",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Complete button
                Button(
                    onClick = onComplete,
                    modifier = Modifier.weight(1f),
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Tamamla", style = MaterialTheme.typography.labelSmall)
                }
                
                // Cancel button
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f),
                    enabled = !isLoading,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFFE91E63)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Cancel,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("İptal Et", style = MaterialTheme.typography.labelSmall)
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // No show button
                OutlinedButton(
                    onClick = onNoShow,
                    modifier = Modifier.weight(1f),
                    enabled = !isLoading,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFFFF9800)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.PersonOff,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Gelmedi", style = MaterialTheme.typography.labelSmall)
                }
                
                // Delete button
                OutlinedButton(
                    onClick = onDelete,
                    modifier = Modifier.weight(1f),
                    enabled = !isLoading,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Sil", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

/**
 * Info row component
 */
@Composable
private fun InfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
} 