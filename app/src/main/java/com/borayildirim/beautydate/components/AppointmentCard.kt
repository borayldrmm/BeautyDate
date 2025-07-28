package com.borayildirim.beautydate.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.borayildirim.beautydate.data.models.Appointment
import com.borayildirim.beautydate.data.models.AppointmentStatus

/**
 * Gets the status color based on appointment status
 * Color coding: Blue (SCHEDULED), Green (COMPLETED), Red (CANCELLED), Yellow-Orange (NO_SHOW)
 */
private fun getStatusColor(status: AppointmentStatus): Color {
    return when (status) {
        AppointmentStatus.SCHEDULED -> Color(0xFF1976D2)    // Blue
        AppointmentStatus.COMPLETED -> Color(0xFF388E3C)    // Green  
        AppointmentStatus.CANCELLED -> Color(0xFFD32F2F)    // Red
        AppointmentStatus.NO_SHOW -> Color(0xFFFF8F00)      // Yellow-Orange
    }
}

/**
 * Gets the status border color for modern card design
 * Uses full opacity colors for clean visual separation
 */
private fun getStatusBorderColor(status: AppointmentStatus): Color {
    return when (status) {
        AppointmentStatus.SCHEDULED -> Color(0xFF1976D2)    // Blue
        AppointmentStatus.COMPLETED -> Color(0xFF388E3C)    // Green
        AppointmentStatus.CANCELLED -> Color(0xFFD32F2F)    // Red
        AppointmentStatus.NO_SHOW -> Color(0xFFFF8F00)      // Yellow-Orange
    }
}

/**
 * Modern Material3 appointment card component with clean design
 * Displays appointment information in a modern card layout with action menu
 * Layout: Customer info, phone, service with price, date/time, and action menu
 * Memory efficient: reusable component with conditional rendering
 * Design: Neutral background with colored border based on appointment status
 * Status indicator: Colored dot and matching border for clear visual hierarchy
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentCard(
    appointment: Appointment,
    onClick: () -> Unit,
    onStatusUpdate: (AppointmentStatus) -> Unit,
    onDelete: () -> Unit,
    onPaymentMethodRequired: (Appointment) -> Unit = {}, // New parameter for payment method
    isLoading: Boolean = false,
    modifier: Modifier = Modifier
) {
    var showActionMenu by remember { mutableStateOf(false) }
    var showCancelDialog by remember { mutableStateOf(false) }
    var showNoShowDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    Card(
        onClick = onClick,
        modifier = modifier
            .border(
                width = 2.dp,
                color = getStatusBorderColor(appointment.status),
                shape = RoundedCornerShape(12.dp)
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 4.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Row 1: 👤 Customer Name with Status Indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Status indicator dot - Larger and more prominent
                Card(
                    modifier = Modifier.size(16.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = getStatusColor(appointment.status)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {}
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = appointment.customerName,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                
                // Action Menu (⋮) with Box as anchor
                Box {
                    IconButton(
                        onClick = { showActionMenu = true }
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Aksiyon Menüsü",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    // Action Menu Dropdown positioned relative to this Box
                    AppointmentActionMenu(
                        expanded = showActionMenu,
                        onDismiss = { showActionMenu = false },
                        onCompleted = { 
                            showActionMenu = false
                            // Show payment method selection for completed appointments
                            onPaymentMethodRequired(appointment)
                        },
                        onCancelled = { 
                            showActionMenu = false
                            showCancelDialog = true
                        },
                        onNoShow = { 
                            showActionMenu = false
                            showNoShowDialog = true
                        },
                        onEdit = { 
                            showActionMenu = false
                            onClick() // Navigate to edit screen
                        }
                    )
                }
            }
            
            // Row 2: 📞 Phone Number
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = appointment.customerPhone,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            // Row 3: ✂️ Service Name and Price
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCut, // ✂️ Scissors icon for beauty services
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${appointment.serviceName} - ${appointment.formattedPrice}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            // Row 4: 📅 Date and Time
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${appointment.appointmentDate} - ${appointment.appointmentTime}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
    
    // Cancel Confirmation Dialog
    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { 
                Text(
                    text = "Randevu İptal",
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = { 
                Text(
                    text = "Geçerli randevunun iptal etme işlemini onaylıyor musunuz?",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onStatusUpdate(AppointmentStatus.CANCELLED)
                        showCancelDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("İptal Et")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) {
                    Text("Vazgeç")
                }
            }
        )
    }
    
    // No Show Confirmation Dialog
    if (showNoShowDialog) {
        AlertDialog(
            onDismissRequest = { showNoShowDialog = false },
            title = { 
                Text(
                    text = "Gelmedi İşaretleme",
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = { 
                Text(
                    text = "Geçerli randevuyu \"Gelmedi\" olarak işaretlemeyi onaylıyor musunuz?",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onStatusUpdate(AppointmentStatus.NO_SHOW)
                        showNoShowDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color(0xFFFF9800) // Orange
                    )
                ) {
                    Text("Gelmedi")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNoShowDialog = false }) {
                    Text("Vazgeç")
                }
            }
        )
    }
    
    // Delete Confirmation Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { 
                Text(
                    text = "Randevuyu Sil",
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = { 
                Text(
                    text = "Bu randevuyu silmek istediğinizden emin misiniz? Bu işlem geri alınamaz.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
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
 * Action menu dropdown for appointment operations
 */
@Composable
private fun AppointmentActionMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    onCompleted: () -> Unit,
    onCancelled: () -> Unit,
    onNoShow: () -> Unit,
    onEdit: () -> Unit
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss
        // Removed offset since we're using Box anchoring
    ) {
        // Tamamlandı
        DropdownMenuItem(
            text = { 
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF4CAF50) // Green
                    )
                    Text("Tamamlandı")
                }
            },
            onClick = onCompleted
        )
        
        // İptal Et
        DropdownMenuItem(
            text = { 
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Cancel,
                        contentDescription = null,
                        tint = Color(0xFFE91E63) // Pink
                    )
                    Text("İptal Et")
                }
            },
            onClick = onCancelled
        )
        
        // Gelmedi
        DropdownMenuItem(
            text = { 
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PersonOff,
                        contentDescription = null,
                        tint = Color(0xFFFF9800) // Orange
                    )
                    Text("Gelmedi")
                }
            },
            onClick = onNoShow
        )
        
        HorizontalDivider()
        
        // Düzenle
        DropdownMenuItem(
            text = { 
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text("Düzenle")
                }
            },
            onClick = onEdit
        )
        
        HorizontalDivider()
    }
} 