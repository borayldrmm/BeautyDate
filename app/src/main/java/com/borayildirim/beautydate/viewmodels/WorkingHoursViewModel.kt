package com.borayildirim.beautydate.viewmodels

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.borayildirim.beautydate.R
import com.borayildirim.beautydate.data.models.DayHours
import com.borayildirim.beautydate.data.models.DayOfWeek
import com.borayildirim.beautydate.data.models.WorkingHours
import com.borayildirim.beautydate.data.repository.WorkingHoursRepository
import com.borayildirim.beautydate.utils.NetworkMonitor
import com.borayildirim.beautydate.viewmodels.state.WorkingHoursUiState
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for working hours management
 * Handles all working hours operations following MVVM pattern
 * Memory efficient: Flow-based reactive data and minimal object creation
 */
@HiltViewModel
class WorkingHoursViewModel @Inject constructor(
    private val workingHoursRepository: WorkingHoursRepository,
    private val networkMonitor: NetworkMonitor,
    private val firebaseAuth: FirebaseAuth,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(WorkingHoursUiState())
    val uiState: StateFlow<WorkingHoursUiState> = _uiState.asStateFlow()

    private var networkMonitorJob: Job? = null
    private var currentBusinessId: String = ""

    init {
        initializeWorkingHours()
    }
    
    private fun initializeWorkingHours() {
        viewModelScope.launch {
            try {
                // Check if working hours exist, create default if not
                val result = workingHoursRepository.createDefaultWorkingHours()
                
                if (result.isFailure) {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Çalışma saatleri oluşturulamadı: ${result.exceptionOrNull()?.message}"
                    )
                }
                
                // Start observing working hours changes
                workingHoursRepository.getWorkingHoursFlow()
                    .collect { workingHours ->
                        _uiState.value = _uiState.value.copy(
                            workingHours = workingHours,
                            isLoading = false,
                            errorMessage = null
                        )
                    }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Çalışma saatleri yüklenirken hata oluştu"
                )
            }
        }
    }

    /**
     * Updates working hours for a specific day
     * Memory efficient: partial update with single day change
     */
    fun updateDayHours(dayOfWeek: DayOfWeek, dayHours: DayHours) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                
                val result = workingHoursRepository.updateDayHours(dayOfWeek, dayHours)
                
                if (result.isSuccess) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Çalışma saatleri güncellendi"
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.exceptionOrNull()?.message ?: "Güncelleme başarısız"
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
     * Toggles the working status of a specific day
     * Memory efficient: single day toggle with immediate save and state feedback
     */
    fun toggleDay(dayOfWeek: DayOfWeek) {
        val currentWorkingHours = _uiState.value.workingHours ?: return
        val currentDayHours = currentWorkingHours.getDayHours(dayOfWeek)
        val toggledDayHours = currentDayHours.copy(isWorking = !currentDayHours.isWorking)
        
        val updatedWorkingHours = currentWorkingHours.updateDayHours(dayOfWeek, toggledDayHours)
        _uiState.value = _uiState.value.copy(workingHours = updatedWorkingHours)
        
        // Set success message for UI feedback
        val dayName = getDayName(dayOfWeek)
        val successMessage = if (toggledDayHours.isWorking) {
            context.getString(R.string.toast_day_opened, dayName)
        } else {
            context.getString(R.string.toast_day_closed, dayName)
        }
        _uiState.value = _uiState.value.copy(successMessage = successMessage)
        clearMessageAfterDelay()
        
        // Save the changes
        saveWorkingHours(updatedWorkingHours, showToast = false)
    }

    /**
     * Apply specified working hours to all days except Sunday
     * Sunday remains closed as per business rule
     */
    fun applyToAllDays(startTime: String, endTime: String) {
        val currentWorkingHours = _uiState.value.workingHours ?: return
        val newDayHours = DayHours(isWorking = true, startTime = startTime, endTime = endTime)
        val sundayClosedHours = DayHours(isWorking = false, startTime = "09:00", endTime = "19:00")
        
        val updatedWorkingHours = currentWorkingHours.copy(
            monday = newDayHours,
            tuesday = newDayHours,
            wednesday = newDayHours,
            thursday = newDayHours,
            friday = newDayHours,
            saturday = newDayHours,
            sunday = sundayClosedHours // Sunday always stays closed
        )
        _uiState.value = _uiState.value.copy(
            workingHours = updatedWorkingHours,
            successMessage = context.getString(R.string.toast_apply_all_success)
        )
        clearMessageAfterDelay()
        
        // Save the changes
        saveWorkingHours(updatedWorkingHours, showToast = false)
    }
    
    /**
     * Apply specified working hours to weekdays only (Monday to Friday)
     */
    fun applyToWeekdays(startTime: String, endTime: String) {
        val currentWorkingHours = _uiState.value.workingHours ?: return
        val newDayHours = DayHours(isWorking = true, startTime = startTime, endTime = endTime)
        
        val updatedWorkingHours = currentWorkingHours.copy(
            monday = newDayHours,
            tuesday = newDayHours,
            wednesday = newDayHours,
            thursday = newDayHours,
            friday = newDayHours
        )
        _uiState.value = _uiState.value.copy(
            workingHours = updatedWorkingHours,
            successMessage = context.getString(R.string.toast_apply_weekdays_success)
        )
        clearMessageAfterDelay()
        
        // Save the changes
        saveWorkingHours(updatedWorkingHours, showToast = false)
    }

    /**
     * Syncs working hours with Firebase
     * Memory efficient: one-time sync operation
     */
    fun syncWorkingHours() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                
                val result = workingHoursRepository.syncWithFirestore()
                
                _uiState.value = if (result.isSuccess) {
                    _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Çalışma saatleri senkronize edildi"
                    )
                } else {
                    _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.exceptionOrNull()?.message ?: "Senkronizasyon başarısız"
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
     * Shows edit dialog for a specific day
     * Memory efficient: minimal state update
     */
    fun showEditDialog(dayOfWeek: DayOfWeek) {
        val currentDayHours = _uiState.value.getDayHours(dayOfWeek)
        _uiState.value = _uiState.value.copy(
            showEditDialog = true,
            editingDay = dayOfWeek,
            editingDayHours = currentDayHours,
            isEditing = true
        )
    }

    /**
     * Hides edit dialog
     * Memory efficient: minimal state reset
     */
    fun hideEditDialog() {
        _uiState.value = _uiState.value.copy(
            showEditDialog = false,
            editingDay = null,
            editingDayHours = null,
            isEditing = false
        )
    }

    /**
     * Shows apply to all dialog
     * Memory efficient: single boolean update
     */
    fun showApplyToAllDialog() {
        _uiState.value = _uiState.value.copy(showApplyToAllDialog = true)
    }

    /**
     * Hides apply to all dialog
     * Memory efficient: single boolean update
     */
    fun hideApplyToAllDialog() {
        _uiState.value = _uiState.value.copy(showApplyToAllDialog = false)
    }

    /**
     * Updates editing day hours temporarily
     * Memory efficient: temporary state for preview
     */
    fun updateEditingDayHours(dayHours: DayHours) {
        _uiState.value = _uiState.value.copy(editingDayHours = dayHours)
    }

    /**
     * Clears success/error messages
     * Memory efficient: selective state clearing
     */
    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            successMessage = null,
            errorMessage = null
        )
    }

    /**
     * Reset working hours to default values
     * Default: Monday-Friday 9:00-19:00, Saturday 10:00-17:00, Sunday closed
     */
    fun resetToDefaults() {
        val defaultWorkingHours = WorkingHours.createDefault(currentBusinessId)
        _uiState.value = _uiState.value.copy(
            workingHours = defaultWorkingHours,
            successMessage = context.getString(R.string.toast_reset_success)
        )
        clearMessageAfterDelay()
        
        saveWorkingHours(defaultWorkingHours, showToast = false)
    }

    /**
     * Private helper method to save working hours to repository
     * Memory efficient: single repository call with error handling
     */
    private fun saveWorkingHours(workingHours: WorkingHours, showToast: Boolean = true) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isSaving = true, errorMessage = null)
                val result = workingHoursRepository.saveWorkingHours(workingHours)
                result.fold(
                    onSuccess = {
                        _uiState.value = _uiState.value.copy(
                            isSaving = false,
                            successMessage = "Çalışma saatleri kaydedildi"
                        )
                        clearMessageAfterDelay()
                    },
                    onFailure = { exception ->
                        _uiState.value = _uiState.value.copy(
                            isSaving = false,
                            errorMessage = "Kaydetme hatası: ${exception.message}"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    errorMessage = "Beklenmeyen hata: ${e.message}"
                )
            }
        }
    }

    /**
     * Starts network monitoring for sync management
     * Memory efficient: single Flow subscription
     */
    private fun startNetworkMonitoring() {
        networkMonitorJob = viewModelScope.launch {
            networkMonitor.isConnected.collect { isOnline ->
                _uiState.value = _uiState.value.copy(isOnline = isOnline)
            }
        }
    }

    /**
     * Clears messages after delay
     * Memory efficient: single coroutine with delay
     */
    private fun clearMessageAfterDelay() {
        viewModelScope.launch {
            delay(3000) // 3 seconds
            _uiState.value = _uiState.value.copy(
                successMessage = null,
                errorMessage = null
            )
        }
    }

    /**
     * Shows toast message
     * Memory efficient: single toast instance
     */
    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    /**
     * Gets localized day name for display
     * Memory efficient: direct string resource access
     */
    private fun getDayName(dayOfWeek: DayOfWeek): String {
        return when (dayOfWeek) {
            DayOfWeek.MONDAY -> context.getString(R.string.working_hours_monday)
            DayOfWeek.TUESDAY -> context.getString(R.string.working_hours_tuesday)
            DayOfWeek.WEDNESDAY -> context.getString(R.string.working_hours_wednesday)
            DayOfWeek.THURSDAY -> context.getString(R.string.working_hours_thursday)
            DayOfWeek.FRIDAY -> context.getString(R.string.working_hours_friday)
            DayOfWeek.SATURDAY -> context.getString(R.string.working_hours_saturday)
            DayOfWeek.SUNDAY -> context.getString(R.string.working_hours_sunday)
        }
    }

    /**
     * Save edited day hours with toast feedback
     * Memory efficient: targeted day update with user feedback
     */
    fun saveEditedDayHours() {
        val editingDay = _uiState.value.editingDay ?: return
        val editingDayHours = _uiState.value.editingDayHours ?: return
        val currentWorkingHours = _uiState.value.workingHours ?: return
        
        val updatedWorkingHours = currentWorkingHours.updateDayHours(editingDay, editingDayHours)
        _uiState.value = _uiState.value.copy(workingHours = updatedWorkingHours)
        
        // Show toast feedback
        val dayName = getDayName(editingDay)
        _uiState.value = _uiState.value.copy(successMessage = context.getString(R.string.toast_hours_updated, dayName))
        
        // Save the changes
        saveWorkingHours(updatedWorkingHours, showToast = false)
        
        // Hide dialog
        hideEditDialog()
    }

    /**
     * Updates specific day hours (for direct screen updates)
     * Memory efficient: direct day update with state-based feedback
     */
    fun updateDayHoursWithToast(dayOfWeek: DayOfWeek, dayHours: DayHours) {
        val currentWorkingHours = _uiState.value.workingHours ?: return
        val updatedWorkingHours = currentWorkingHours.updateDayHours(dayOfWeek, dayHours)
        _uiState.value = _uiState.value.copy(workingHours = updatedWorkingHours)
        
        // Save the changes - this will trigger success/error messages through UI state
        // Don't show toast here to avoid duplicate messages
        saveWorkingHours(updatedWorkingHours, showToast = false)
    }

    /**
     * Gets weekend status for dynamic button text
     * Memory efficient: computed status based on current working hours
     */
    fun getWeekendStatus(): WeekendStatus {
        val workingHours = _uiState.value.workingHours ?: return WeekendStatus.CLOSED
        val saturdayOpen = workingHours.saturday.isWorking
        val sundayOpen = workingHours.sunday.isWorking
        
        return when {
            !saturdayOpen && !sundayOpen -> WeekendStatus.CLOSED
            saturdayOpen && !sundayOpen -> WeekendStatus.SATURDAY_ONLY
            else -> WeekendStatus.OPEN
        }
    }

    enum class WeekendStatus {
        CLOSED,          // Both Saturday and Sunday closed
        SATURDAY_ONLY,   // Only Saturday open (Sunday closed)
        OPEN            // Both days open (shouldn't happen as Sunday should always be closed)
    }

    override fun onCleared() {
        super.onCleared()
        networkMonitorJob?.cancel()
    }
} 