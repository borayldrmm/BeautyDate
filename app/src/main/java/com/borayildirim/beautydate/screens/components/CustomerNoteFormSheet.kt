package com.borayildirim.beautydate.screens.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.borayildirim.beautydate.data.models.Customer

/**
 * Bottom sheet for creating/editing customer notes
 * Material Design 3 compliant with customer selection
 * Memory efficient: optimized for form state management
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerNoteFormSheet(
    title: String,
    customers: List<Customer>,
    selectedCustomer: Customer?,
    noteTitle: String,
    noteContent: String,
    isImportant: Boolean,
    isLoading: Boolean,
    onCustomerSelected: (Customer) -> Unit,
    onTitleChanged: (String) -> Unit,
    onContentChanged: (String) -> Unit,
    onImportantChanged: (Boolean) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
    isEditMode: Boolean = false,
    modifier: Modifier = Modifier
) {
    var showCustomerSelection by remember { mutableStateOf(false) }
    var customerSearchQuery by remember { mutableStateOf("") }
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            
            // Customer selection (disabled in edit mode)
            if (!isEditMode) {
                Text(
                    text = "Müşteri Seçin",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                OutlinedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showCustomerSelection = true },
                    colors = CardDefaults.outlinedCardColors(
                        containerColor = if (selectedCustomer != null) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surface
                        }
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (selectedCustomer != null) {
                            Column {
                                Text(
                                    text = selectedCustomer.fullName,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = selectedCustomer.phoneNumber,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                )
                            }
                        } else {
                            Text(
                                text = "Müşteri seçiniz",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else if (selectedCustomer != null) {
                // Show selected customer info in edit mode
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.outlinedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Column {
                            Text(
                                text = selectedCustomer.fullName,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = selectedCustomer.phoneNumber,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
            
            // Note title
            OutlinedTextField(
                value = noteTitle,
                onValueChange = onTitleChanged,
                label = { Text("Not Başlığı") },
                placeholder = { Text("Örn: Özel istek, alerji bilgisi...") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Title,
                        contentDescription = null
                    )
                }
            )
            
            // Note content
            OutlinedTextField(
                value = noteContent,
                onValueChange = onContentChanged,
                label = { Text("Not İçeriği") },
                placeholder = { Text("Detaylı açıklama yazınız...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                shape = RoundedCornerShape(12.dp),
                minLines = 4,
                maxLines = 6,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = null,
                        modifier = Modifier.offset(y = (-40).dp) // Align with top
                    )
                }
            )
            
            // Important toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = if (isImportant) Color(0xFFFF9800) else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Önemli Not",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                Switch(
                    checked = isImportant,
                    onCheckedChange = onImportantChanged,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFFFF9800),
                        checkedTrackColor = Color(0xFFFF9800).copy(alpha = 0.3f)
                    )
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    enabled = !isLoading
                ) {
                    Text("İptal")
                }
                
                Button(
                    onClick = onSave,
                    modifier = Modifier.weight(1f),
                    enabled = !isLoading && selectedCustomer != null && 
                             noteTitle.isNotBlank() && noteContent.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text(if (isEditMode) "Güncelle" else "Kaydet")
                    }
                }
            }
        }
    }
    
    // Customer selection sheet
    if (showCustomerSelection) {
        ModalBottomSheet(
            onDismissRequest = { showCustomerSelection = false },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Müşteri Seçin",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                // Search field
                OutlinedTextField(
                    value = customerSearchQuery,
                    onValueChange = { customerSearchQuery = it },
                    label = { Text("Müşteri Ara") },
                    placeholder = { Text("İsim veya telefon numarası") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null
                        )
                    },
                    trailingIcon = {
                        if (customerSearchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = { customerSearchQuery = "" }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Temizle"
                                )
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                
                // Customer list
                LazyColumn(
                    modifier = Modifier.height(300.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val filteredCustomers = customers.filter { customer ->
                        customerSearchQuery.isBlank() || 
                        customer.fullName.contains(customerSearchQuery, ignoreCase = true) ||
                        customer.phoneNumber.contains(customerSearchQuery)
                    }
                    
                    items(
                        items = filteredCustomers,
                        key = { customer -> customer.id }
                    ) { customer ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onCustomerSelected(customer)
                                    showCustomerSelection = false
                                    customerSearchQuery = ""
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                
                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = customer.fullName,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Medium
                                        ),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = customer.phoneNumber,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                }
                                
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                    
                    if (filteredCustomers.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Müşteri bulunamadı",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
} 