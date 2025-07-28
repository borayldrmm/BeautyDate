package com.borayildirim.beautydate.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.borayildirim.beautydate.data.models.Employee
import com.borayildirim.beautydate.data.models.EmployeeGender
import com.borayildirim.beautydate.data.models.EmployeePermission
import com.borayildirim.beautydate.data.repository.EmployeeRepository
import com.borayildirim.beautydate.utils.NetworkMonitor
import com.borayildirim.beautydate.utils.AuthUtil
import com.borayildirim.beautydate.viewmodels.state.EmployeeUiState
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for employee management functionality
 * Handles all employee-related operations following MVVM pattern
 * Multi-tenant architecture: BusinessId handled automatically by AuthUtil
 * Memory efficient: Flow-based reactive data and minimal object creation
 */
@HiltViewModel
class EmployeeViewModel @Inject constructor(
    private val employeeRepository: EmployeeRepository,
    private val networkMonitor: NetworkMonitor,
    private val firebaseAuth: FirebaseAuth,
    private val authUtil: AuthUtil
) : ViewModel() {

    private val _uiState = MutableStateFlow(EmployeeUiState())
    val uiState: StateFlow<EmployeeUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null
    private var networkMonitorJob: Job? = null
    private var isInitialized: Boolean = false
    
    init {
        // Start monitoring network connectivity
        startNetworkMonitoring()
    }

    /**
     * Initializes employee data with automatic authentication check
     * Memory efficient: reuses existing data if already loaded
     * BusinessId handled automatically by AuthUtil
     */
    fun initializeEmployees() {
        
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
        
        if (isInitialized && _uiState.value.employees.isNotEmpty()) {
            _uiState.value = _uiState.value.copy(isLoading = false)
            return // Already initialized
        }
        
        isInitialized = true
        
        // Perform initial sync to ensure offline functionality
        performInitialSyncIfNeeded()
        
        // Load employees from local database
        loadEmployees()
    }
    
    /**
     * Manually triggers sync with loading state management
     * Used for refresh button functionality
     * BusinessId handled automatically by AuthUtil
     */
    fun manualSync() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    isSyncing = true,
                    errorMessage = null
                )
                
                // Call syncWithFirestore without parameters
                val result = employeeRepository.syncWithFirestore()
                result.fold(
                    onSuccess = {
                        _uiState.value = _uiState.value.copy(
                            isSyncing = false,
                            successMessage = "Çalışanlar başarıyla senkronize edildi"
                        )
                    },
                    onFailure = { exception ->
                        _uiState.value = _uiState.value.copy(
                            isSyncing = false,
                            errorMessage = "Senkronizasyon hatası: ${exception.message}"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSyncing = false,
                    errorMessage = "Senkronizasyon hatası: ${e.message}"
                )
            }
        }
    }

    /**
     * Performs initial sync if needed (on first app launch or new business)
     * Memory efficient: only performs sync when necessary
     * BusinessId handled automatically by AuthUtil
     */
    private fun performInitialSyncIfNeeded() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                    isSyncing = true,
                    errorMessage = null
                )
                
                employeeRepository.performInitialSync()
                    .onSuccess {
                        _uiState.value = _uiState.value.copy(
                            isSyncing = false
                        )
                    }
                    .onFailure { error ->
                        _uiState.value = _uiState.value.copy(
                            isSyncing = false,
                            errorMessage = "Senkronizasyon hatası: ${error.message}"
                        )
                    }
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSyncing = false,
                    errorMessage = "Senkronizasyon hatası: ${e.message}"
                )
            }
        }
    }

    /**
     * Loads employees for the business
     * Memory efficient: Flow-based reactive loading
     * BusinessId handled automatically by AuthUtil
     */
    private fun loadEmployees() {
        // Check authentication before loading employees
        if (!authUtil.isUserAuthenticated()) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                errorMessage = authUtil.getAuthErrorMessage()
            )
            return
        }
        
        val businessId = authUtil.getCurrentBusinessIdSafe()
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            val employeeFlow = if (_uiState.value.showActiveOnly) {
                employeeRepository.getActiveEmployees()
            } else {
                employeeRepository.getAllEmployees()
            }
            
            employeeFlow
                .catch { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Çalışanlar yüklenirken hata oluştu: ${error.message}"
                    )
                }
                .collect { employees ->
                    employees.forEach { employee ->
                    }
                    
                    _uiState.value = _uiState.value.copy(
                        employees = employees,
                        isLoading = false,
                        totalEmployees = employees.size,
                        errorMessage = null
                    )
                    
                    // Update filtered employees if there's a search query
                    if (_uiState.value.searchQuery.isNotBlank()) {
                        searchEmployees(_uiState.value.searchQuery)
                    }
                }
        }
    }

    /**
     * Searches employees by query (name, phone, or email)
     * Memory efficient: debounced search with minimal object creation
     * @param query Search query
     */
    fun searchEmployees(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            if (query.isBlank()) {
                _uiState.value = _uiState.value.copy(filteredEmployees = emptyList())
                return@launch
            }
            
            // Add slight delay for better UX (debouncing)
            delay(300)
            
            val businessId = authUtil.getCurrentBusinessIdSafe()
            if (businessId.isEmpty()) {
                return@launch
            }
            
            employeeRepository.searchEmployees(query)
                .catch { error ->
                }
                .collect { filteredEmployees ->
                    _uiState.value = _uiState.value.copy(filteredEmployees = filteredEmployees)
                }
        }
    }
    
    /**
     * Filters employees by gender
     * Memory efficient: uses computed property in UI state
     * @param gender Gender to filter by (null for all)
     */
    fun filterByGender(gender: EmployeeGender?) {
        _uiState.value = _uiState.value.copy(selectedGender = gender)
    }
    
    /**
     * Filters employees by permission
     * @param permission Permission to filter by (null for all)
     */
    fun filterByPermission(permission: EmployeePermission?) {
        _uiState.value = _uiState.value.copy(selectedPermission = permission)
    }
    
    /**
     * Filters employees by skill
     * @param skill Skill to filter by (null for all)
     */
    fun filterBySkill(skill: String?) {
        _uiState.value = _uiState.value.copy(selectedSkill = skill)
    }
    
    /**
     * Toggles active/all employees display
     */
    fun toggleActiveFilter(showActiveOnly: Boolean) {
        _uiState.value = _uiState.value.copy(showActiveOnly = showActiveOnly)
        // Reload employees with new filter
        val businessId = authUtil.getCurrentBusinessIdSafe()
        if (businessId.isNotEmpty()) {
            loadEmployees()
        }
    }

    /**
     * Adds new employee with validation and sync
     * Memory efficient: immediate local insert with background sync
     * @param employee Employee to add
     */
    fun addEmployee(employee: Employee) {
        // Check authentication before adding employee
        if (!authUtil.isUserAuthenticated()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = authUtil.getAuthErrorMessage()
            )
            return
        }
        
        val businessId = authUtil.getCurrentBusinessIdSafe()
        
        viewModelScope.launch {
            // Ensure we have a valid business ID
            val actualBusinessId = if (businessId.isBlank()) {
                firebaseAuth.currentUser?.uid ?: ""
            } else {
                businessId
            }
            
            val employeeWithBusinessId = employee.copy(businessId = actualBusinessId)
            
            
            employeeRepository.addEmployee(employeeWithBusinessId)
                .onSuccess { addedEmployee ->
                    _uiState.value = _uiState.value.copy(
                        successMessage = "${addedEmployee.firstName} ${addedEmployee.lastName} başarılı bir şekilde kaydedildi",
                        showAddEmployeeSheet = false
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        errorMessage = error.message ?: "Çalışan eklenirken hata oluştu"
                    )
                }
        }
    }

    /**
     * Updates existing employee
     * Memory efficient: direct entity update with automatic sync
     * @param employee Updated employee data
     */
    fun updateEmployee(employee: Employee) {
        // Check authentication before updating employee
        if (!authUtil.isUserAuthenticated()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = authUtil.getAuthErrorMessage()
            )
            return
        }
        
        val businessId = authUtil.getCurrentBusinessIdSafe()
        
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                    errorMessage = null,
                    successMessage = null
                )
                
                // Memory efficient: reuse existing businessId
                val actualBusinessId = if (businessId.isBlank()) {
                    firebaseAuth.currentUser?.uid ?: ""
                } else {
                    businessId
                }
                
                // Ensure business ID is set
                val employeeWithBusinessId = employee.copy(businessId = actualBusinessId)
                
                employeeRepository.updateEmployee(employeeWithBusinessId)
                    .onSuccess { updatedEmployee ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            successMessage = "${updatedEmployee.firstName} ${updatedEmployee.lastName} başarılı bir şekilde güncellendi",
                            selectedEmployee = updatedEmployee
                        )
                        
                        // Refresh employee list after successful update
                        loadEmployees()
                    }
                    .onFailure { error ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Çalışan güncellenirken hata oluştu"
                        )
                    }
                    
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Çalışan güncellenirken hata oluştu: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Deletes an employee and updates UI state
     * Memory efficient: immediate UI update with background sync
     */
    fun deleteEmployee(employeeId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                val result = employeeRepository.deleteEmployee(employeeId)
                if (result.isSuccess) {
                    // Update local state to remove the employee immediately
                    val currentState = _uiState.value
                    val updatedEmployees = currentState.employees.filter { it.id != employeeId }
                    val updatedFiltered = currentState.filteredEmployees.filter { it.id != employeeId }
                    val updatedSelected = currentState.selectedEmployees - employeeId
                    
                    _uiState.value = currentState.copy(
                        employees = updatedEmployees,
                        filteredEmployees = updatedFiltered,
                        selectedEmployees = updatedSelected,
                        isLoading = false,
                        successMessage = "Çalışan başarıyla silindi"
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false, 
                        errorMessage = "Çalışan silinemedi: ${result.exceptionOrNull()?.message}"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false, 
                    errorMessage = "Beklenmeyen hata: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Toggles employee active status
     * Memory efficient: direct repository call
     */
    fun toggleEmployeeStatus(employeeId: String) {
        viewModelScope.launch {
            try {
                // Find current employee to get current status
                val currentEmployee = _uiState.value.employees.find { it.id == employeeId }
                if (currentEmployee == null) {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Çalışan bulunamadı"
                    )
                    return@launch
                }
                
                val newStatus = !currentEmployee.isActive
                
                employeeRepository.toggleEmployeeStatus(employeeId, newStatus)
                    .onSuccess {
                        val statusText = if (newStatus) "aktif" else "pasif"
                        _uiState.value = _uiState.value.copy(
                            successMessage = "Çalışan durumu $statusText olarak güncellendi"
                        )
                        // Refresh list to show updated status
                        val businessId = authUtil.getCurrentBusinessIdSafe()
                        if (businessId.isNotEmpty()) {
                            loadEmployees()
                        }
                    }
                    .onFailure { error ->
                        _uiState.value = _uiState.value.copy(
                            errorMessage = "Durum güncelleme hatası: ${error.message}"
                        )
                    }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Durum güncelleme hatası: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Updates employee permissions
     * Memory efficient: direct repository call with JSON serialization
     */
    fun updateEmployeePermissions(employeeId: String, permissions: List<EmployeePermission>) {
        viewModelScope.launch {
            try {
                employeeRepository.updateEmployeePermissions(employeeId, permissions)
                    .onSuccess {
                        _uiState.value = _uiState.value.copy(
                            successMessage = "Yetkiler başarıyla güncellendi",
                            showPermissionUpdateSheet = false
                        )
                        // Refresh list to show updated permissions
                        val businessId = authUtil.getCurrentBusinessIdSafe()
                        if (businessId.isNotEmpty()) {
                            loadEmployees()
                        }
                    }
                    .onFailure { error ->
                        _uiState.value = _uiState.value.copy(
                            errorMessage = "Yetki güncelleme hatası: ${error.message}"
                        )
                    }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Yetki güncelleme hatası: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Updates employee skills
     * Memory efficient: direct repository call with JSON serialization
     */
    fun updateEmployeeSkills(employeeId: String, skills: List<String>) {
        viewModelScope.launch {
            try {
                employeeRepository.updateEmployeeSkills(employeeId, skills)
                    .onSuccess {
                        _uiState.value = _uiState.value.copy(
                            successMessage = "Yetenekler başarıyla güncellendi",
                            showSkillUpdateSheet = false
                        )
                        // Refresh list to show updated skills
                        val businessId = authUtil.getCurrentBusinessIdSafe()
                        if (businessId.isNotEmpty()) {
                            loadEmployees()
                        }
                    }
                    .onFailure { error ->
                        _uiState.value = _uiState.value.copy(
                            errorMessage = "Yetenek güncelleme hatası: ${error.message}"
                        )
                    }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Yetenek güncelleme hatası: ${e.message}"
                )
            }
        }
    }

    /**
     * Shows/hides add employee bottom sheet
     */
    fun showAddEmployeeSheet(show: Boolean) {
        _uiState.value = _uiState.value.copy(showAddEmployeeSheet = show)
    }
    
    /**
     * Shows/hides permission update sheet
     */
    fun showPermissionUpdateSheet(show: Boolean) {
        _uiState.value = _uiState.value.copy(showPermissionUpdateSheet = show)
    }
    
    /**
     * Shows/hides skill update sheet
     */
    fun showSkillUpdateSheet(show: Boolean) {
        _uiState.value = _uiState.value.copy(showSkillUpdateSheet = show)
    }
    
    /**
     * Toggles selection mode for bulk operations
     * Memory efficient: simple boolean toggle
     */
    fun toggleSelectionMode() {
        val currentMode = _uiState.value.isInSelectionMode
        _uiState.value = _uiState.value.copy(
            isInSelectionMode = !currentMode,
            selectedEmployees = if (!currentMode) emptySet() else _uiState.value.selectedEmployees
        )
    }
    
    /**
     * Toggles employee selection for bulk operations
     * Memory efficient: Set operations for optimal performance
     */
    fun toggleEmployeeSelection(employeeId: String) {
        val currentSelected = _uiState.value.selectedEmployees
        val newSelected = if (employeeId in currentSelected) {
            currentSelected - employeeId
        } else {
            currentSelected + employeeId
        }
        _uiState.value = _uiState.value.copy(selectedEmployees = newSelected)
    }
    
    /**
     * Selects all visible employees
     * Memory efficient: converts to Set for optimal lookup performance
     */
    fun selectAllEmployees() {
        val allVisibleIds = _uiState.value.displayEmployees.map { it.id }.toSet()
        _uiState.value = _uiState.value.copy(selectedEmployees = allVisibleIds)
    }
    
    /**
     * Clears all selected employees
     */
    fun clearSelection() {
        _uiState.value = _uiState.value.copy(
            selectedEmployees = emptySet(),
            isInSelectionMode = false
        )
    }

    /**
     * Syncs employees with Firestore (manual refresh)
     * @param showSuccessMessage Whether to show success message after sync
     */
    fun syncEmployees(showSuccessMessage: Boolean = true) {
        val businessId = authUtil.getCurrentBusinessIdSafe()
        if (businessId.isEmpty()) {
            return
        }
        
        // Check authentication before syncing
        if (!authUtil.isUserAuthenticated()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = authUtil.getAuthErrorMessage()
            )
            return
        }
        
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSyncing = true, errorMessage = null)
            
            employeeRepository.syncWithFirestore()
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isSyncing = false,
                        successMessage = if (showSuccessMessage) "Veriler başarıyla senkronize edildi" else null
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isSyncing = false,
                        errorMessage = "Senkronizasyon hatası: ${error.message}"
                    )
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
    fun clearSuccess() {
        _uiState.value = _uiState.value.copy(successMessage = null)
    }

    /**
     * Clears selected employee
     */
    fun clearSelectedEmployee() {
        _uiState.value = _uiState.value.copy(selectedEmployee = null)
    }
    
    /**
     * Selects an employee for detail view or editing
     * Memory efficient: simple state update
     * @param employee Employee to select
     */
    fun selectEmployee(employee: Employee) {
        _uiState.value = _uiState.value.copy(selectedEmployee = employee)
    }
    
    /**
     * Selects an employee by ID for navigation purposes
     * Memory efficient: finds from existing list to avoid database query
     * @param employeeId Employee ID to select
     */
    fun selectEmployeeById(employeeId: String) {
        val employee = _uiState.value.employees.find { it.id == employeeId }
        if (employee != null) {
            _uiState.value = _uiState.value.copy(selectedEmployee = employee)
        } else {
        }
    }
    
    /**
     * Checks if an employee phone number already exists (for validation)
     * @param phoneNumber Phone number to check
     * @param excludeEmployeeId Employee ID to exclude from check (for updates)
     * @return True if phone number exists
     */
    suspend fun phoneNumberExists(phoneNumber: String, excludeEmployeeId: String = ""): Boolean {
        return try {
            employeeRepository.phoneNumberExists(phoneNumber, excludeEmployeeId)
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Checks if email already exists for another employee
     * Memory efficient: single database query
     * @param email The email to check
     * @param excludeEmployeeId Employee ID to exclude from check (for editing)
     * @return True if email exists
     */
    suspend fun emailExists(email: String, excludeEmployeeId: String = ""): Boolean {
        return try {
            employeeRepository.emailExists(email, excludeEmployeeId)
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Validates phone number uniqueness
     * Memory efficient: single database query for validation
     */
    private suspend fun validatePhoneNumber(phoneNumber: String, excludeEmployeeId: String = ""): Boolean {
        return try {
            !employeeRepository.phoneNumberExists(phoneNumber, excludeEmployeeId)
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Validates email uniqueness for new employee
     * Memory efficient: single database query with business filtering
     */
    private suspend fun validateEmail(email: String, excludeEmployeeId: String = ""): Boolean {
        return try {
            !employeeRepository.emailExists(email, excludeEmployeeId)
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Gets total employee count
     * Memory efficient: single count query
     */
    suspend fun getEmployeeCount(): Int {
        return try {
            employeeRepository.getEmployeeCount()
        } catch (e: Exception) {
            0
        }
    }

    /**
     * Gets active employee count
     * Memory efficient: status-filtered count query
     */
    suspend fun getActiveEmployeeCount(): Int {
        return try {
            employeeRepository.getEmployeeCountByStatus(true)
        } catch (e: Exception) {
            0
        }
    }

    /**
     * Syncs all employee data manually
     * Memory efficient: comprehensive sync operation
     */
    fun syncAllEmployees() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                
                val result = employeeRepository.syncWithFirestore()
                
                _uiState.value = if (result.isSuccess) {
                    _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Tüm çalışan verileri senkronize edildi"
                    )
                } else {
                    _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.exceptionOrNull()?.message ?: "Senkronizasyon hatası"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Bilinmeyen hata"
                )
            }
        }
    }

    /**
     * Starts monitoring network connectivity for automatic sync
     * Memory efficient: single coroutine with Flow collection
     */
    private fun startNetworkMonitoring() {
        networkMonitorJob = viewModelScope.launch {
            networkMonitor.isConnected.collect { isConnected ->
                
                val businessId = authUtil.getCurrentBusinessIdSafe()
                if (isConnected && businessId.isNotEmpty()) {
                    // Internet came back - trigger sync for pending changes
                    
                    employeeRepository.syncWithFirestore()
                        .onSuccess {
                        }
                        .onFailure { error ->
                        }
                }
            }
        }
    }

    /**
     * Clears success and error messages
     * Memory efficient: selective state clearing
     */
    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            successMessage = null,
            errorMessage = null
        )
    }

    /**
     * Clean up resources when ViewModel is destroyed
     * Memory efficient: cancels all running coroutines
     */
    override fun onCleared() {
        super.onCleared()
        searchJob?.cancel()
        networkMonitorJob?.cancel()
    }
} 