package com.borayildirim.beautydate.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.borayildirim.beautydate.data.models.Appointment
import com.borayildirim.beautydate.data.models.AppointmentStatus

/**
 * Modern appointment detail bottom sheet
 * Displays comprehensive appointment information with action buttons
 * Material Design 3 compliant with clean layout
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentDetailBottomSheet(
    appointment: Appointment,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header
            Text(
                text = "Randevu Detayları",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            
            // Customer Information Section
            DetailSection(
                title = "Müşteri Bilgileri",
                icon = Icons.Default.Person
            ) {
                DetailItem(
                    icon = Icons.Default.Person,
                    label = "Ad Soyad",
                    value = appointment.customerName
                )
                DetailItem(
                    icon = Icons.Default.Phone,
                    label = "Telefon",
                    value = appointment.customerPhone
                )
            }
            
            // Service Information Section
            DetailSection(
                title = "Hizmet Bilgileri",
                icon = Icons.Default.ContentCut
            ) {
                DetailItem(
                    icon = Icons.Default.ContentCut,
                    label = "Hizmet",
                    value = appointment.serviceName
                )
                DetailItem(
                    icon = Icons.Default.AttachMoney,
                    label = "Ücret",
                    value = appointment.formattedPrice
                )
            }
            
            // Appointment Information Section
            DetailSection(
                title = "Randevu Bilgileri",
                icon = Icons.Default.CalendarToday
            ) {
                DetailItem(
                    icon = Icons.Default.DateRange,
                    label = "Tarih",
                    value = appointment.appointmentDate
                )
                DetailItem(
                    icon = Icons.Default.Schedule,
                    label = "Saat",
                    value = appointment.appointmentTime
                )
                DetailItem(
                    icon = Icons.Default.Info,
                    label = "Durum",
                    value = appointment.status.getDisplayName()
                )
            }
            
            // Notes Section (if available)
            if (appointment.notes.isNotBlank()) {
                DetailSection(
                    title = "Notlar",
                    icon = Icons.Default.Notes
                ) {
                    Text(
                        text = appointment.notes,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Close Button
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Tamam")
                }
                
                // Edit Button - Only show for SCHEDULED appointments
                if (appointment.status == AppointmentStatus.SCHEDULED) {
                    Button(
                        onClick = {
                            onEdit()
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Düzenle")
                    }
                }
            }
            
            // Bottom spacing for gesture handling
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * Section container for grouped information
 */
@Composable
private fun DetailSection(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Section Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            // Section Content
            content()
        }
    }
}

/**
 * Individual detail item with icon, label and value
 */
@Composable
private fun DetailItem(
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
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
        )
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
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