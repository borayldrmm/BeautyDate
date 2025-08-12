package com.borayildirim.beautydate.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.borayildirim.beautydate.data.models.Employee
import com.borayildirim.beautydate.data.models.EmployeeGender
import com.borayildirim.beautydate.data.models.EmployeePermission
import com.borayildirim.beautydate.screens.components.*
import com.borayildirim.beautydate.viewmodels.EmployeeViewModel
import com.google.firebase.auth.FirebaseAuth
import com.borayildirim.beautydate.utils.ToastUtils
import com.borayildirim.beautydate.components.LoadingWithBreathingLogo
import androidx.compose.ui.platform.LocalContext

/**
 * Employee management screen for listing and managing employees
 * Enhanced with filtering, search, detail view and selection features
 * Memory efficient: LazyColumn with reactive state management
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeeScreen(
    onNavigateToAddEmployee: () -> Unit,
    onNavigateToEditEmployee: (String) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: EmployeeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    
    // Initialize employees when screen loads
    LaunchedEffect(Unit) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUser?.uid?.let { businessId ->
            viewModel.initializeEmployees()
        }
    }
    
    // Show success messages with Toast
    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let { message ->
            ToastUtils.showSuccess(context, message)
            viewModel.clearMessages()
        }
    }
    
    // Show error messages with Toast
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            ToastUtils.showError(context, message)
            viewModel.clearMessages()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Çalışanlarımız",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Geri"
                        )
                    }
                },
                actions = {
                    // Sync button
                    IconButton(
                        onClick = { viewModel.syncEmployees() },
                        enabled = !uiState.isSyncing
                    ) {
                        if (uiState.isSyncing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Sync,
                                contentDescription = "Senkronize Et"
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddEmployee,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Çalışan Ekle"
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Search and filter section
            EmployeeSearchAndFilterSection(
                searchQuery = uiState.searchQuery,
                onSearchQueryChange = { viewModel.searchEmployees(it) },
                selectedGender = uiState.selectedGender,
                onGenderFilterChange = { viewModel.filterByGender(it) },
                selectedPermission = uiState.selectedPermission,
                onPermissionFilterChange = { viewModel.filterByPermission(it) },
                selectedSkill = uiState.selectedSkill,
                onSkillFilterChange = { viewModel.filterBySkill(it) },
                availableSkills = uiState.employees.flatMap { it.skills }.distinct(),
                showActiveOnly = uiState.showActiveOnly,
                onActiveFilterChange = { viewModel.toggleActiveFilter(it) },
                isInSelectionMode = uiState.isInSelectionMode,
                onToggleSelectionMode = { viewModel.toggleSelectionMode() },
                selectedCount = uiState.selectedEmployees.size
            )
            
            // Content Section
            Box(modifier = Modifier.weight(1f)) {
                if (uiState.isLoading) {
                    LoadingWithBreathingLogo(
                        message = "Çalışanlar yükleniyor...",
                        subMessage = "Lütfen bekleyiniz",
                        modifier = Modifier.fillMaxSize()
                    )
                } else if (uiState.isEmpty) {
                    EmployeeEmptyState(
                        onAddEmployeeClick = onNavigateToAddEmployee
                    )
                } else if (uiState.isSearchEmpty) {
                    EmployeeSearchEmptyState()
                } else if (uiState.isFilterEmpty) {
                    EmployeeFilterEmptyState()
                } else {
                    EmployeeList(
                        employees = uiState.displayEmployees,
                        isInSelectionMode = uiState.isInSelectionMode,
                        selectedEmployees = uiState.selectedEmployees,
                        onEmployeeClick = { employee ->
                            if (uiState.isInSelectionMode) {
                                viewModel.toggleEmployeeSelection(employee.id)
                            } else {
                                viewModel.selectEmployee(employee)
                                onNavigateToEditEmployee(employee.id)
                            }
                        },
                        onEmployeeSelect = { employeeId ->
                            viewModel.toggleEmployeeSelection(employeeId)
                        },
                        onToggleStatus = { employeeId, isActive ->
                            viewModel.toggleEmployeeStatus(employeeId)
                        }
                    )
                }
            }
        }
    }
}

/**
 * Empty state when search yields no results
 */
@Composable
private fun EmployeeSearchEmptyState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.SearchOff,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Arama sonucu bulunamadı",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Farklı anahtar kelimelerle deneyin",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Empty state when filter yields no results
 */
@Composable
private fun EmployeeFilterEmptyState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.FilterListOff,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Filtre sonucu bulunamadı",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Filtre kriterlerini değiştirmeyi deneyin",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
} 