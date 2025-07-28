package com.borayildirim.beautydate.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.borayildirim.beautydate.R
import com.borayildirim.beautydate.viewmodels.AuthViewModel

/**
 * Home screen for BeautyDate business app
 * Shows welcome message and logout option
 */
@Composable
fun HomeScreen(
    authViewModel: AuthViewModel,
    onNavigateToLogin: () -> Unit
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    var showLogoutDialog by remember { mutableStateOf(false) }
    
    // Handle logout
    LaunchedEffect(Unit) {
        // Check if user is still logged in
        if (!authViewModel.isLoggedIn()) {
            onNavigateToLogin()
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Welcome message
        Text(
            text = stringResource(R.string.welcome_message),
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 32.sp
            ),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // User info
        currentUser?.let { user ->
            Text(
                text = "Email: ${user.email}",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Email Verified: ${if (user.isEmailVerified) "Yes" else "No"}",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // Business info placeholder
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Business Dashboard",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Welcome to your beauty business management dashboard!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Here you can manage your appointments, customers, and business operations.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Logout button
        Button(
            onClick = {
                showLogoutDialog = true
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            )
        ) {
            Text(
                text = stringResource(R.string.logout),
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
    
    // Logout confirmation dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = {
                Text(text = "Çıkış Yap")
            },
            text = {
                Text(text = "Çıkış yapmayı onaylıyor musunuz?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        authViewModel.signOut()
                        onNavigateToLogin()
                    }
                ) {
                    Text("Evet")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showLogoutDialog = false }
                ) {
                    Text("Hayır")
                }
            }
        )
    }
} 