package com.borayildirim.beautydate.screens.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Header section for "Diğer" screen
 * Shows business name and username with profile icon
 * Clickable to navigate to profile settings
 */
@Composable
fun DigerHeaderSection(
    businessName: String,
    username: String,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onProfileClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Profile icon
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = "Profil",
            modifier = Modifier
                .size(48.dp)
                .padding(end = 16.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        // Business name and username
        Column(
            modifier = Modifier.weight(1f)
        ) {
            // Business name - larger, more prominent
            Text(
                text = businessName.ifEmpty { "İşletme Adı" },
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            
            // Username - smaller, under business name
            Text(
                text = username.ifEmpty { "Kullanıcı" },
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Normal,
                    fontSize = 19.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
} 