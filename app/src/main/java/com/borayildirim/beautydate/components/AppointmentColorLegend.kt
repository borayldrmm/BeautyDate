package com.borayildirim.beautydate.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.borayildirim.beautydate.data.models.AppointmentStatus

/**
 * Color legend for appointment statuses
 * Shows color-coded dots with status names for user reference
 * Memory efficient: static component with predefined status colors
 */
@Composable
fun AppointmentColorLegend(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = "Durum Renkleri",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Planned (Blue)
                LegendItem(
                    color = Color(0xFF1976D2),
                    text = "Planlanan",
                    modifier = Modifier.weight(1f)
                )
                
                // Completed (Green)
                LegendItem(
                    color = Color(0xFF388E3C),
                    text = "Tamamlanan",
                    modifier = Modifier.weight(1f)
                )
                
                // Cancelled (Red)
                LegendItem(
                    color = Color(0xFFD32F2F),
                    text = "İptal Edilen",
                    modifier = Modifier.weight(1f)
                )
                
                // No Show (Yellow-Orange)
                LegendItem(
                    color = Color(0xFFFF8F00),
                    text = "Gelmedi",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * Individual legend item with colored dot and text
 */
@Composable
private fun LegendItem(
    color: Color,
    text: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        // Colored dot
        Card(
            modifier = Modifier.size(8.dp),
            shape = RoundedCornerShape(4.dp),
            colors = CardDefaults.cardColors(
                containerColor = color
            )
        ) {}
        
        Spacer(modifier = Modifier.width(4.dp))
        
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
} 