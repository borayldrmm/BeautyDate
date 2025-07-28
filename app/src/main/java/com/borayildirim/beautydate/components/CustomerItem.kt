package com.borayildirim.beautydate.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Face3
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.borayildirim.beautydate.data.models.Customer
import com.borayildirim.beautydate.data.models.CustomerGender

/**
 * Enhanced customer item component with action buttons
 * Layout: [Gender Icon] | Name-Phone | [Edit Icon] [Delete Icon]
 * Memory efficient design with action callbacks
 */
@Composable
fun CustomerItem(
    customer: Customer,
    onClick: (Customer) -> Unit,
    onEditClick: ((Customer) -> Unit)? = null,
    onDeleteClick: ((Customer) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick(customer) },
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Gender Icon (ICON1)
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        color = when (customer.gender) {
                            CustomerGender.MALE -> Color(0xFF2196F3) // Blue
                            CustomerGender.FEMALE -> Color(0xFFE91E63) // Pink
                            CustomerGender.OTHER -> Color(0xFF9E9E9E) // Gray
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (customer.gender) {
                        CustomerGender.MALE -> Icons.Default.Face
                        CustomerGender.FEMALE -> Icons.Default.Face3
                        CustomerGender.OTHER -> Icons.Default.Face
                    },
                    contentDescription = customer.gender.getDisplayName(),
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
            }
            
            // Customer Info - Name and Phone
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                // Full Name
                Text(
                    text = "${customer.firstName} ${customer.lastName}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                // Phone Number
                Text(
                    text = customer.phoneNumber,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            // Action Icons Row
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Edit Icon (ICON2) - Orange/Amber
                if (onEditClick != null) {
                    IconButton(
                        onClick = { onEditClick(customer) },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFF8F00).copy(alpha = 0.1f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Düzenle",
                            tint = Color(0xFFFF8F00),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                
                // Delete Icon (ICON3) - Red
                if (onDeleteClick != null) {
                    IconButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFD32F2F).copy(alpha = 0.1f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Sil",
                            tint = Color(0xFFD32F2F),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
    
    // Delete confirmation dialog
    if (showDeleteDialog && onDeleteClick != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Müşteriyi Sil") },
            text = { 
                Text("${customer.firstName} ${customer.lastName} adlı müşteriyi silmek istediğinizden emin misiniz?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteClick(customer)
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFD32F2F)
                    )
                ) {
                    Text("Sil", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showDeleteDialog = false }
                ) {
                    Text("İptal")
                }
            }
        )
    }
} 