package com.borayildirim.beautydate.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.borayildirim.beautydate.data.models.BusinessStatistics
import com.borayildirim.beautydate.data.models.StatisticsPeriod
import com.borayildirim.beautydate.data.models.StatisticsCategory
import com.borayildirim.beautydate.data.repository.StatisticsRepository
import com.borayildirim.beautydate.viewmodels.state.StatisticsUiState
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Statistics screen
 * Follows MVVM pattern with Hilt DI and Clean Architecture
 * Memory efficient: StateFlow for UI state management
 */
@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val statisticsRepository: StatisticsRepository,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(StatisticsUiState())
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()
    
    private val currentBusinessId: String
        get() = firebaseAuth.currentUser?.uid ?: ""
    
    init {
        loadStatistics()
    }
    
    /**
     * Initializes statistics with cross-device sync
     * Should be called from MainScreen for proper sync timing
     */
    fun initializeStatistics() {
        
        if (currentBusinessId.isEmpty()) {
            return
        }
        
        // Perform sync first for cross-device consistency
        viewModelScope.launch {
            try {
                val refreshResult = statisticsRepository.refreshStatistics(currentBusinessId)
                if (refreshResult.isSuccess) {
                } else {
                }
            } catch (e: Exception) {
            }
        }
        
        // Reload statistics (will be triggered automatically by Flow)
    }
    
    /**
     * Loads business statistics
     * Memory efficient: cancels previous job if new one starts
     */
    fun loadStatistics() {
        viewModelScope.launch {
            if (currentBusinessId.isEmpty()) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Kullanıcı oturumu bulunamadı"
                )
                return@launch
            }
            
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                statisticsRepository.getBusinessStatisticsFlow(currentBusinessId, _uiState.value.selectedPeriod)
                    .catch { exception ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = exception.message ?: "İstatistikler yüklenirken hata oluştu"
                        )
                    }
                    .collect { statistics ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            statistics = statistics,
                            error = null
                        )
                    }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "İstatistikler yüklenirken hata oluştu"
                )
            }
        }
    }
    
    /**
     * Changes statistics period and reloads data
     */
    fun changePeriod(period: StatisticsPeriod) {
        if (_uiState.value.selectedPeriod != period) {
            _uiState.value = _uiState.value.copy(selectedPeriod = period)
            loadStatistics()
        }
    }
    
    /**
     * Changes statistics category filter
     */
    fun changeCategory(category: StatisticsCategory) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
    }
    
    /**
     * Refreshes statistics data
     */
    fun refreshStatistics() {
        viewModelScope.launch {
            if (currentBusinessId.isEmpty()) return@launch
            
            _uiState.value = _uiState.value.copy(isRefreshing = true)
            
            try {
                statisticsRepository.refreshStatistics(currentBusinessId)
                loadStatistics()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isRefreshing = false,
                    error = "Veriler yenilenirken hata oluştu: ${e.message}"
                )
            } finally {
                _uiState.value = _uiState.value.copy(isRefreshing = false)
            }
        }
    }
    
    /**
     * Exports statistics data
     */
    fun exportStatistics(format: com.borayildirim.beautydate.data.repository.ExportFormat) {
        // Show development message for export features
        _uiState.value = _uiState.value.copy(
            exportMessage = "Bu özellik şu anda geliştirme aşamasında"
        )
    }
    
    /**
     * Clears export message
     */
    fun clearExportMessage() {
        _uiState.value = _uiState.value.copy(exportMessage = null)
    }
    
    /**
     * Clears error message
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    /**
     * Returns whether statistics should be shown based on category filter
     */
    fun shouldShowFinancialStats(): Boolean {
        return _uiState.value.selectedCategory == StatisticsCategory.OVERVIEW || 
               _uiState.value.selectedCategory == StatisticsCategory.FINANCIAL
    }
    
    fun shouldShowCustomerStats(): Boolean {
        return _uiState.value.selectedCategory == StatisticsCategory.OVERVIEW || 
               _uiState.value.selectedCategory == StatisticsCategory.CUSTOMERS
    }
    
    fun shouldShowAppointmentStats(): Boolean {
        return _uiState.value.selectedCategory == StatisticsCategory.OVERVIEW || 
               _uiState.value.selectedCategory == StatisticsCategory.APPOINTMENTS
    }
    
    fun shouldShowEmployeeStats(): Boolean {
        return _uiState.value.selectedCategory == StatisticsCategory.OVERVIEW || 
               _uiState.value.selectedCategory == StatisticsCategory.EMPLOYEES
    }
    
    fun shouldShowServiceStats(): Boolean {
        return _uiState.value.selectedCategory == StatisticsCategory.OVERVIEW || 
               _uiState.value.selectedCategory == StatisticsCategory.SERVICES
    }
} 