package com.borayildirim.beautydate.screens.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

/**
 * Profile form section component
 * Contains editable fields: Business name, address, phone number
 * Contains read-only fields: Email, username
 */
@Composable
fun ProfileFormSection(
    businessName: String,
    onBusinessNameChange: (String) -> Unit,
    address: String,
    onAddressChange: (String) -> Unit,
    phoneNumber: String,
    onPhoneNumberChange: (String) -> Unit,
    email: String,
    username: String,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section title
        Text(
            text = "İşletme Bilgileri",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
        
        // Business name field - editable
        OutlinedTextField(
            value = businessName,
            onValueChange = onBusinessNameChange,
            label = { Text("İşletme Adı") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading,
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
        )
        
        // Address field - editable
        OutlinedTextField(
            value = address,
            onValueChange = onAddressChange,
            label = { Text("İşletme Adresi") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading,
            minLines = 2,
            maxLines = 3,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
        )
        
        // Phone number field - editable
        OutlinedTextField(
            value = phoneNumber,
            onValueChange = onPhoneNumberChange,
            label = { Text("Telefon Numarası") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading,
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            placeholder = { Text("0 (5--) --- ----") }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Account information section
        Text(
            text = "Hesap Bilgileri",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
        
        // Email field - read only
        OutlinedTextField(
            value = email,
            onValueChange = { }, // Read-only
            label = { Text("E-posta") },
            modifier = Modifier.fillMaxWidth(),
            enabled = false,
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            )
        )
        
        // Username field - read only
        OutlinedTextField(
            value = username,
            onValueChange = { }, // Read-only
            label = { Text("Kullanıcı Adı") },
            modifier = Modifier.fillMaxWidth(),
            enabled = false,
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            )
        )
    }
} 