package com.borayildirim.beautydate.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.borayildirim.beautydate.components.AuthButton
import com.borayildirim.beautydate.components.PasswordFieldWithInfo
import com.borayildirim.beautydate.components.UsernameField

/**
 * Composable for the login form section
 * Includes username, password, remember me checkbox, and login button
 */
@Composable
fun LoginFormSection(
    username: String,
    onUsernameChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    rememberUsername: Boolean,
    onRememberUsernameChange: (Boolean) -> Unit,
    onLoginClick: () -> Unit,
    isLoading: Boolean,
    onForgotPasswordClick: () -> Unit = {},

    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Username field
        UsernameField(
            value = username,
            onValueChange = onUsernameChange,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Password field with info dialog
        PasswordFieldWithInfo(
            value = password,
            onValueChange = onPasswordChange,
            label = "Parola",
            modifier = Modifier.fillMaxWidth()
        )

        // Remember me checkbox
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = rememberUsername,
                onCheckedChange = onRememberUsernameChange
            )
            Text(
                text = "Beni Hatırla",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))
        
        // Login button
        AuthButton(
            onClick = onLoginClick,
            text = "Giriş Yap",
            enabled = username.isNotBlank() && password.isNotBlank(),
            isLoading = isLoading
        )
        
        // Forgot password button
        TextButton(
            onClick = onForgotPasswordClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
                               Text(
                       text = "Şifremi Unuttum",
                       style = MaterialTheme.typography.bodyMedium.copy(
                           fontWeight = FontWeight.Medium
                       ),
                       color = MaterialTheme.colorScheme.primary
                   )
               }

           }
} 