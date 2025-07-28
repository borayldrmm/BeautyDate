package com.borayildirim.beautydate.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.hilt.navigation.compose.hiltViewModel
import com.borayildirim.beautydate.components.BottomNavigationBar
import com.borayildirim.beautydate.components.LoadingWithBreathingLogo
import com.borayildirim.beautydate.navigation.BottomNavigationItem
import com.borayildirim.beautydate.navigation.NavigationRoutes
import com.borayildirim.beautydate.viewmodels.AuthViewModel
import com.borayildirim.beautydate.viewmodels.CustomerViewModel
import com.borayildirim.beautydate.screens.CalendarScreen

/**
 * Main screen with bottom navigation
 * Contains bottom navigation bar and navigation host for tab content
 * Memory efficient: reuses CustomerViewModel for navigation
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    authViewModel: AuthViewModel,
    onNavigateToLogin: () -> Unit,
    onNavigateToProfileSettings: () -> Unit,
    mainNavController: NavHostController? = null,
    customerViewModel: CustomerViewModel? = null,
    employeeViewModel: com.borayildirim.beautydate.viewmodels.EmployeeViewModel? = null,
    navController: NavHostController = rememberNavController(),
    initialTab: String = "ziyaretler"
) {
    // Memory efficient: use passed CustomerViewModel or create new one
    val custViewModel = customerViewModel ?: hiltViewModel<CustomerViewModel>()
    val empViewModel =
        employeeViewModel ?: hiltViewModel<com.borayildirim.beautydate.viewmodels.EmployeeViewModel>()
    val appointmentViewModel =
        hiltViewModel<com.borayildirim.beautydate.viewmodels.AppointmentViewModel>()
    val serviceViewModel = hiltViewModel<com.borayildirim.beautydate.viewmodels.ServiceViewModel>()
    val expenseViewModel = hiltViewModel<com.borayildirim.beautydate.viewmodels.ExpenseViewModel>()
    val statisticsViewModel = hiltViewModel<com.borayildirim.beautydate.viewmodels.StatisticsViewModel>()

    // Collect loading states for initial app loading
    val customerUiState by custViewModel.uiState.collectAsState()
    val appointmentUiState by appointmentViewModel.uiState.collectAsState()
    val serviceUiState by serviceViewModel.uiState.collectAsState()

    // Determine if app is still initializing
    val isInitializing =
        customerUiState.isLoading || appointmentUiState.isLoading || serviceUiState.isLoading

    // Initialize ViewModels for cross-device sync when MainScreen starts
    LaunchedEffect(Unit) {
        custViewModel.initializeCustomers()
        appointmentViewModel.initializeAppointments()
        serviceViewModel.initializeServices()
        expenseViewModel.initializeExpenses()
        statisticsViewModel.initializeStatistics()
    }

    // Determine start destination based on initialTab parameter
    val startDestination = when (initialTab) {
        "musteriler" -> NavigationRoutes.MUSTERILER
        "ziyaretler" -> NavigationRoutes.ZIYARETLER
        "operations" -> NavigationRoutes.OPERATIONS  // Updated: "islemler" → "operations"
        "diger" -> NavigationRoutes.DIGER
        else -> NavigationRoutes.RANDEVULAR // default
    }

    Box {
        Scaffold(
            bottomBar = {
                BottomNavigationBar(navController = navController)
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = startDestination,
                modifier = Modifier.padding(innerPadding)
            ) {
                // Randevular tab (Takvim ekranı)
                composable(NavigationRoutes.RANDEVULAR) {
                    CalendarScreen(
                        onNavigateToAddAppointment = { date, time ->
                            mainNavController?.navigate("${NavigationRoutes.ADD_APPOINTMENT}?date=$date&time=$time")
                        },
                        onNavigateToAppointmentDetail = { appointmentId ->
                            mainNavController?.navigate("${NavigationRoutes.APPOINTMENT_DETAIL}/$appointmentId")
                        },
                        onNavigateToWorkingHours = {
                            mainNavController?.navigate(NavigationRoutes.WORKING_HOURS)
                        }
                    )
                }

                // Ziyaretler tab (randevu sistemi burada)
                composable(NavigationRoutes.ZIYARETLER) {
                    AppointmentsScreen(
                        onNavigateToAddAppointment = {
                            mainNavController?.navigate(NavigationRoutes.ADD_APPOINTMENT)
                        },
                        onNavigateToEditAppointment = { appointmentId ->
                            mainNavController?.navigate("${NavigationRoutes.EDIT_APPOINTMENT}/$appointmentId")
                        },
                        appointmentViewModel = appointmentViewModel // Pass the same instance for cross-device sync
                    )
                }

                // İşlemler tab - Updated from YeniScreen to OperationsScreen
                composable(NavigationRoutes.OPERATIONS) {
                    OperationsScreen(
                        onNavigateToAddCustomer = {
                            mainNavController?.navigate("${NavigationRoutes.ADD_CUSTOMER}?source=operations")
                        },
                        onNavigateToAddAppointment = {
                            mainNavController?.navigate(NavigationRoutes.ADD_APPOINTMENT)
                        },
                        onNavigateToEmployeeList = {
                            mainNavController?.navigate(NavigationRoutes.EMPLOYEE_LIST)
                        },
                        onNavigateToAddEmployee = {
                            mainNavController?.navigate(NavigationRoutes.ADD_EMPLOYEE)
                        },
                        onNavigateToServiceList = {
                            mainNavController?.navigate(NavigationRoutes.SERVICE_LIST)
                        },
                        onNavigateToAddService = {
                            mainNavController?.navigate(NavigationRoutes.ADD_SERVICE)
                        },
                        onNavigateToWorkingHours = {
                            mainNavController?.navigate(NavigationRoutes.WORKING_HOURS)
                        },
                        onNavigateToBusinessExpenses = {
                            mainNavController?.navigate(NavigationRoutes.BUSINESS_EXPENSES)
                        },
                        onNavigateToPriceUpdate = {
                            mainNavController?.navigate(NavigationRoutes.PRICE_UPDATE)
                        },
                        onNavigateToCustomerNotes = {
                            mainNavController?.navigate(NavigationRoutes.CUSTOMER_NOTES)
                        },
                        onNavigateToAppointments = {
                            mainNavController?.navigate(NavigationRoutes.APPOINTMENTS)
                        },
                        onNavigateToCalendar = {
                            // Navigate to Calendar (Randevular) tab from Operations
                            navController.navigate(NavigationRoutes.RANDEVULAR)
                        },
                        employeeViewModel = empViewModel
                    )
                }

                // Müşteriler tab
                composable(NavigationRoutes.MUSTERILER) {
                    MusterilerScreen(
                        onNavigateToCustomerDetail = { customer ->
                            // Memory efficient: select customer in ViewModel, then navigate
                            custViewModel.selectCustomer(customer)
                            mainNavController?.let { navController ->
                                // Pass customerViewModel to AppNavigation for proper state management
                                navController.navigate(NavigationRoutes.CUSTOMER_DETAIL)
                            }
                        },
                        onNavigateToAddCustomer = {
                            mainNavController?.navigate("${NavigationRoutes.ADD_CUSTOMER}?source=musteriler")
                        },
                        onNavigateToEditCustomer = { customer ->
                            // Memory efficient: select customer in ViewModel, then navigate
                            custViewModel.selectCustomer(customer)
                            mainNavController?.let { navController ->
                                navController.navigate(NavigationRoutes.EDIT_CUSTOMER)
                            }
                        },
                        customerViewModel = custViewModel
                    )
                }

                // Diğer tab
                composable(NavigationRoutes.DIGER) {
                    DigerScreen(
                        onNavigateToProfileSettings = onNavigateToProfileSettings,
                        onNavigateToTheme = {
                            mainNavController?.navigate(NavigationRoutes.THEME)
                        },
                        onNavigateToFeedback = {
                            mainNavController?.navigate(NavigationRoutes.FEEDBACK)
                        },
                        onNavigateToFinance = {
                            mainNavController?.navigate(NavigationRoutes.FINANCE)
                        },
                        onNavigateToStatistics = {
                            mainNavController?.navigate(NavigationRoutes.STATISTICS)
                        },
                        onNavigateToTutorial = {
                            mainNavController?.navigate(NavigationRoutes.TUTORIAL)
                        },
                        onNavigateToLogin = onNavigateToLogin,
                        authViewModel = authViewModel
                    )
                }
            }

                    // Loading overlay during initialization with breathing logo
        if (isInitializing) {
            LoadingWithBreathingLogo(
                message = "Veriler yükleniyor...",
                subMessage = "Lütfen bekleyiniz",
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
            )
        }
        }
    }
}