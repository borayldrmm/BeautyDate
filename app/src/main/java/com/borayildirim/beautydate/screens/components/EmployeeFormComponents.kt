package com.borayildirim.beautydate.screens.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.borayildirim.beautydate.data.models.Employee
import com.borayildirim.beautydate.data.models.EmployeeGender
import com.borayildirim.beautydate.data.models.EmployeePermission

/**
 * Reusable form section component for employee forms
 * Memory efficient: simple container with consistent styling
 */
@Composable
fun EmployeeFormSection(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
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
 * Gender selector component with radio buttons
 * Memory efficient: simple radio button group
 */
@Composable
fun EmployeeGenderSelector(
    selectedGender: EmployeeGender,
    onGenderSelected: (EmployeeGender) -> Unit
) {
    Column {
        Text(
            text = "Cinsiyet",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            EmployeeGender.values()
                .filter { it != EmployeeGender.OTHER } // Hide OTHER option
                .forEach { gender ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable { onGenderSelected(gender) }
                            .padding(8.dp)
                    ) {
                        RadioButton(
                            selected = selectedGender == gender,
                            onClick = { onGenderSelected(gender) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${gender.getEmoji()} ${gender.getDisplayName()}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
        }
    }
}

/**
 * Skills selection component with chips
 * Memory efficient: lazy loading of chips with toggleable state
 */
@Composable
fun EmployeeSkillsSection(
    selectedSkills: Set<String>,
    onSkillsChanged: (Set<String>) -> Unit
) {
    var customSkill by remember { mutableStateOf("") }
    var showCustomSkillInput by remember { mutableStateOf(false) }
    
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Yetenekler",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
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
                            onSkillsChanged(selectedSkills + customSkill.trim())
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
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Skills chips
        val allSkills = (Employee.commonSkills + selectedSkills.toList()).distinct()
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            items(allSkills) { skill ->
                val isSelected = skill in selectedSkills
                
                FilterChip(
                    onClick = {
                        if (isSelected) {
                            onSkillsChanged(selectedSkills - skill)
                        } else {
                            onSkillsChanged(selectedSkills + skill)
                        }
                    },
                    label = { Text(skill) },
                    selected = isSelected,
                    leadingIcon = if (isSelected) {
                        {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    } else null
                )
            }
        }
        
        if (selectedSkills.isNotEmpty()) {
            Text(
                text = "${selectedSkills.size} yetenek seçildi",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

/**
 * Permissions selection component with detailed descriptions
 * Memory efficient: simple checkbox list with descriptions
 */
@Composable
fun EmployeePermissionsSection(
    selectedPermissions: Set<EmployeePermission>,
    onPermissionsChanged: (Set<EmployeePermission>) -> Unit
) {
    Column {
        Text(
            text = "Yetkiler",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            EmployeePermission.values().forEach { permission ->
                val isSelected = permission in selectedPermissions
                
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (isSelected) {
                                onPermissionsChanged(selectedPermissions - permission)
                            } else {
                                onPermissionsChanged(selectedPermissions + permission)
                            }
                        },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) {
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        } else {
                            MaterialTheme.colorScheme.surface
                        }
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = if (isSelected) 4.dp else 2.dp
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Checkbox(
                            checked = isSelected,
                            onCheckedChange = {
                                if (it) {
                                    onPermissionsChanged(selectedPermissions + permission)
                                } else {
                                    onPermissionsChanged(selectedPermissions - permission)
                                }
                            }
                        )
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = permission.getEmoji(),
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = permission.getDisplayName(),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    }
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            
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
        
        if (selectedPermissions.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${selectedPermissions.size} yetki seçildi",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
} 