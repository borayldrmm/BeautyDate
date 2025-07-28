package com.borayildirim.beautydate.components

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

/**
 * Multi-action FAB component for service operations
 * Uses menu icon as main FAB with expandable options for add service and price update
 * Memory efficient: Following the same pattern as CustomerFab with service-specific actions
 */
@Composable
fun ServiceFab(
    onAddServiceClick: () -> Unit,
    onPriceUpdateClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Sub FABs (appear when expanded)
        AnimatedVisibility(
            visible = isExpanded,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Price Update FAB
                SubFab(
                    icon = Icons.AutoMirrored.Filled.TrendingUp,
                    contentDescription = "Fiyat Güncelle",
                    onClick = {
                        onPriceUpdateClick()
                        isExpanded = false
                    },
                    label = "Fiyat Güncelle"
                )
                
                // Add Service FAB
                SubFab(
                    icon = Icons.Default.Add,
                    contentDescription = "Yeni Hizmet",
                    onClick = {
                        onAddServiceClick()
                        isExpanded = false
                    },
                    label = "Yeni Hizmet"
                )
            }
        }
        
        // Main Menu FAB (3-line menu icon)
        FloatingActionButton(
            onClick = { isExpanded = !isExpanded },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            AnimatedContent(
                targetState = isExpanded,
                transitionSpec = {
                    (fadeIn() + scaleIn()) togetherWith (fadeOut() + scaleOut())
                },
                label = "menu_icon"
            ) { expanded ->
                Icon(
                    imageVector = if (expanded) Icons.Default.Close else Icons.Default.Menu,
                    contentDescription = if (expanded) "Kapat" else "Menü"
                )
            }
        }
    }
}

/**
 * Sub FAB component for service menu items
 * Uses distinct colors for better visibility against background
 */
@Composable
private fun SubFab(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Label with enhanced visibility
        Surface(
            color = MaterialTheme.colorScheme.tertiaryContainer,
            shape = MaterialTheme.shapes.small,
            shadowElevation = 4.dp
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
            )
        }
        
        // Small FAB with distinct colors
        SmallFloatingActionButton(
            onClick = onClick,
            containerColor = MaterialTheme.colorScheme.tertiary,
            contentColor = MaterialTheme.colorScheme.onTertiary,
            elevation = FloatingActionButtonDefaults.elevation(
                defaultElevation = 6.dp,
                pressedElevation = 8.dp
            )
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription
            )
        }
    }
} 