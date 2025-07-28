package com.borayildirim.beautydate.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.borayildirim.beautydate.data.models.AppointmentStatus

/**
 * Filter chips component for appointment status filtering
 * Horizontal scrollable row with status counts
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentFilterChips(
    selectedStatus: AppointmentStatus?,
    onStatusSelected: (AppointmentStatus?) -> Unit,
    appointmentCounts: Map<AppointmentStatus, Int>,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        // All appointments chip (shows only SCHEDULED appointments)
        item {
            FilterChip(
                onClick = { onStatusSelected(null) },
                label = { 
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Tümü")
                        val scheduledCount = appointmentCounts[AppointmentStatus.SCHEDULED] ?: 0
                        if (scheduledCount > 0) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "($scheduledCount)",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                },
                selected = selectedStatus == null,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            )
        }
        
        // Status filter chips
        items(AppointmentStatus.values().toList()) { status ->
            val count = appointmentCounts[status] ?: 0
            FilterChip(
                onClick = { 
                    onStatusSelected(if (selectedStatus == status) null else status) 
                },
                label = { 
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(status.getDisplayName())
                        if (count > 0) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "($count)",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                },
                selected = selectedStatus == status,
                leadingIcon = {
                    Icon(
                        imageVector = when (status) {
                            AppointmentStatus.SCHEDULED -> Icons.Default.Schedule
                            AppointmentStatus.COMPLETED -> Icons.Default.CheckCircle
                            AppointmentStatus.CANCELLED -> Icons.Default.Cancel
                            AppointmentStatus.NO_SHOW -> Icons.Default.PersonOff
                        },
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = if (selectedStatus == status) {
                            Color.White
                        } else {
                            Color(status.getColor())
                        }
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(status.getColor()),
                    selectedLabelColor = Color.White,
                    selectedLeadingIconColor = Color.White
                )
            )
        }
    }
} 