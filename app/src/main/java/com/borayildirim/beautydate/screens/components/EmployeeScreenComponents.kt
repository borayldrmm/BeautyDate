package com.borayildirim.beautydate.screens.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.borayildirim.beautydate.data.models.Employee
import com.borayildirim.beautydate.data.models.EmployeeGender
import com.borayildirim.beautydate.data.models.EmployeePermission

/**
 * Header section for Employee screen showing statistics and sync button
 * Memory efficient: simple composable with minimal state
 */
@Composable
fun EmployeeScreenHeader(
    totalEmployees: Int,
    activeEmployees: Int,
    isLoading: Boolean,
    isSyncing: Boolean,
    onSyncClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Çalışan Yönetimi",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Personel bilgileri ve yetkileri",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
                
                // Sync Button
                IconButton(
                    onClick = onSyncClick,
                    enabled = !isSyncing && !isLoading
                ) {
                    if (isSyncing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = "Senkronize Et",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Statistics Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                EmployeeStatCard(
                    title = "Toplam",
                    value = totalEmployees.toString(),
                    icon = Icons.Default.Group,
                    color = MaterialTheme.colorScheme.primary
                )
                
                EmployeeStatCard(
                    title = "Aktif",
                    value = activeEmployees.toString(),
                    icon = Icons.Default.CheckCircle,
                    color = Color(0xFF4CAF50)
                )
                
                val inactiveEmployees = totalEmployees - activeEmployees
                EmployeeStatCard(
                    title = "Pasif",
                    value = inactiveEmployees.toString(),
                    icon = Icons.Default.Cancel,
                    color = Color(0xFFFF9800)
                )
            }
        }
    }
}

/**
 * Stat card component for employee statistics
 * Memory efficient: simple UI component
 */
