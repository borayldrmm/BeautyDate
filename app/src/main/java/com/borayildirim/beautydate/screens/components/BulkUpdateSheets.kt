package com.borayildirim.beautydate.screens.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.borayildirim.beautydate.data.models.Employee
import com.borayildirim.beautydate.data.models.EmployeePermission

/**
 * Bottom sheet for bulk permission updates
 * Allows updating permissions for multiple selected employees
 * Memory efficient: reusable component with minimal state
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BulkPermissionUpdateSheet(
    selectedEmployees: Set<String>,
    employees: List<Employee>,
    onDismiss: () -> Unit,
    onUpdatePermissions: (List<EmployeePermission>) -> Unit
) {
    var selectedPermissions by remember { mutableStateOf(emptySet<EmployeePermission>()) }
    
    val selectedEmployeeList = employees.filter { it.id in selectedEmployees }
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(),
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Yetki Güncelleme",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Kapat"
                    )
                }
            }
            
            // Selected employees info
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Seçili Çalışanlar (${selectedEmployeeList.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    selectedEmployeeList.forEach { employee ->
                        Text(
                            text = "• ${employee.firstName} ${employee.lastName}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
            
            // Permissions selection
            Text(
                text = "Yeni Yetkiler",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.height(300.dp)
            ) {
                items(EmployeePermission.values()) { permission ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (permission in selectedPermissions) {
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                            } else {
                                MaterialTheme.colorScheme.surface
                            }
                        ),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = if (permission in selectedPermissions) 4.dp else 2.dp
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = permission in selectedPermissions,
                                onCheckedChange = { checked ->
                                    if (checked) {
                                        selectedPermissions = selectedPermissions + permission
                                    } else {
                                        selectedPermissions = selectedPermissions - permission
                                    }
                                }
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = permission.getDisplayName(),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = permission.getDescription(),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
            
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
                    onClick = { onUpdatePermissions(selectedPermissions.toList()) },
                    modifier = Modifier.weight(1f),
                    enabled = selectedPermissions.isNotEmpty()
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Güncelle")
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * Bottom sheet for bulk skill updates
 * Allows updating skills for multiple selected employees
 * Memory efficient: reusable component with minimal state
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BulkSkillUpdateSheet(
    selectedEmployees: Set<String>,
    employees: List<Employee>,
    onDismiss: () -> Unit,
    onUpdateSkills: (List<String>) -> Unit
) {
    var selectedSkills by remember { mutableStateOf(emptySet<String>()) }
    var customSkill by remember { mutableStateOf("") }
    var showCustomSkillInput by remember { mutableStateOf(false) }
    
    val selectedEmployeeList = employees.filter { it.id in selectedEmployees }
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(),
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Yetenek Güncelleme",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Kapat"
                    )
                }
            }
            
            // Selected employees info
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Seçili Çalışanlar (${selectedEmployeeList.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    selectedEmployeeList.forEach { employee ->
                        Text(
                            text = "• ${employee.firstName} ${employee.lastName}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
            
            // Skills selection
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Yeni Yetenekler",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                TextButton(
                    onClick = { showCustomSkillInput = !showCustomSkillInput }
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Özel Ekle")
                }
            }
            
            // Custom skill input
            if (showCustomSkillInput) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = customSkill,
                        onValueChange = { customSkill = it },
                        label = { Text("Özel Yetenek") },
                        placeholder = { Text("Yetenek adı") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (customSkill.isNotBlank()) {
                                selectedSkills = selectedSkills + customSkill.trim()
                                customSkill = ""
                                showCustomSkillInput = false
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Ekle"
                        )
                    }
                }
            }
            
            // Skills chips
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.height(300.dp)
            ) {
                val allSkills = (Employee.commonSkills + selectedSkills.toList()).distinct()
                
                items(allSkills) { skill ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (skill in selectedSkills) {
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                            } else {
                                MaterialTheme.colorScheme.surface
                            }
                        ),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = if (skill in selectedSkills) 4.dp else 2.dp
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = skill in selectedSkills,
                                onCheckedChange = { checked ->
                                    if (checked) {
                                        selectedSkills = selectedSkills + skill
                                    } else {
                                        selectedSkills = selectedSkills - skill
                                    }
                                }
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = if (skill in selectedSkills) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = skill,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
            
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
                    onClick = { onUpdateSkills(selectedSkills.toList()) },
                    modifier = Modifier.weight(1f),
                    enabled = selectedSkills.isNotEmpty()
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Güncelle")
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
} 