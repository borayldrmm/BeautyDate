package com.borayildirim.beautydate.screens.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

/**
 * Simple contact information form section component
 * Memory efficient: basic phone and email fields only
 * Used for customer forms
 */
@Composable
fun SimpleContactFormSection(
    phoneNumber: String,
    onPhoneNumberChange: (String) -> Unit,
    email: String,
    onEmailChange: (String) -> Unit,
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
                text = "İletişim Bilgileri",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.primary
            )
            
            // Phone number field
            OutlinedTextField(
                value = phoneNumber,
                onValueChange = onPhoneNumberChange,
                label = { Text("Telefon Numarası") },
                placeholder = { Text("0 (5__) ___ __ __") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )
            
            // Email field
            OutlinedTextField(
                value = email,
                onValueChange = onEmailChange,
                label = { Text("E-posta (Opsiyonel)") },
                placeholder = { Text("ornek@email.com") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )
        }
    }
} 