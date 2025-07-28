package com.borayildirim.beautydate.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp

/**
 * Floating action button for adding appointments
 * Material3 design with loading state animation
 */
@Composable
fun AppointmentFab(
    onClick: () -> Unit,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (isLoading) 0.9f else 1.0f,
        label = "FabScale"
    )
    
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier.scale(scale),
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 2.dp
            )
        } else {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Randevu Ekle"
            )
        }
    }
} 