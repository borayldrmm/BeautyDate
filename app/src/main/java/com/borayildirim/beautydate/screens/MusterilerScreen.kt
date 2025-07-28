package com.borayildirim.beautydate.screens

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.borayildirim.beautydate.data.models.Customer
import com.borayildirim.beautydate.utils.ToastUtils
import com.borayildirim.beautydate.viewmodels.CustomerViewModel
import com.borayildirim.beautydate.viewmodels.AuthViewModel
import com.borayildirim.beautydate.components.CustomerFab
import com.borayildirim.beautydate.components.CustomerItem
import com.borayildirim.beautydate.components.CustomerSearchBar
import com.borayildirim.beautydate.components.LoadingWithBreathingLogo
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.google.firebase.auth.FirebaseAuth

/**
 * Customer management screen with swipe-to-refresh functionality
 * Memory efficient: LazyColumn with offline-first data sync
 * Features: Search, sorting, real-time sync, cross-device compatibility
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusterilerScreen(
    onNavigateToAddCustomer: () -> Unit,
    onNavigateToCustomerDetail: (Customer) -> Unit,
    onNavigateToEditCustomer: ((Customer) -> Unit)? = null,
    customerViewModel: CustomerViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by customerViewModel.uiState.collectAsStateWithLifecycle()
    val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()
    val context = LocalContext.current
    
    // SwipeRefresh state - Memory efficient: only recreate when needed
    val swipeRefreshState = rememberSwipeRefreshState(
        isRefreshing = uiState.isSyncing || uiState.isLoading
    )
    
    // Initialize customers when screen loads
    LaunchedEffect(Unit) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUser?.uid?.let { businessId ->
            customerViewModel.initializeCustomers()
        }
    }
    
    // Auto-sync when screen becomes visible again (for cross-device consistency)
    LaunchedEffect(Unit) {
        currentUser?.let { user ->
            customerViewModel.syncCustomers()
        }
    }

    // Show success messages with Toast - Memory efficient: reuse same context
    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let { message ->
            // Only show messages that are not customer addition messages (handled in AddCustomerScreen)
            if (!message.contains("kaydedildi")) {
                ToastUtils.showSuccess(context, message)
            }
            customerViewModel.clearSuccess()
        }
    }

    // Show error messages with Toast - Memory efficient: reuse same context
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            ToastUtils.showError(context, message)
            customerViewModel.clearError()
        }
    }

    // Show loading state if no user is authenticated
    if (currentUser == null) {
        LoadingWithBreathingLogo(
            message = "Kullanıcı doğrulanıyor...",
            subMessage = "Lütfen bekleyiniz",
            modifier = Modifier.fillMaxSize()
        )
        return
    }

    // Calculate filtered customers - Memory efficient: computed property
    val filteredCustomers = remember(uiState.customers, uiState.searchQuery) {
        if (uiState.searchQuery.isBlank()) {
            uiState.customers
        } else {
            uiState.customers.filter { customer ->
                customer.firstName.contains(uiState.searchQuery, ignoreCase = true) ||
                customer.lastName.contains(uiState.searchQuery, ignoreCase = true) ||
                customer.phoneNumber.contains(uiState.searchQuery, ignoreCase = true) ||
                customer.email.contains(uiState.searchQuery, ignoreCase = true)
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column {
            // Top App Bar
            TopAppBar(
                title = {
                    Text(
                        text = "Müşteriler",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                },
                actions = {
                    // Manual refresh button with loading indicator
                    IconButton(
                        onClick = { customerViewModel.syncCustomers() },
                        enabled = !uiState.isSyncing
                    ) {
                        if (uiState.isSyncing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Yenile",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            )

            // Search Bar
            CustomerSearchBar(
                searchQuery = uiState.searchQuery,
                onQueryChange = { query ->
                    customerViewModel.searchCustomers(query)
                },
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Sync Progress Indicator - Memory efficient: only show when needed
            if (uiState.isSyncing && uiState.customers.isNotEmpty()) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Main content with swipe refresh
            SwipeRefresh(
                state = swipeRefreshState,
                onRefresh = { customerViewModel.syncCustomers() }
            ) {
                // Customer list or empty state
                if (uiState.isLoading && uiState.customers.isEmpty()) {
                    // Initial loading
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Müşteriler yükleniyor...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else if (filteredCustomers.isEmpty() && uiState.searchQuery.isBlank()) {
                    // Empty state with add customer guidance
                    EmptyCustomerState(
                        onAddCustomer = onNavigateToAddCustomer,
                        modifier = Modifier.fillMaxSize()
                    )
                } else if (filteredCustomers.isEmpty() && uiState.searchQuery.isNotBlank()) {
                    // No search results
                    NoSearchResultsState(
                        searchQuery = uiState.searchQuery,
                        onClearSearch = { customerViewModel.searchCustomers("") },
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    // Customer list
                    LazyColumn(
                        contentPadding = PaddingValues(bottom = 80.dp), // Space for FAB
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(
                            items = filteredCustomers,
                            key = { customer -> customer.id }
                        ) { customer ->
                            CustomerItem(
                                customer = customer,
                                onClick = { onNavigateToCustomerDetail(customer) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                            )
                        }
                    }
                }
            }
        }

        // Floating Action Button - Memory efficient: positioned with Box
        CustomerFab(
            onAddClick = onNavigateToAddCustomer,
            onFilterClick = { /* TODO: Implement filter */ },
            onExportClick = { /* TODO: Implement export */ },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        )
    }
}

/**
 * Empty state when no customers exist
 */
@Composable
private fun EmptyCustomerState(
    onAddCustomer: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.People,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(64.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Henüz müşteri yok",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "İlk müşterinizi ekleyerek başlayın",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onAddCustomer,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.PersonAdd,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Müşteri Ekle")
            }
        }
    }
}

/**
 * No search results state
 */
@Composable
private fun NoSearchResultsState(
    searchQuery: String,
    onClearSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
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
                text = "Sonuç bulunamadı",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "'$searchQuery' için müşteri bulunamadı",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onClearSearch,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Aramayı Temizle")
            }
        }
    }
} 