package com.borayildirim.beautydate.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Password input field with visibility toggle and info dialog
 * Shows password requirements when info icon is clicked
 */
@Composable
fun PasswordFieldWithInfo(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    errorMessage: String? = null
) {
    var passwordVisible by remember { mutableStateOf(false) }
    var showInfoDialog by remember { mutableStateOf(false) }
    
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            visualTransformation = if (passwordVisible) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            trailingIcon = {
                Row {
                    // Info icon button
                    IconButton(
                        onClick = { showInfoDialog = true }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Info,
                            contentDescription = "Şifre gereksinimleri",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    // Visibility toggle button
                    IconButton(
                        onClick = { passwordVisible = !passwordVisible }
                    ) {
                        Icon(
                            imageVector = if (passwordVisible) {
                                Icons.Filled.Visibility
                            } else {
                                Icons.Filled.VisibilityOff
                            },
                            contentDescription = if (passwordVisible) {
                                "Şifreyi gizle"
                            } else {
                                "Şifreyi göster"
                            }
                        )
                    }
                }
            },
            isError = isError,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        
        if (isError && errorMessage != null) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
    
    // Password requirements dialog
    if (showInfoDialog) {
        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            title = {
                Text(
                    text = "Neden güçlü şifre istiyoruz?",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Sizin ve işletmenizin verilerinin güvenliğini korumak için 1 küçük harf, 1 büyük harf ve 1 rakamdan oluşan minimum 8 karakter uzunluğunda şifre girmeniz gerekmektedir.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Start
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { showInfoDialog = false }
                ) {
                    Text(
                        text = "Tamam",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        )
    }
} 