@Composable
private fun EmployeeStatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .background(
                color.copy(alpha = 0.1f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(16.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Search and filter section for employees
 * Memory efficient: combines search, filters, and selection mode
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeeSearchAndFilterSection(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedGender: EmployeeGender?,
    onGenderFilterChange: (EmployeeGender?) -> Unit,
    selectedPermission: EmployeePermission?,
    onPermissionFilterChange: (EmployeePermission?) -> Unit,
    selectedSkill: String?,
    onSkillFilterChange: (String?) -> Unit,
    availableSkills: List<String>,
    showActiveOnly: Boolean,
    onActiveFilterChange: (Boolean) -> Unit,
    isInSelectionMode: Boolean,
    onToggleSelectionMode: () -> Unit,
    selectedCount: Int
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            label = { Text("Çalışan Ara") },
            placeholder = { Text("İsim, telefon veya e-mail...") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Ara"
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(
                        onClick = { onSearchQueryChange("") }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Temizle"
                        )
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Filter Chips Row
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            // Active/All Filter
            item {
                FilterChip(
                    onClick = { onActiveFilterChange(!showActiveOnly) },
                    label = { 
                        Text(if (showActiveOnly) "Aktif" else "Tümü") 
                    },
                    selected = showActiveOnly,
                    leadingIcon = {
                        Icon(
                            imageVector = if (showActiveOnly) Icons.Default.CheckCircle else Icons.Default.Group,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )
            }
            
            // Gender Filter
            item {
                var genderMenuExpanded by remember { mutableStateOf(false) }
                
                FilterChip(
                    onClick = { genderMenuExpanded = true },
                    label = { 
                        Text(selectedGender?.getDisplayName() ?: "Cinsiyet") 
                    },
                    selected = selectedGender != null,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.ExpandMore,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )
                
                DropdownMenu(
                    expanded = genderMenuExpanded,
                    onDismissRequest = { genderMenuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Tümü") },
                        onClick = {
                            onGenderFilterChange(null)
                            genderMenuExpanded = false
                        }
                    )
                    EmployeeGender.values()
                        .filter { it != EmployeeGender.OTHER } // Hide OTHER option
                        .forEach { gender ->
                            DropdownMenuItem(
                                text = { Text(gender.getDisplayName()) },
                                onClick = {
                                    onGenderFilterChange(gender)
                                    genderMenuExpanded = false
                                }
                            )
                        }
                }
            }
            
            // Permission Filter
            item {
                var permissionMenuExpanded by remember { mutableStateOf(false) }
                
                FilterChip(
                    onClick = { permissionMenuExpanded = true },
                    label = { 
                        Text(selectedPermission?.getDisplayName() ?: "Yetki") 
                    },
                    selected = selectedPermission != null,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.ExpandMore,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )
                
                DropdownMenu(
                    expanded = permissionMenuExpanded,
                    onDismissRequest = { permissionMenuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Tümü") },
                        onClick = {
                            onPermissionFilterChange(null)
                            permissionMenuExpanded = false
                        }
                    )
                    EmployeePermission.values().forEach { permission ->
                        DropdownMenuItem(
                            text = { Text(permission.getDisplayName()) },
                            onClick = {
                                onPermissionFilterChange(permission)
                                permissionMenuExpanded = false
                            }
                        )
                    }
                }
            }
            
            // Skill Filter
            if (availableSkills.isNotEmpty()) {
                item {
                    var skillMenuExpanded by remember { mutableStateOf(false) }
                    
                    FilterChip(
                        onClick = { skillMenuExpanded = true },
                        label = { 
                            Text(selectedSkill ?: "Yetenek") 
                        },
                        selected = selectedSkill != null,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.ExpandMore,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    )
                    
                    DropdownMenu(
                        expanded = skillMenuExpanded,
                        onDismissRequest = { skillMenuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Tümü") },
                            onClick = {
                                onSkillFilterChange(null)
                                skillMenuExpanded = false
                            }
                        )
                        availableSkills.forEach { skill ->
                            DropdownMenuItem(
                                text = { Text(skill) },
                                onClick = {
                                    onSkillFilterChange(skill)
                                    skillMenuExpanded = false
                                }
                            )
                        }
                    }
                }
            }
            
            // Selection Mode Toggle
            item {
                FilterChip(
                    onClick = onToggleSelectionMode,
                    label = { 
                        Text(
                            if (isInSelectionMode) "Seçim: $selectedCount" else "Seç"
                        ) 
                    },
                    selected = isInSelectionMode,
                    leadingIcon = {
                        Icon(
                            imageVector = if (isInSelectionMode) Icons.Default.CheckBox else Icons.Default.CheckBoxOutlineBlank,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
} 

/**
 * Employee list component with LazyColumn
 * Memory efficient: lazy loading with item recycling
 */
@Composable
fun EmployeeList(
    employees: List<Employee>,
    isInSelectionMode: Boolean,
    selectedEmployees: Set<String>,
    onEmployeeClick: (Employee) -> Unit,
    onEmployeeSelect: (String) -> Unit,
    onToggleStatus: (String, Boolean) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(
            items = employees,
            key = { employee -> employee.id }
        ) { employee ->
            EmployeeItem(
                employee = employee,
                isSelected = employee.id in selectedEmployees,
                isInSelectionMode = isInSelectionMode,
                onClick = { onEmployeeClick(employee) },
                onSelect = { onEmployeeSelect(employee.id) },
                onToggleStatus = { employeeId, isActive ->
                    onToggleStatus(employeeId, isActive)
                }
            )
        }
    }
}

/**
 * Individual employee item component
 * Memory efficient: minimal state and recomposition
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeeItem(
    employee: Employee,
    isSelected: Boolean,
    isInSelectionMode: Boolean,
    onClick: () -> Unit,
    onSelect: () -> Unit,
    onToggleStatus: (String, Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { 
                if (isInSelectionMode) onSelect() else onClick() 
            },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 8.dp else 4.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Selection Checkbox (Selection Mode)
            if (isInSelectionMode) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onSelect() }
                )
                Spacer(modifier = Modifier.width(12.dp))
            }
            
            // Employee Avatar
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${employee.firstName.firstOrNull() ?: ""}${employee.lastName.firstOrNull() ?: ""}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Employee Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "${employee.firstName} ${employee.lastName}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = employee.phoneNumber,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            // Status and Actions
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Status Badge
                AssistChip(
                    onClick = { onToggleStatus(employee.id, !employee.isActive) },
                    label = { 
                        Text(
                            text = if (employee.isActive) "Aktif" else "Pasif",
                            style = MaterialTheme.typography.labelSmall
                        ) 
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = if (employee.isActive) Icons.Default.CheckCircle else Icons.Default.Cancel,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = if (employee.isActive) {
                            Color(0xFF4CAF50).copy(alpha = 0.1f)
                        } else {
                            Color(0xFFFF9800).copy(alpha = 0.1f)
                        },
                        labelColor = if (employee.isActive) {
                            Color(0xFF2E7D32)
                        } else {
                            Color(0xFFE65100)
                        },
                        leadingIconContentColor = if (employee.isActive) {
                            Color(0xFF2E7D32)
                        } else {
                            Color(0xFFE65100)
                        }
                    )
                )
            }
        }
    }
}

/**
 * Empty state when no employees exist
 * Memory efficient: static composable
 */
@Composable
fun EmployeeEmptyState(
    onAddEmployeeClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Group,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Henüz çalışan yok",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "İlk çalışanınızı ekleyerek başlayın.\nPersonel yönetimi ve yetkilendirme burada yapılır.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Empty state when search returns no results
 */
@Composable
fun EmployeeSearchEmptyState(
    searchQuery: String,
    onClearSearch: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.SearchOff,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Sonuç bulunamadı",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "\"$searchQuery\" araması için sonuç bulunamadı.\nFarklı bir arama terimi deneyin.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        OutlinedButton(onClick = onClearSearch) {
            Icon(
                imageVector = Icons.Default.Clear,
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Aramayı Temizle")
        }
    }
}

/**
 * Empty state when filters return no results
 */
@Composable
fun EmployeeFilterEmptyState(
    onClearFilters: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.FilterAltOff,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Filtre sonucu bulunamadı",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Seçili filtreler için çalışan bulunamadı.\nFiltreleri temizleyerek tekrar deneyin.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        OutlinedButton(onClick = onClearFilters) {
            Icon(
                imageVector = Icons.Default.FilterAltOff,
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Filtreleri Temizle")
        }
    }
} 