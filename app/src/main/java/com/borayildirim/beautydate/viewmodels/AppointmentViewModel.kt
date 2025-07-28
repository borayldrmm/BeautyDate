package com.borayildirim.beautydate.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.borayildirim.beautydate.data.models.Appointment
import com.borayildirim.beautydate.data.models.AppointmentStatus
import com.borayildirim.beautydate.data.models.Customer
import com.borayildirim.beautydate.data.models.Payment
import com.borayildirim.beautydate.data.models.PaymentMethod
import com.borayildirim.beautydate.data.models.PaymentStatus
import com.borayildirim.beautydate.data.repository.AppointmentRepository
import com.borayildirim.beautydate.data.repository.CustomerRepository
import com.borayildirim.beautydate.data.repository.PaymentRepository
import com.borayildirim.beautydate.utils.NetworkMonitor
import com.borayildirim.beautydate.utils.AuthUtil
import com.borayildirim.beautydate.viewmodels.state.AppointmentUiState
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
 * ViewModel for appointment management functionality
 * Handles all appointment-related operations following MVVM pattern
 * Multi-tenant architecture: BusinessId handled automatically by AuthUtil
 * Memory efficient: Flow-based reactive data and minimal object creation
 */
@HiltViewModel
class AppointmentViewModel @Inject constructor(
    private val appointmentRepository: AppointmentRepository,
    private val customerRepository: CustomerRepository,
    private val paymentRepository: PaymentRepository,
    private val networkMonitor: NetworkMonitor,
    private val firebaseAuth: FirebaseAuth,
    private val authUtil: AuthUtil
) : ViewModel() {

    private val _uiState = MutableStateFlow(AppointmentUiState())
    val uiState: StateFlow<AppointmentUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null
    private var networkMonitorJob: Job? = null
    private var isInitialized: Boolean = false

    init {
        // Start monitoring network connectivity
        startNetworkMonitoring()
    }

    /**
     * Initializes appointment data with automatic authentication check
     * Memory efficient: reuses existing data if already loaded
     * BusinessId handled automatically by AuthUtil
     */
    fun initializeAppointments() {
        
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
        
        // Load appointments from local database
        loadAppointments()
        
        // Load customers for selection
        loadCustomers()
    }

    /**
     * Enhanced loadAppointments with better sync coordination
     * Memory efficient: Flow-based data loading with sync conflict resolution
     * BusinessId handled automatically by repository layer
     */
    private fun loadAppointments() {
        // Check authentication before loading appointments
        if (!authUtil.isUserAuthenticated()) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                errorMessage = authUtil.getAuthErrorMessage()
            )
            return
        }
        
        
        viewModelScope.launch {
            try {
                appointmentRepository.getAllAppointments("")
                    .catch { exception ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "Randevular yüklenirken hata oluştu: ${exception.message}"
                        )
                    }
                    .collect { appointments ->
                        // Update UI state with new appointments
                        _uiState.value = _uiState.value.copy(
                            appointments = appointments,
                            isLoading = false,
                            errorMessage = null
                        )
                        
                        // Load statistics after appointments are loaded
                        loadAppointmentStatistics()
                        
                    }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Randevular yüklenirken hata oluştu: ${e.message}"
                )
            }
        }
    }

    /**
     * Loads appointment statistics for dashboard
     * BusinessId handled automatically by repository layer
     */
    private fun loadAppointmentStatistics() {
        viewModelScope.launch {
            try {
                val totalCount = appointmentRepository.getAppointmentCount("")
                
                _uiState.value = _uiState.value.copy(
                    appointmentStatistics = mapOf(
                        AppointmentStatus.SCHEDULED to totalCount,
                        AppointmentStatus.COMPLETED to 0,
                        AppointmentStatus.CANCELLED to 0,
                        AppointmentStatus.NO_SHOW to 0
                    )
                )
                
            } catch (e: Exception) {
            }
        }
    }

    /**
     * Loads customers for appointment creation
     * BusinessId handled automatically by repository layer
     */
    private fun loadCustomers() {
        viewModelScope.launch {
            try {
                customerRepository.getAllCustomers()
                    .catch { exception ->
                    }
                    .collect { customers ->
                        _uiState.value = _uiState.value.copy(customers = customers)
                    }
            } catch (e: Exception) {
            }
        }
    }

    /**
     * Adds a new appointment
     * Business logic: validates appointment data and handles creation
     */
    fun addAppointment(
        customer: Customer,
        serviceName: String,
        servicePrice: Double,
        appointmentDate: String,
        appointmentTime: String,
        notes: String = "",
        serviceId: String? = null
    ) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
                
                val appointment = Appointment.createForCustomer(
                    customer = customer,
                    serviceName = serviceName,
                    servicePrice = servicePrice,
                    appointmentDate = appointmentDate,
                    appointmentTime = appointmentTime,
                    businessId = authUtil.getCurrentBusinessIdSafe(),
                    notes = notes,
                    serviceId = serviceId
                )
                
                if (!appointment.isValid()) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Lütfen tüm gerekli alanları doldurun."
                    )
                    return@launch
                }
                
                val result = appointmentRepository.addAppointment(appointment)
                result.fold(
                    onSuccess = {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            successMessage = "Randevu başarıyla oluşturuldu.",
                            showAddAppointmentSheet = false
                        )
                        clearFormData()
                    },
                    onFailure = { exception ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "Randevu oluşturulurken hata oluştu: ${exception.message}"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Beklenmeyen hata: ${e.message}"
                )
            }
        }
    }

    /**
     * Enhanced status update with optimistic updates
     * Memory efficient: local update + remote sync pattern
     */
    fun updateAppointmentStatus(appointmentId: String, status: AppointmentStatus) {
        viewModelScope.launch {
            try {
                // Show immediate feedback
                _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
                
                val result = appointmentRepository.updateAppointmentStatus(appointmentId, status)
                result.fold(
                    onSuccess = {
                        val statusMessage = when (status) {
                            AppointmentStatus.COMPLETED -> "Randevu tamamlandı olarak işaretlendi"
                            AppointmentStatus.CANCELLED -> "Randevu iptal edildi"
                            AppointmentStatus.NO_SHOW -> "Randevu 'Gelmedi' olarak işaretlendi"
                            AppointmentStatus.SCHEDULED -> "Randevu zamanlandı"
                        }
                        
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            successMessage = statusMessage
                        )
                    },
                    onFailure = { exception ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "Durum güncellenirken hata oluştu: ${exception.message}"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Beklenmeyen hata: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Completes appointment with payment method
     * Creates payment record for finance tracking
     */
    fun completeAppointmentWithPayment(appointmentId: String, paymentMethod: PaymentMethod) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
                
                // Get the appointment first
                val appointment = _uiState.value.appointments.find { it.id == appointmentId }
                if (appointment == null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Randevu bulunamadı"
                    )
                    return@launch
                }
                
                val businessId = authUtil.getCurrentBusinessIdSafe()
                
                // Create payment record
                val payment = Payment.fromAppointment(
                    appointment = appointment,
                    paymentMethod = paymentMethod,
                    businessId = businessId
                ).copy(status = PaymentStatus.COMPLETED)
                
                // Save payment first
                val paymentResult = paymentRepository.addPayment(payment)
                if (paymentResult.isFailure) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Ödeme kaydı oluşturulamadı: ${paymentResult.exceptionOrNull()?.message}"
                    )
                    return@launch
                }
                
                // Then update appointment status
                val statusResult = appointmentRepository.updateAppointmentStatus(appointmentId, AppointmentStatus.COMPLETED)
                statusResult.fold(
                    onSuccess = {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            successMessage = "Randevu tamamlandı ve ${paymentMethod.getDisplayName()} ödemesi kaydedildi"
                        )
                    },
                    onFailure = { exception ->
                        // Payment was saved but appointment status update failed
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "Randevu durumu güncellenirken hata oluştu: ${exception.message}"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Beklenmeyen hata: ${e.message}"
                )
            }
        }
    }

    /**
     * Updates an existing appointment
     * Business logic: validates appointment data and handles update
     */
    fun updateAppointment(appointment: Appointment) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
                
                if (!appointment.isValid()) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Lütfen tüm gerekli alanları doldurun."
                    )
                    return@launch
                }
                
                val result = appointmentRepository.updateAppointment(appointment)
                result.fold(
                    onSuccess = {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            successMessage = "Randevu başarıyla güncellendi"
                        )
                    },
                    onFailure = { exception ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "Randevu güncellenirken hata oluştu: ${exception.message}"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Beklenmeyen hata: ${e.message}"
                )
            }
        }
    }

    /**
     * Deletes an appointment
     */
    fun deleteAppointment(appointmentId: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
                
                val result = appointmentRepository.deleteAppointment(appointmentId)
                result.fold(
                    onSuccess = {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            successMessage = "Randevu silindi."
                        )
                    },
                    onFailure = { exception ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "Randevu silinirken hata oluştu: ${exception.message}"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Beklenmeyen hata: ${e.message}"
                )
            }
        }
    }

    /**
     * Filters appointments by status and search query
     * Fixed: Proper status filtering without showing only SCHEDULED when null
     */
    fun filterAppointments(status: AppointmentStatus?) {
        _uiState.value = _uiState.value.copy(selectedStatus = status)
        // Remove the old manual filtering - let the UI state computed property handle it
    }

    /**
     * Searches appointments by customer name or phone
     * Enhanced: Real-time reactive search with debouncing
     */
    fun searchAppointments(query: String) {
        // Cancel previous search job to implement debouncing
        searchJob?.cancel()
        
        searchJob = viewModelScope.launch {
            // Add small delay for debouncing
            kotlinx.coroutines.delay(300)
            
            _uiState.value = _uiState.value.copy(searchQuery = query.trim())
        }
    }

    /**
     * Shows/hides add appointment sheet
     * UI state management: sheet visibility
     */
    fun setShowAddAppointmentSheet(show: Boolean) {
        _uiState.value = _uiState.value.copy(showAddAppointmentSheet = show)
    }

    /**
     * Selects an appointment for detail view
     * UI state management: appointment selection
     */
    fun selectAppointment(appointment: Appointment?) {
        _uiState.value = _uiState.value.copy(selectedAppointment = appointment)
    }

    /**
     * Clears success and error messages
     */
    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            successMessage = null,
            errorMessage = null
        )
    }

    /**
     * Performs initial sync if needed
     * BusinessId handled automatically by repository layer
     */
    private fun performInitialSyncIfNeeded() {
        viewModelScope.launch {
            try {
                // Only sync if network is available
                if (networkMonitor.isCurrentlyConnected()) {
                    val result = appointmentRepository.syncWithFirestore("")
                    result.fold(
                        onSuccess = {
                        },
                        onFailure = { exception ->
                            exception.printStackTrace()
                        }
                    )
                } else {
                }
            } catch (e: Exception) {
                // Silent failure for background sync
                e.printStackTrace()
            }
        }
    }

    /**
     * Starts network monitoring for sync management
     */
    private fun startNetworkMonitoring() {
        networkMonitorJob = viewModelScope.launch {
            networkMonitor.isConnected.collect { isOnline ->
                _uiState.value = _uiState.value.copy(isOnline = isOnline)
                
                // Auto-sync when coming back online
                if (isOnline && isInitialized) {
                    performInitialSyncIfNeeded()
                }
            }
        }
    }

    /**
     * Enhanced sync with conflict resolution
     * Memory efficient: optimized sync process with duplicate prevention
     * BusinessId handled automatically by repository layer
     */
    fun syncAppointments() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isSyncing = true, errorMessage = null)
                
                // Perform sync with conflict resolution - no businessId parameter needed
                val result = appointmentRepository.syncWithFirestore("")
                result.fold(
                    onSuccess = {
                        _uiState.value = _uiState.value.copy(
                            isSyncing = false,
                            successMessage = "Randevular başarıyla senkronize edildi"
                        )
                        
                        // Reload statistics after sync
                        loadAppointmentStatistics()
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
     * Helper method to filter appointments
     */
    private fun filterAppointments(
        appointments: List<Appointment>,
        status: AppointmentStatus?,
        query: String
    ): List<Appointment> {
        return appointments.filter { appointment ->
            // When status is null (Tümü selected), show only SCHEDULED appointments
            val matchesStatus = if (status == null) {
                appointment.status == AppointmentStatus.SCHEDULED
            } else {
                appointment.status == status
            }
            
            val matchesQuery = query.isBlank() || 
                appointment.customerName.contains(query, ignoreCase = true) ||
                appointment.customerPhone.contains(query, ignoreCase = true) ||
                appointment.serviceName.contains(query, ignoreCase = true)
            
            matchesStatus && matchesQuery
        }
    }

    /**
     * Gets a specific appointment by ID directly from repository
     * Used for editing appointments across different screens
     * @param appointmentId ID of the appointment to retrieve
     * @return Appointment object or null if not found
     */
    suspend fun getAppointmentById(appointmentId: String): Appointment? {
        return try {
            appointmentRepository.getAppointmentById(appointmentId)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Clears form data
     */
    private fun clearFormData() {
        // Form data clearing can be implemented if needed for add appointment form
    }

    override fun onCleared() {
        super.onCleared()
        searchJob?.cancel()
        networkMonitorJob?.cancel()
    }
} 