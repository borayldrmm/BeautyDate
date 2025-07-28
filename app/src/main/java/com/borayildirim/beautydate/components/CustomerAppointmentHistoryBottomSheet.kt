package com.borayildirim.beautydate.components

import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.borayildirim.beautydate.data.models.Appointment
import com.borayildirim.beautydate.data.models.AppointmentStatus
import com.borayildirim.beautydate.data.models.Customer

/**
 * Modern customer appointment history bottom sheet
 * Shows appointment history with status-based filtering using Material Design 3
 * Memory efficient: filtered lists and reusable components
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerAppointmentHistoryBottomSheet(
    customer: Customer,
    appointments: List<Appointment>,
    onDismiss: () -> Unit,
    onAppointmentClick: (Appointment) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val bottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    
    var selectedFilter by remember { mutableStateOf(AppointmentStatus.SCHEDULED) }
    
    // Filter appointments by status
    val filteredAppointments = remember(appointments, selectedFilter) {
        appointments.filter { it.status == selectedFilter }
    }
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = bottomSheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            // Header with customer info
            CustomerHistoryHeader(customer = customer)
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Filter tabs
            AppointmentStatusFilterTabs(
                selectedStatus = selectedFilter,
                onStatusSelected = { selectedFilter = it },
                appointmentCounts = appointments.groupingBy { it.status }.eachCount()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Appointments list
            AppointmentHistoryList(
                appointments = filteredAppointments,
                onAppointmentClick = onAppointmentClick,
                modifier = Modifier.weight(1f, fill = false)
            )
            
            // Bottom padding for navigation gestures
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * Customer header for bottom sheet
 */
@Composable
private fun CustomerHistoryHeader(
    customer: Customer,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Customer avatar
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(28.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = customer.fullName,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Randevu Geçmişi",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        // History icon
        Icon(
            imageVector = Icons.Default.History,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
    }
}

/**
 * Status filter tabs with appointment counts
 */
@Composable
private fun AppointmentStatusFilterTabs(
    selectedStatus: AppointmentStatus,
    onStatusSelected: (AppointmentStatus) -> Unit,
    appointmentCounts: Map<AppointmentStatus, Int>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Randevu Durumu",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        ScrollableTabRow(
            selectedTabIndex = AppointmentStatus.entries.indexOf(selectedStatus),
            containerColor = Color.Transparent,
            edgePadding = 0.dp,
            divider = {},
            indicator = {}
        ) {
            AppointmentStatus.entries.forEach { status ->
                val isSelected = status == selectedStatus
                val count = appointmentCounts[status] ?: 0
                
                FilterChip(
                    onClick = { onStatusSelected(status) },
                    label = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = getStatusIcon(status),
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = getStatusDisplayName(status),
                                style = MaterialTheme.typography.labelMedium
                            )
                            if (count > 0) {
                                Badge(
                                    containerColor = if (isSelected) 
                                        MaterialTheme.colorScheme.onPrimary 
                                    else MaterialTheme.colorScheme.primary
                                ) {
                                    Text(
                                        text = count.toString(),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isSelected) 
                                            MaterialTheme.colorScheme.primary 
                                        else MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                            }
                        }
                    },
                    selected = isSelected,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
        }
    }
}

/**
 * Appointment history list
 */
@Composable
private fun AppointmentHistoryList(
    appointments: List<Appointment>,
    onAppointmentClick: (Appointment) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.heightIn(max = 400.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        if (appointments.isEmpty()) {
            item {
                EmptyAppointmentsState()
            }
        } else {
            items(appointments) { appointment ->
                AppointmentHistoryCard(
                    appointment = appointment,
                    onClick = { onAppointmentClick(appointment) }
                )
            }
        }
    }
}

/**
 * Individual appointment history card
 */
@Composable
private fun AppointmentHistoryCard(
    appointment: Appointment,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header with service and status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = appointment.serviceName,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                StatusChip(status = appointment.status)
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Date and time
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = appointment.appointmentDate,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = appointment.appointmentTime,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Price and payment status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${appointment.servicePrice.toInt()} ₺",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
                
                if (appointment.notes.isNotBlank()) {
                    Icon(
                        imageVector = Icons.Default.Notes,
                        contentDescription = "Not var",
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

/**
 * Status chip component
 */
@Composable
private fun StatusChip(
    status: AppointmentStatus,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, textColor) = when (status) {
        AppointmentStatus.SCHEDULED -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        AppointmentStatus.COMPLETED -> Color(0xFF4CAF50).copy(alpha = 0.2f) to Color(0xFF4CAF50)
        AppointmentStatus.CANCELLED -> Color(0xFFF44336).copy(alpha = 0.2f) to Color(0xFFF44336)
        AppointmentStatus.NO_SHOW -> Color(0xFFFF9800).copy(alpha = 0.2f) to Color(0xFFFF9800)
    }
    
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor
    ) {
        Text(
            text = getStatusDisplayName(status),
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = textColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

/**
 * Empty state for appointments
 */
@Composable
private fun EmptyAppointmentsState(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.EventBusy,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(48.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Bu durumda randevu bulunamadı",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        
        Text(
            text = "Farklı bir filtre seçerek tekrar deneyin",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

/**
 * Helper functions for status display
 */
private fun getStatusDisplayName(status: AppointmentStatus): String {
    return when (status) {
        AppointmentStatus.SCHEDULED -> "Programlanan"
        AppointmentStatus.COMPLETED -> "Tamamlanan"
        AppointmentStatus.CANCELLED -> "İptal Edilen"
        AppointmentStatus.NO_SHOW -> "Gelmedi"
    }
}

private fun getStatusIcon(status: AppointmentStatus): ImageVector {
    return when (status) {
        AppointmentStatus.SCHEDULED -> Icons.Default.Schedule
        AppointmentStatus.COMPLETED -> Icons.Default.CheckCircle
        AppointmentStatus.CANCELLED -> Icons.Default.Cancel
        AppointmentStatus.NO_SHOW -> Icons.Default.PersonOff
    }
} 