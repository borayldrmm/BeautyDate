package com.borayildirim.beautydate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.borayildirim.beautydate.data.local.ThemeMode
import com.borayildirim.beautydate.navigation.AppNavigation
import com.borayildirim.beautydate.navigation.NavigationRoutes
import com.borayildirim.beautydate.ui.theme.BeautyDateTheme
import com.borayildirim.beautydate.viewmodels.AuthViewModel
import com.borayildirim.beautydate.viewmodels.ThemeViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main activity for BeautyDate app
 * Handles navigation between Welcome, Login, Register, and Home screens
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BeautyDateApp()
        }
    }
}

/**
 * Main app composable with theme support
 */
@Composable
fun BeautyDateApp() {
    val authViewModel: AuthViewModel = hiltViewModel()
    val themeViewModel: ThemeViewModel = hiltViewModel()
    val customerViewModel: com.borayildirim.beautydate.viewmodels.CustomerViewModel = hiltViewModel()
    val employeeViewModel: com.borayildirim.beautydate.viewmodels.EmployeeViewModel = hiltViewModel()
    val appointmentViewModel: com.borayildirim.beautydate.viewmodels.AppointmentViewModel = hiltViewModel()
    val navController = rememberNavController()
    
    // Observe current theme
    val themeState by themeViewModel.uiState.collectAsState()
    val isDarkTheme = themeState.currentTheme == ThemeMode.DARK
    
    // Determine start destination based on login status
    val startDestination = if (authViewModel.isLoggedIn()) {
        NavigationRoutes.HOME
    } else {
        NavigationRoutes.WELCOME
    }
    
    BeautyDateTheme(darkTheme = isDarkTheme) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            AppNavigation(
                navController = navController,
                authViewModel = authViewModel,
                customerViewModel = customerViewModel,
                employeeViewModel = employeeViewModel,
                appointmentViewModel = appointmentViewModel,
                startDestination = startDestination
            )
        }
    }
}

