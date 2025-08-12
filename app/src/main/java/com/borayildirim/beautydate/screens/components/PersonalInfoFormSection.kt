package com.borayildirim.beautydate.screens.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.material3.MenuAnchorType
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.borayildirim.beautydate.data.models.CustomerGender

/**
 * Personal information form section component
 * Memory efficient: reusable component for customer forms
 * Contains name, birth date, and gender fields
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalInfoFormSection(
    firstName: String,
    onFirstNameChange: (String) -> Unit,
    lastName: String,
    onLastNameChange: (String) -> Unit,
    birthDate: String,
    onBirthDateChange: (String) -> Unit,
    gender: CustomerGender,
    onGenderChange: (CustomerGender) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Kişisel Bilgiler",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.primary
            )
            
            // Name fields row - Memory efficient: reuse modifiers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = firstName,
                    onValueChange = onFirstNameChange,
                    label = { Text("Ad") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = lastName,
                    onValueChange = onLastNameChange,
                    label = { Text("Soyad") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }
            
            // Birth date field
            OutlinedTextField(
                value = birthDate,
                onValueChange = onBirthDateChange,
                label = { Text("Doğum Tarihi (Opsiyonel)") },
                placeholder = { Text("GG/AA/YYYY") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            
            // Gender selection - Memory efficient: use remember for expanded state
            var expanded by remember { mutableStateOf(false) }
            
            Column {
                Text(
                    text = "Cinsiyet",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = when (gender) {
                            CustomerGender.MALE -> "Erkek"
                            CustomerGender.FEMALE -> "Kadın"
                            CustomerGender.OTHER -> "Diğer"
                        },
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Cinsiyet") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    )
                    
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        CustomerGender.values().forEach { genderOption ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        when (genderOption) {
                                            CustomerGender.MALE -> "Erkek"
                                            CustomerGender.FEMALE -> "Kadın"
                                            CustomerGender.OTHER -> "Diğer"
                                        }
                                    )
                                },
                                onClick = {
                                    onGenderChange(genderOption)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}