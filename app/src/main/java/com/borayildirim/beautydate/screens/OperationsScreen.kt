package com.borayildirim.beautydate.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.borayildirim.beautydate.components.ActionCard
import com.borayildirim.beautydate.viewmodels.EmployeeViewModel

/**
 * Operations screen with dashboard layout
 * Provides quick access to business operations:
 * - Quick Actions: Customer, Appointment, Notes, Price Update
 * - Service Management: Services, Add Service, Working Hours, Coming Soon
 * - Employee Management: Staff, Add Employee
 * Memory efficient: uses LazyVerticalGrid for optimal rendering
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OperationsScreen(
    onNavigateToAddCustomer: () -> Unit = {},
    onNavigateToAddAppointment: () -> Unit = {},
    onNavigateToEmployeeList: () -> Unit = {},
    onNavigateToAddEmployee: () -> Unit = {},
    onNavigateToServiceList: () -> Unit = {},
    onNavigateToAddService: () -> Unit = {},
    onNavigateToWorkingHours: () -> Unit = {},
    onNavigateToPriceUpdate: () -> Unit = {},
    onNavigateToCustomerNotes: () -> Unit = {},
    onNavigateToAppointments: () -> Unit = {},
    onNavigateToCalendar: () -> Unit = {},
    onNavigateToBusinessExpenses: () -> Unit = {}, // New parameter for business expenses
    employeeViewModel: EmployeeViewModel = hiltViewModel()
) {
    // Screen state for handling actions
    var selectedAction by remember { mutableStateOf<String?>(null) }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Header
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "İşlem Merkezi",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Hızlı erişim için işleminizi seçin",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
            
            // Quick Actions Section
            item {
                DashboardSection(
                    title = "⚡ Hızlı Aksiyonlar",
                    actions = getQuickActions(),
                    onActionClick = { actionId ->
                        selectedAction = actionId
                        handleQuickAction(
                            actionId, 
                            onNavigateToAddCustomer, 
                            onNavigateToCustomerNotes, 
                            onNavigateToAddAppointment, // Updated: Use AddAppointment instead of Calendar
                            onNavigateToPriceUpdate
                        )
                    }
                )
            }
            
            // Service Management Section
            item {
                DashboardSection(
                    title = "💼 Hizmet Yönetimi",
                    actions = getServiceActions(),
                    onActionClick = { actionId ->
                        selectedAction = actionId
                        handleServiceAction(
                            actionId, 
                            onNavigateToServiceList, 
                            onNavigateToAddService,
                            onNavigateToWorkingHours,
                            onNavigateToBusinessExpenses
                        )
                    }
                )
            }
            
            // Employee Management Section
            item {
                DashboardSection(
                    title = "👥 Kadromuz",
                    actions = getEmployeeActions(),
                    onActionClick = { actionId ->
                        selectedAction = actionId
                        handleEmployeeAction(actionId, onNavigateToEmployeeList, onNavigateToAddEmployee)
                    }
                )
            }
        }
    }
}

/**
 * Dashboard section component with title and action grid
 * Memory efficient: reusable component for different sections
 */
