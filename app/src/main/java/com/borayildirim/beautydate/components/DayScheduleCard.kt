package com.borayildirim.beautydate.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.borayildirim.beautydate.data.models.DayHours
import com.borayildirim.beautydate.data.models.DayOfWeek

/**
 * Card component for displaying day schedule
 * Material Design 3 styled card with working hours information
 * Memory efficient: minimal state, computed properties
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayScheduleCard(
    dayOfWeek: DayOfWeek,
    dayHours: DayHours,
    onEditClick: () -> Unit,
    onToggleOpen: () -> Unit,
    modifier: Modifier = Modifier,
    isEditing: Boolean = false,
    isLoading: Boolean = false
) {
    // Dynamic colors based on working status
    val containerColor = if (dayHours.isWorking) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.secondaryContainer
    }
    
    val contentColor = if (dayHours.isWorking) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSecondaryContainer
    }

    Card(
        onClick = onToggleOpen,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isEditing) 8.dp else 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left side: Day and status
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Status icon
                Icon(
                    imageVector = if (dayHours.isWorking) Icons.Default.Schedule else Icons.Default.EventBusy,
                    contentDescription = if (dayHours.isWorking) "Açık" else "Kapalı",
                    tint = contentColor,
                    modifier = Modifier.size(24.dp)
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column {
                    // Day name
                    Text(
                        text = dayOfWeek.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = contentColor
                    )
                    
                    // Working hours or status
                    Text(
                        text = dayHours.getDisplayText(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = contentColor.copy(alpha = 0.8f)
                    )
                }
            }
            
            // Right side: Status badge and edit button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Working status badge
                WorkingStatusBadge(
                    isWorking = dayHours.isWorking,
                    contentColor = contentColor
                )
                
                // Edit button
                if (!isLoading) {
                    IconButton(
                        onClick = onEditClick,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Düzenle",
                            tint = contentColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                } else {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = contentColor,
                        strokeWidth = 2.dp
                    )
                }
            }
        }
    }
}

/**
 * Badge component for working status
 * Memory efficient: minimal composable with computed colors
 */
@Composable
private fun WorkingStatusBadge(
    isWorking: Boolean,
    contentColor: Color
) {
    val badgeColor = if (isWorking) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outline
    }
    
    val badgeText = if (isWorking) "Açık" else "Kapalı"
    
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = badgeColor.copy(alpha = 0.2f),
        modifier = Modifier.padding(0.dp)
    ) {
        Text(
            text = badgeText,
            style = MaterialTheme.typography.labelSmall,
            color = badgeColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Compact version of day schedule card for lists
 * Memory efficient: reduced padding and simplified layout
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayScheduleCardCompact(
    dayOfWeek: DayOfWeek,
    dayHours: DayHours,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false
) {
    val containerColor = when {
        isSelected -> MaterialTheme.colorScheme.primaryContainer
        dayHours.isWorking -> MaterialTheme.colorScheme.surface
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    
    Card(
        onClick = onEditClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = dayOfWeek.shortName,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                text = if (dayHours.isWorking) {
                    "${dayHours.startTime}-${dayHours.endTime}"
                } else {
                    "Kapalı"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
} 