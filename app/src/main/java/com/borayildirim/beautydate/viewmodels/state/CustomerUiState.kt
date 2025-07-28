package com.borayildirim.beautydate.viewmodels.state

import com.borayildirim.beautydate.data.models.Customer

/**
 * UI state for customer management screens
 * Holds all state needed for customer listing and management
 */
data class CustomerUiState(
    val customers: List<Customer> = emptyList(),
    val filteredCustomers: List<Customer> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isSearching: Boolean = false,
    val isOnline: Boolean = true,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val totalCustomers: Int = 0,
    val selectedCustomer: Customer? = null,
    val showAddCustomerSheet: Boolean = false,
    val isSyncing: Boolean = false
) {
    /**
     * Returns customers to display based on search query
     */
    val displayCustomers: List<Customer>
        get() = if (searchQuery.isBlank()) customers else filteredCustomers
        
    /**
     * Returns true if no customers are available
     */
    val isEmpty: Boolean
        get() = customers.isEmpty() && !isLoading
        
    /**
     * Returns true if search results are empty but there are customers
     */
    val isSearchEmpty: Boolean
        get() = searchQuery.isNotBlank() && filteredCustomers.isEmpty() && customers.isNotEmpty()
} 