@Composable
private fun DashboardSection(
    title: String,
    actions: List<ActionItem>,
    onActionClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Section title
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        
        // Action cards grid - Fixed: Use Column + Row layout instead of LazyVerticalGrid
        // This avoids height constraint issues with aspectRatio cards
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Group actions into rows of 2
            val chunkedActions = actions.chunked(2)
            
            chunkedActions.forEach { rowActions ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rowActions.forEach { action ->
                        ActionCard(
                            title = action.title,
                            icon = action.icon,
                            backgroundColor = action.backgroundColor,
                            contentColor = action.contentColor,
                            onClick = { onActionClick(action.id) },
                            enabled = action.enabled,
                            modifier = Modifier.weight(1f) // Equal width for each card
                        )
                    }
                    
                    // If odd number of items in last row, add spacer for balance
                    if (rowActions.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

/**
 * Action item data class for dashboard cards
 * Immutable data structure for memory efficiency
 */
data class ActionItem(
    val id: String,
    val title: String,
    val icon: ImageVector,
    val backgroundColor: Color,
    val contentColor: Color = Color.White,
    val enabled: Boolean = true
)

/**
 * Quick actions configuration - Updated layout
 * Row 1: Müşteri Ekle | Randevu Oluştur
 * Row 2: Müşteri Notları | Fiyatları Güncelle
 */
private fun getQuickActions(): List<ActionItem> = listOf(
    ActionItem(
        id = "add_customer",
        title = "Müşteri Ekle",
        icon = Icons.Default.PersonAdd,
        backgroundColor = Color(0xFF4CAF50) // Green
    ),
    ActionItem(
        id = "create_appointment",
        title = "Randevu Oluştur",
        icon = Icons.Default.EventAvailable,
        backgroundColor = Color(0xFF2196F3) // Blue
    ),
    ActionItem(
        id = "customer_notes",
        title = "Müşteri Notları",
        icon = Icons.Default.Description,
        backgroundColor = Color(0xFF9C27B0) // Purple
    ),
    ActionItem(
        id = "update_prices",
        title = "Fiyatları Güncelle",
        icon = Icons.Default.AttachMoney,
        backgroundColor = Color(0xFFE91E63) // Pink
    )
)

/**
 * Service management actions configuration - Updated layout
 * Row 1: Hizmetlerimiz | Yeni Hizmet Ekle
 * Row 2: Çalışma Saatlerimiz | İşletme Giderleri
 */
private fun getServiceActions(): List<ActionItem> = listOf(
    ActionItem(
        id = "view_services",
        title = "Hizmetlerimiz",
        icon = Icons.Default.MedicalServices,
        backgroundColor = Color(0xFF607D8B) // Blue Grey
    ),
    ActionItem(
        id = "add_service",
        title = "Yeni Hizmet Ekle",
        icon = Icons.Default.AddBusiness,
        backgroundColor = Color(0xFF795548) // Brown
    ),
    ActionItem(
        id = "working_hours",
        title = "Çalışma Saatlerimiz",
        icon = Icons.Default.Schedule,
        backgroundColor = Color(0xFF4CAF50) // Green
    ),
    ActionItem(
        id = "business_expenses",
        title = "İşletme Giderleri",
        icon = Icons.Default.Receipt,
        backgroundColor = Color(0xFFFF6B35), // Orange-red for expenses
        enabled = true // Now enabled for expense management
    )
)

/**
 * Employee management actions configuration - Unchanged
 */
private fun getEmployeeActions(): List<ActionItem> = listOf(
    ActionItem(
        id = "view_employees",
        title = "Çalışanlarımız",
        icon = Icons.Default.Groups,
        backgroundColor = Color(0xFF3F51B5) // Indigo
    ),
    ActionItem(
        id = "add_employee",
        title = "Yeni Çalışan Ekle",
        icon = Icons.Default.PersonAddAlt1,
        backgroundColor = Color(0xFF009688) // Teal
    )
)

/**
 * Handle quick action clicks - Updated with appointment creation navigation
 */
private fun handleQuickAction(
    actionId: String,
    onNavigateToAddCustomer: () -> Unit,
    onNavigateToCustomerNotes: () -> Unit,
    onNavigateToAddAppointment: () -> Unit, // Updated: Use AddAppointment instead of Calendar
    onNavigateToPriceUpdate: () -> Unit
) {
    when (actionId) {
        "add_customer" -> {
            onNavigateToAddCustomer()
        }
        "create_appointment" -> {
            onNavigateToAddAppointment() // Updated: Navigate to AddAppointment directly
        }
        "customer_notes" -> {
            onNavigateToCustomerNotes()
        }
        "update_prices" -> {
            onNavigateToPriceUpdate()
        }
    }
}

/**
 * Handle service management action clicks - Updated with working hours and business expenses
 */
private fun handleServiceAction(
    actionId: String,
    onNavigateToServiceList: () -> Unit,
    onNavigateToAddService: () -> Unit,
    onNavigateToWorkingHours: () -> Unit,
    onNavigateToBusinessExpenses: () -> Unit
) {
    when (actionId) {
        "view_services" -> {
            onNavigateToServiceList()
        }
        "add_service" -> {
            onNavigateToAddService()
        }
        "working_hours" -> {
            onNavigateToWorkingHours()
        }
        "business_expenses" -> {
            onNavigateToBusinessExpenses()
        }
    }
}

/**
 * Handle employee management action clicks - Unchanged
 */
private fun handleEmployeeAction(
    actionId: String,
    onNavigateToEmployeeList: () -> Unit,
    onNavigateToAddEmployee: () -> Unit
) {
    when (actionId) {
        "view_employees" -> {
            onNavigateToEmployeeList()
        }
        "add_employee" -> {
            onNavigateToAddEmployee()
        }
    }
} 