package com.borayildirim.beautydate.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.borayildirim.beautydate.data.models.Employee
import com.borayildirim.beautydate.data.models.EmployeePermission
import com.borayildirim.beautydate.viewmodels.EmployeeViewModel

/**
 * Employee detail screen showing comprehensive information
 * Modern Material3 design with sectioned layout
 * Memory efficient: reusable components and lazy loading
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeeDetailScreen(
    employee: Employee,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (() -> Unit)? = null,
    employeeViewModel: EmployeeViewModel = hiltViewModel()
) {
    var showStatusDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Çalışan Detayları",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Geri"
                        )
                    }
                },
                actions = {
                    onNavigateToEdit?.let { onEdit ->
                        IconButton(onClick = onEdit) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Düzenle"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
            // Header Section with Avatar and Basic Info
            item {
                EmployeeDetailHeader(
                    employee = employee,
                    onStatusToggle = { showStatusDialog = true }
                )
            }
            
            // Contact Information Section
            item {
                EmployeeDetailSection(
                    title = "İletişim Bilgileri",
                    icon = Icons.Default.Phone
                ) {
                    ContactInformationContent(employee = employee)
                }
            }
            
            // Work Information Section
            item {
                EmployeeDetailSection(
                    title = "İş Bilgileri",
                    icon = Icons.Default.Work
                ) {
                    WorkInformationContent(employee = employee)
                }
            }
            
            // Skills Section
            if (employee.skills.isNotEmpty()) {
                item {
                    EmployeeDetailSection(
                        title = "Yetenekler",
                        icon = Icons.Default.Star
                    ) {
                        SkillsContent(skills = employee.skills)
                    }
                }
            }
            
            // Permissions Section
            if (employee.permissions.isNotEmpty()) {
                item {
                    EmployeeDetailSection(
                        title = "Yetkiler",
                        icon = Icons.Default.Security
                    ) {
                        PermissionsContent(permissions = employee.permissions)
                    }
                }
            }
            
            // Notes Section
            if (employee.notes.isNotBlank()) {
                item {
                    EmployeeDetailSection(
                        title = "Notlar",
                        icon = Icons.Default.Notes
                    ) {
                        NotesContent(notes = employee.notes)
                    }
                }
            }
        }
    }
    
    // Status Change Dialog
    if (showStatusDialog) {
        StatusChangeDialog(
            currentStatus = employee.isActive,
            employeeName = "${employee.firstName} ${employee.lastName}",
            onConfirm = { newStatus ->
                employeeViewModel.toggleEmployeeStatus(employee.id)
                showStatusDialog = false
            },
            onDismiss = { showStatusDialog = false }
        )
    }
}

/**
 * Header section with employee avatar and basic information
 */
@Composable
private fun EmployeeDetailHeader(
    employee: Employee,
    onStatusToggle: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Large Avatar
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${employee.firstName.firstOrNull() ?: ""}${employee.lastName.firstOrNull() ?: ""}",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Full Name
            Text(
                text = "${employee.firstName} ${employee.lastName}",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Gender and ID
            Text(
                text = "${employee.gender.getDisplayName()} • ID: ${employee.id.take(8)}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Status Toggle Button
            OutlinedButton(
                onClick = onStatusToggle,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = if (employee.isActive) {
                        Color(0xFF2E7D32)
                    } else {
                        Color(0xFFE65100)
                    }
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = if (employee.isActive) Icons.Default.CheckCircle else Icons.Default.Cancel,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (employee.isActive) "Aktif Çalışan" else "Pasif Çalışan",
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

/**
 * Reusable section component for detail screen
 */
@Composable
private fun EmployeeDetailSection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable () -> Unit
) {
    // Her section için farklı renk temaları
    val cardColor = when (title) {
        "İletişim Bilgileri" -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f)
        "İş Bilgileri" -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.2f)
        "Yetenekler" -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
        "Yetkiler" -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)
        "Notlar" -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        else -> MaterialTheme.colorScheme.surface
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = cardColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Section Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            // Section Content
            content()
        }
    }
}

/**
 * Contact information content
 */
@Composable
private fun ContactInformationContent(employee: Employee) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Phone Number
        DetailInfoRow(
            icon = Icons.Default.Phone,
            label = "Telefon",
            value = employee.phoneNumber
        )
        
        // Email
        if (employee.email.isNotBlank()) {
            DetailInfoRow(
                icon = Icons.Default.Email,
                label = "E-mail",
                value = employee.email
            )
        }
        
        // Address
        if (employee.address.isNotBlank()) {
            DetailInfoRow(
                icon = Icons.Default.LocationOn,
                label = "Adres",
                value = employee.address
            )
        }
    }
}

/**
 * Work information content
 */
@Composable
private fun WorkInformationContent(employee: Employee) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Hire Date
        DetailInfoRow(
            icon = Icons.Default.DateRange,
            label = "İşe Başlama Tarihi",
            value = employee.hireDate
        )
        
        // Status
        DetailInfoRow(
            icon = if (employee.isActive) Icons.Default.CheckCircle else Icons.Default.Cancel,
            label = "Durum",
            value = if (employee.isActive) "Aktif" else "Pasif"
        )
    }
}

/**
 * Skills content with chips
 */
@Composable
private fun SkillsContent(skills: List<String>) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(skills) { skill ->
            AssistChip(
                onClick = { },
                label = { Text(skill) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    labelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    leadingIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    }
}

/**
 * Permissions content with detailed chips
 */
@Composable
private fun PermissionsContent(permissions: List<EmployeePermission>) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        permissions.forEach { permission ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = permission.getDisplayName(),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = permission.getDescription(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

/**
 * Notes content
 */
@Composable
private fun NotesContent(notes: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Text(
            text = notes,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(16.dp)
        )
    }
}

/**
 * Reusable info row component
 */
@Composable
private fun DetailInfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * Status change confirmation dialog
 */
@Composable
private fun StatusChangeDialog(
    currentStatus: Boolean,
    employeeName: String,
    onConfirm: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    val newStatus = !currentStatus
    val actionText = if (newStatus) "aktif" else "pasif"
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Durum Değişikliği",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = "$employeeName adlı çalışanı $actionText yapmak istediğinizden emin misiniz?",
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(newStatus) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (newStatus) {
                        Color(0xFF4CAF50)
                    } else {
                        Color(0xFFFF9800)
                    },
                    contentColor = Color.White
                )
            ) {
                Text("Evet, ${actionText.uppercase()} yap")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("İptal")
            }
        }
    )
}

