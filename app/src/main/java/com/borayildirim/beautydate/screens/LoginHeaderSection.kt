package com.borayildirim.beautydate.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.borayildirim.beautydate.R

/**
 * Composable for the login screen header section
 * Includes logo, title, and subtitle
 * Enhanced: Larger logo with logo_png.png, adjusted layout and text
 */
@Composable
fun LoginHeaderSection(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        
        // Logo - Compact size with perfect spacing
        Image(
            painter = painterResource(id = R.drawable.logo_png),
            contentDescription = "BeautyDate Logo",
            modifier = Modifier.size(200.dp)
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Title - Updated text
        Text(
            text = "Hoş geldiniz",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 32.sp
            ),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(6.dp))
        
        // Subtitle - Updated text
        Text(
            text = "Güzellik salonunuz için akıllı, hızlı ve güvenli yönetim.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(24.dp))
    }
} 