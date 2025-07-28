package com.borayildirim.beautydate.screens.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.borayildirim.beautydate.data.repository.PriceUpdateType

/**
 * Bottom sheet for bulk price updates
 * Provides percentage and fixed amount options
 * Memory efficient: single composable with minimal state
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BulkPriceUpdateSheet(
    onUpdatePrices: (PriceUpdateType, Double) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedUpdateType by remember { mutableStateOf(PriceUpdateType.PERCENTAGE_INCREASE) }
    var updateValue by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Text(
                text = "Toplu Fiyat Güncelleme",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            // Update type selection
            Text(
                text = "Güncelleme Türü",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            // Percentage options
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (selectedUpdateType == PriceUpdateType.PERCENTAGE_INCREASE) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    }
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedUpdateType = PriceUpdateType.PERCENTAGE_INCREASE }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.TrendingUp,
                        contentDescription = null,
                        tint = if (selectedUpdateType == PriceUpdateType.PERCENTAGE_INCREASE) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Fiyat Artışı (%)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    if (selectedUpdateType == PriceUpdateType.PERCENTAGE_INCREASE) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (selectedUpdateType == PriceUpdateType.PERCENTAGE_DECREASE) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    }
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedUpdateType = PriceUpdateType.PERCENTAGE_DECREASE }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.TrendingDown,
                        contentDescription = null,
                        tint = if (selectedUpdateType == PriceUpdateType.PERCENTAGE_DECREASE) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Fiyat İndirimi (%)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    if (selectedUpdateType == PriceUpdateType.PERCENTAGE_DECREASE) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Fixed amount options
            Text(
                text = "Sabit Miktar",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (selectedUpdateType == PriceUpdateType.FIXED_AMOUNT_ADD) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    }
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedUpdateType = PriceUpdateType.FIXED_AMOUNT_ADD }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = if (selectedUpdateType == PriceUpdateType.FIXED_AMOUNT_ADD) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Sabit Miktar Ekle (₺)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    if (selectedUpdateType == PriceUpdateType.FIXED_AMOUNT_ADD) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (selectedUpdateType == PriceUpdateType.FIXED_AMOUNT_SUBTRACT) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    }
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedUpdateType = PriceUpdateType.FIXED_AMOUNT_SUBTRACT }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = null,
                        tint = if (selectedUpdateType == PriceUpdateType.FIXED_AMOUNT_SUBTRACT) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Sabit Miktar Çıkar (₺)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    if (selectedUpdateType == PriceUpdateType.FIXED_AMOUNT_SUBTRACT) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Value input
            OutlinedTextField(
                value = updateValue,
                onValueChange = { updateValue = it },
                label = {
                    Text(
                        when (selectedUpdateType) {
                            PriceUpdateType.PERCENTAGE_INCREASE -> "Artış Yüzdesi (%)"
                            PriceUpdateType.PERCENTAGE_DECREASE -> "İndirim Yüzdesi (%)"
                            PriceUpdateType.FIXED_AMOUNT_ADD -> "Eklenecek Miktar (₺)"
                            PriceUpdateType.FIXED_AMOUNT_SUBTRACT -> "Çıkarılacak Miktar (₺)"
                            PriceUpdateType.SET_EXACT_PRICE -> "Yeni Fiyat (₺)"
                            PriceUpdateType.ROUND_PRICES -> "Yuvarla (50'nin katları)"
                        }
                    )
                },
                placeholder = {
                    Text(
                        when (selectedUpdateType) {
                            PriceUpdateType.PERCENTAGE_INCREASE -> "10"
                            PriceUpdateType.PERCENTAGE_DECREASE -> "5"
                            PriceUpdateType.FIXED_AMOUNT_ADD -> "50"
                            PriceUpdateType.FIXED_AMOUNT_SUBTRACT -> "25"
                            PriceUpdateType.SET_EXACT_PRICE -> "200"
                            PriceUpdateType.ROUND_PRICES -> "Otomatik"
                        }
                    )
                },
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("İptal")
                }
                
                Button(
                    onClick = {
                        val value = updateValue.toDoubleOrNull()
                        if (value != null && value > 0) {
                            isLoading = true
                            onUpdatePrices(selectedUpdateType, value)
                        }
                    },
                    enabled = updateValue.isNotBlank() && updateValue.toDoubleOrNull() != null && !isLoading,
                    modifier = Modifier.weight(1f)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Güncelle")
                    }
                }
            }
        }
    }
}

/**
 * Overloaded version for ServiceViewModel integration
 * Maps actionType to PriceUpdateType and handles ServiceViewModel calls
 */
@Composable
fun BulkPriceUpdateSheet(
    onDismiss: () -> Unit,
    serviceViewModel: com.borayildirim.beautydate.viewmodels.ServiceViewModel,
    actionType: String
) {
    // Map actionType to initial PriceUpdateType
    val initialUpdateType = when (actionType) {
        "increase_percentage" -> PriceUpdateType.PERCENTAGE_INCREASE
        "decrease_percentage" -> PriceUpdateType.PERCENTAGE_DECREASE
        "set_fixed_amount" -> PriceUpdateType.SET_EXACT_PRICE
        "round_prices" -> PriceUpdateType.ROUND_PRICES
        else -> PriceUpdateType.PERCENTAGE_INCREASE
    }
    
    // For round_prices, directly execute without showing value input
    if (actionType == "round_prices") {
        LaunchedEffect(Unit) {
            serviceViewModel.bulkUpdatePrices(PriceUpdateType.ROUND_PRICES, 0.0, category = null, serviceIds = emptyList())
            onDismiss()
        }
        return
    }
    
    BulkPriceUpdateSheet(
        onUpdatePrices = { updateType, value ->
            // Call ServiceViewModel's bulk update method
            serviceViewModel.bulkUpdatePrices(updateType, value, category = null, serviceIds = emptyList())
            onDismiss()
        },
        onDismiss = onDismiss
    )
}