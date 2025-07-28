package com.borayildirim.beautydate.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.borayildirim.beautydate.domain.models.OtherMenuItem as MenuItem

/**
 * Reusable menu item component for Other screen
 * Supports different states: enabled, disabled, special (logout)
 */
@Composable
fun OtherMenuItem(
    item: MenuItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = item.isEnabled) { 
                if (item.isEnabled) onClick() 
            },
        colors = CardDefaults.cardColors(
            containerColor = when {
                item.isSpecial -> Color(0xFFD32F2F) // Fixed red color for both themes
                !item.isEnabled -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                else -> MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (item.isEnabled) 2.dp else 0.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Icon(
                imageVector = item.icon,
                contentDescription = item.title,
                modifier = Modifier.size(32.dp),
                tint = when {
                    item.isSpecial -> Color.White // Fixed white color for red background
                    !item.isEnabled -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    else -> MaterialTheme.colorScheme.primary
                }
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Title
            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = if (item.isSpecial) FontWeight.Bold else FontWeight.Medium
                ),
                color = when {
                    item.isSpecial -> Color.White // Fixed white color for red background
                    !item.isEnabled -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    else -> MaterialTheme.colorScheme.onSurface
                },
                modifier = Modifier.weight(1f)
            )
            
            // Coming soon indicator for disabled items
            if (!item.isEnabled && !item.isSpecial) {
                Text(
                    text = "Çok Yakında",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            
            // Chevron for enabled items (except logout)
            if (item.isEnabled && !item.isSpecial) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Navigate",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Special logout button component
 * Full-width button with warning styling
 */
@Composable
fun LogoutButton(
    onClick: () -> Unit,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        enabled = !isLoading,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFD32F2F), // Fixed red color for both themes
            contentColor = Color.White // Fixed white color for red background
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 4.dp
        )
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = Color.White, // Fixed white color for red background
                strokeWidth = 2.dp
            )
            Spacer(modifier = Modifier.width(8.dp))
        } else {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        
        Text(
            text = if (isLoading) "Çıkış yapılıyor..." else "Çıkış Yap",
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Bold
            )
        )
    }
} 