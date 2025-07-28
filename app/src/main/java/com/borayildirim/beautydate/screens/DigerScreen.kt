package com.borayildirim.beautydate.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.borayildirim.beautydate.components.BreathingLogo
import com.borayildirim.beautydate.components.OtherMenuItem
import com.borayildirim.beautydate.screens.components.DigerHeaderSection
import com.borayildirim.beautydate.viewmodels.OtherMenuViewModel

/**
 * "Diğer" screen showing profile header and menu options
 * Part of bottom navigation - rightmost tab with three dots icon
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DigerScreen(
    onNavigateToProfileSettings: () -> Unit,
    onNavigateToTheme: () -> Unit,
    onNavigateToFeedback: () -> Unit,
    onNavigateToFinance: () -> Unit,
    onNavigateToStatistics: () -> Unit,
    onNavigateToTutorial: () -> Unit,
    onNavigateToLogin: () -> Unit,
    authViewModel: com.borayildirim.beautydate.viewmodels.AuthViewModel,
    otherMenuViewModel: OtherMenuViewModel = hiltViewModel()
) {
    val uiState by otherMenuViewModel.uiState.collectAsState()
    val authUiState by authViewModel.uiState.collectAsState()
    val context = LocalContext.current
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Text(
                    text = "Diğer",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface,
                titleContentColor = MaterialTheme.colorScheme.onSurface
            )
        )
        
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        
        // Profile Header Section
        DigerHeaderSection(
            businessName = uiState.businessName.ifEmpty { "İşletme Adı" },
            username = uiState.username,
            onProfileClick = onNavigateToProfileSettings
        )
        
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        
        // Menu Items
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            
            // Menu items
            items(uiState.menuItems) { item ->
                OtherMenuItem(
                    item = item,
                    onClick = {
                        when (item.id) {
                            "profile_settings" -> onNavigateToProfileSettings()
                            "theme" -> onNavigateToTheme()
                            "feedback" -> onNavigateToFeedback()
                            "statistics" -> onNavigateToStatistics()
                            "finance" -> onNavigateToFinance()
                            "how_to_use" -> onNavigateToTutorial()
                            "logout" -> otherMenuViewModel.showLogoutDialog()
                            "notifications" -> {
                                Toast.makeText(
                                    context,
                                    "${item.title} çok yakında eklenecek",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                )
            }
        }
    }
    
    // Logout confirmation dialog
    if (uiState.showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { 
                if (!authUiState.isLoading) {
                    otherMenuViewModel.hideLogoutDialog()
                }
            },
            title = {
                Text(text = if (authUiState.isLoading) "Çıkış Yapılıyor..." else "Çıkış Yap")
            },
            text = {
                if (authUiState.isLoading) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        BreathingLogo(size = 60.dp)
                        Text(
                            text = "Çıkış işlemi gerçekleştiriliyor...",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                } else {
                    Text(text = "Çıkış yapmayı onaylıyor musunuz?")
                }
            },
            confirmButton = {
                if (!authUiState.isLoading) {
                    TextButton(
                        onClick = { 
                            authViewModel.signOut()
                        }
                    ) {
                        Text("Çıkış")
                    }
                }
            },
            dismissButton = {
                if (!authUiState.isLoading) {
                    TextButton(
                        onClick = { otherMenuViewModel.hideLogoutDialog() }
                    ) {
                        Text("İptal")
                    }
                }
            }
        )
    }
    
    // Handle logout completion and navigate to login
    LaunchedEffect(authUiState.shouldNavigateToLogin) {
        if (authUiState.shouldNavigateToLogin) {
            otherMenuViewModel.hideLogoutDialog()
            authViewModel.clearNavigationFlag()
            onNavigateToLogin()
        }
    }
    
    // Error message
    uiState.errorMessage?.let { error ->
        LaunchedEffect(error) {
            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
            otherMenuViewModel.clearError()
        }
    }
} 