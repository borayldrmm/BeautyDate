package com.borayildirim.beautydate.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.borayildirim.beautydate.data.models.Customer
import com.borayildirim.beautydate.data.models.CustomerGender
import com.borayildirim.beautydate.domain.usecases.customer.GetCustomersUseCase
import com.borayildirim.beautydate.domain.usecases.customer.AddCustomerUseCase
import com.borayildirim.beautydate.domain.usecases.customer.SyncCustomersUseCase
import com.borayildirim.beautydate.data.repository.CustomerRepository
import com.borayildirim.beautydate.utils.NetworkMonitor
import com.borayildirim.beautydate.utils.AuthUtil
import com.borayildirim.beautydate.viewmodels.state.CustomerUiState
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for customer management functionality
 * Handles all customer-related operations following MVVM pattern
 * Multi-tenant architecture: BusinessId handled automatically by repositories
 */
@HiltViewModel
class CustomerViewModel @Inject constructor(
    private val getCustomersUseCase: GetCustomersUseCase,
    private val addCustomerUseCase: AddCustomerUseCase,
    private val syncCustomersUseCase: SyncCustomersUseCase,
    private val customerRepository: CustomerRepository,
    private val networkMonitor: NetworkMonitor,
    private val firebaseAuth: FirebaseAuth,
    private val authUtil: AuthUtil
) : ViewModel() {

    private val _uiState = MutableStateFlow(CustomerUiState())
    val uiState: StateFlow<CustomerUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null
    private var networkMonitorJob: Job? = null
    private var isInitialized: Boolean = false
    
    init {
        // Start monitoring network connectivity
        startNetworkMonitoring()
    }

    /**
     * Initializes customer data with automatic authentication check
     * BusinessId is handled automatically by repository layer
     */
    fun initializeCustomers() {
        
        // Set loading state immediately for better UX
        _uiState.value = _uiState.value.copy(
            isLoading = true,
            errorMessage = null,
            successMessage = null
        )
        
        // Check if user is authenticated before proceeding
        if (!authUtil.isUserAuthenticated()) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                errorMessage = authUtil.getAuthErrorMessage()
            )
            return
        }
        
        val businessId = authUtil.getCurrentBusinessIdSafe()
        
        // Always perform fresh sync for cross-device data consistency
        // Remove isInitialized check to ensure fresh data on each device
        
        // Check network status before sync
        
        // Perform initial sync to ensure offline functionality
        performInitialSyncIfNeeded()
        
        // Load customers from local database
        loadCustomers()
    }

    /**
     * Syncs customers with Firestore (manual refresh)
     * BusinessId handled automatically by repository
     */
    fun syncCustomers() {
        if (authUtil.isUserAuthenticated()) {
            manualSync()
        }
    }

    /**
     * Manually triggers sync with loading state management
     * Used for refresh button functionality
     * BusinessId handled automatically by repository
     */
    private fun manualSync() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            try {
                val result = customerRepository.syncWithFirestore()
                
                if (result.isSuccess) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Müşteriler başarıyla senkronize edildi"
                    )
                    
                    // Clear success message after delay
                    delay(2000)
                    clearSuccessMessage()
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Senkronizasyon hatası: ${result.exceptionOrNull()?.message}"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Senkronizasyon hatası: ${e.message}"
                )
            }
        }
    }

    /**
     * Performs initial sync if needed for offline functionality
     * BusinessId handled automatically by repository
     */
    private fun performInitialSyncIfNeeded() {
        viewModelScope.launch {
            try {
                val result = customerRepository.performInitialSync()
                result.fold(
                    onSuccess = {
                    },
                    onFailure = { exception ->
                        exception.printStackTrace()
                    }
                )
            } catch (e: Exception) {
                e.printStackTrace()
                // Don't show error for initial sync failure - will work offline
            }
        }
    }

    /**
     * Loads customers from local database using repository
     * BusinessId handled automatically by repository layer
     */
    private fun loadCustomers() {
        // Check authentication before loading customers
        if (!authUtil.isUserAuthenticated()) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                errorMessage = authUtil.getAuthErrorMessage()
            )
            return
        }
        
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            customerRepository.getAllCustomers() // No businessId parameter needed
                .catch { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Müşteriler yüklenirken hata oluştu: ${error.message}"
                    )
                }
                .collect { customers ->
                    customers.forEach { customer ->
                    }
                    
                    _uiState.value = _uiState.value.copy(
                        customers = customers,
                        isLoading = false,
                        totalCustomers = customers.size,
                        errorMessage = null
                    )
                    
                    // Update filtered customers if there's a search query
                    if (_uiState.value.searchQuery.isNotBlank()) {
                        searchCustomers(_uiState.value.searchQuery)
                    }
                }
        }
    }

    /**
     * Searches customers by query
     * @param query Search query (name or phone)
     * BusinessId handled automatically by repository
     * Transforms phone number queries to remove formatting for better search
     */
    fun searchCustomers(query: String) {
        // Cancel previous search job for memory efficiency
        searchJob?.cancel()
        
        _uiState.value = _uiState.value.copy(
            searchQuery = query,
            isSearching = true
        )
        
        searchJob = viewModelScope.launch {
            // Small delay for better UX (debounce effect)
            delay(300)
            
            try {
                customerRepository.searchCustomers(query) // No businessId parameter needed
                    .catch { error ->
                        _uiState.value = _uiState.value.copy(
                            isSearching = false,
                            errorMessage = "Arama sırasında hata oluştu: ${error.message}"
                        )
                    }
                    .collect { searchResults ->
                        _uiState.value = _uiState.value.copy(
                            isSearching = false,
                            customers = searchResults
                        )
                    }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSearching = false,
                    errorMessage = "Arama sırasında hata oluştu: ${e.message}"
                )
            }
        }
    }

    /**
     * Adds a new customer
     * BusinessId automatically set by repository layer
     */
    fun addCustomer(
        firstName: String,
        lastName: String,
        phoneNumber: String,
        email: String,
        birthDate: String,
        gender: CustomerGender,
        notes: String
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            try {
                // Check if phone number already exists
                val phoneExists = customerRepository.phoneNumberExists(phoneNumber) // No businessId parameter needed
                if (phoneExists) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Bu telefon numarası zaten kayıtlı"
                    )
                    return@launch
                }
                
                val customer = Customer(
                    firstName = firstName,
                    lastName = lastName,
                    phoneNumber = phoneNumber,
                    email = email,
                    birthDate = birthDate,
                    gender = gender,
                    notes = notes
                    // businessId will be set automatically by repository
                )
                
                val result = customerRepository.addCustomer(customer)
                
                if (result.isSuccess) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Müşteri başarıyla eklendi"
                    )
                    
                    // Clear success message after delay
                    delay(2000)
                    clearSuccessMessage()
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Müşteri eklenirken hata oluştu: ${result.exceptionOrNull()?.message}"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Müşteri eklenirken hata oluştu: ${e.message}"
                )
            }
        }
    }

    /**
     * Updates an existing customer
     * BusinessId validation handled by repository layer
     */
    fun updateCustomer(customer: Customer) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            try {
                val result = customerRepository.updateCustomer(customer)
                
                if (result.isSuccess) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Müşteri başarıyla güncellendi"
                    )
                    
                    // Clear success message after delay
                    delay(2000)
                    clearSuccessMessage()
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Müşteri güncellenirken hata oluştu: ${result.exceptionOrNull()?.message}"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Müşteri güncellenirken hata oluştu: ${e.message}"
                )
            }
        }
    }

    /**
     * Deletes a customer
     * BusinessId validation handled by repository layer
     */
    fun deleteCustomer(customerId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            try {
                val result = customerRepository.deleteCustomer(customerId)
                
                if (result.isSuccess) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Müşteri başarıyla silindi"
                    )
                    
                    // Clear success message after delay
                    delay(2000)
                    clearSuccessMessage()
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Müşteri silinirken hata oluştu: ${result.exceptionOrNull()?.message}"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Müşteri silinirken hata oluştu: ${e.message}"
                )
            }
        }
    }

    /**
     * Starts monitoring network connectivity for sync operations
     */
    private fun startNetworkMonitoring() {
        networkMonitorJob = viewModelScope.launch {
            networkMonitor.isConnected.collect { isConnected ->
                _uiState.value = _uiState.value.copy(isOnline = isConnected)
                
                // Auto-sync when network becomes available
                if (isConnected && authUtil.isUserAuthenticated()) {
                    performBackgroundSync()
                }
            }
        }
    }

    /**
     * Performs background sync when network becomes available
     * BusinessId handled automatically by repository
     */
    private fun performBackgroundSync() {
        viewModelScope.launch {
            try {
                customerRepository.syncWithFirestore()
            } catch (e: Exception) {
                // Silent failure for background sync
            }
        }
    }

    /**
     * Clears error message
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    /**
     * Clears success message
     */
    fun clearSuccessMessage() {
        _uiState.value = _uiState.value.copy(successMessage = null)
    }

    /**
     * Clears success message (alias for UI compatibility)
     */
    fun clearSuccess() {
        clearSuccessMessage()
    }

    /**
     * Selects a customer for detail view or operations
     */
    fun selectCustomer(customer: Customer) {
        _uiState.value = _uiState.value.copy(selectedCustomer = customer)
    }

    /**
     * Cleanup when ViewModel is destroyed
     */
    override fun onCleared() {
        super.onCleared()
        searchJob?.cancel()
        networkMonitorJob?.cancel()
    }
} 