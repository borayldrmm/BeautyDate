package com.borayildirim.beautydate.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.borayildirim.beautydate.R
import com.borayildirim.beautydate.components.BreathingLogo
import com.borayildirim.beautydate.viewmodels.AuthViewModel
import kotlinx.coroutines.delay

/**
 * Welcome screen shown when app first launches
 * Displays for 1 second then redirects based on authentication state
 */
@Composable
fun WelcomeScreen(
    authViewModel: AuthViewModel,
    onNavigateToLogin: () -> Unit,
    onNavigateToHome: () -> Unit
) {
    var showWelcome by remember { mutableStateOf(true) }
    
    // Auto-navigate after 1 second (reduced from 2 seconds)
    LaunchedEffect(Unit) {
        delay(1000)
        showWelcome = false
        
        // Check if user is logged in
        if (authViewModel.isLoggedIn()) {
            onNavigateToHome()
        } else {
            onNavigateToLogin()
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo - Optimized size with minimal spacing
            Image(
                painter = painterResource(id = R.drawable.logo_png),
                contentDescription = "BeautyDate Logo",
                modifier = Modifier.size(320.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Title - Updated text
            Text(
                text = "Hoş Geldiniz",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 28.sp
                ),
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Subtitle - Updated text
            Text(
                text = "Tüm randevularınızı kolayca yönetin",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Breathing Logo Loading
            BreathingLogo(size = 100.dp)
        }
    }
} 