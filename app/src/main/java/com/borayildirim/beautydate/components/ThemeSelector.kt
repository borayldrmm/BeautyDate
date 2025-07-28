package com.borayildirim.beautydate.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.borayildirim.beautydate.data.local.ThemeMode

/**
 * Theme selector component
 * Allows user to choose between Light and Dark themes
 */
@Composable
fun ThemeSelector(
    currentTheme: ThemeMode,
    onThemeSelected: (ThemeMode) -> Unit,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Tema Seçimi",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Column(
                modifier = Modifier.selectableGroup()
            ) {
                // Light Theme Option
                ThemeOption(
                    themeMode = ThemeMode.LIGHT,
                    title = "Açık Tema",
                    description = "Beyaz arka plan ile açık tema",
                    icon = Icons.Default.LightMode,
                    isSelected = currentTheme == ThemeMode.LIGHT,
                    isEnabled = !isLoading,
                    onSelect = { onThemeSelected(ThemeMode.LIGHT) }
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Dark Theme Option
                ThemeOption(
                    themeMode = ThemeMode.DARK,
                    title = "Koyu Tema",
                    description = "Siyah arka plan ile koyu tema",
                    icon = Icons.Default.DarkMode,
                    isSelected = currentTheme == ThemeMode.DARK,
                    isEnabled = !isLoading,
                    onSelect = { onThemeSelected(ThemeMode.DARK) }
                )
            }
            
            if (isLoading) {
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Tema değiştiriliyor...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Individual theme option component
 */
@Composable
private fun ThemeOption(
    themeMode: ThemeMode,
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    isEnabled: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .selectable(
                selected = isSelected,
                enabled = isEnabled,
                role = Role.RadioButton,
                onClick = onSelect
            )
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = null,
            enabled = isEnabled
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Icon(
            imageVector = icon,
            contentDescription = title,
            modifier = Modifier.size(24.dp),
            tint = if (isEnabled) {
                MaterialTheme.colorScheme.onSurfaceVariant
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            }
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = if (isEnabled) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                }
            )
            
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = if (isEnabled) {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                }
            )
        }
    }
} 