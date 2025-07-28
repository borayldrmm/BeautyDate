package com.borayildirim.beautydate.viewmodels.state

import com.borayildirim.beautydate.data.models.WorkingHours
import com.borayildirim.beautydate.data.models.DayHours
import com.borayildirim.beautydate.data.models.DayOfWeek

/**
 * UI State for working hours management screens
 * Memory efficient: immutable data class with computed properties
 * Contains all state needed for working hours configuration
 */
data class WorkingHoursUiState(
    // Data
    val workingHours: WorkingHours? = null,
    val originalWorkingHours: WorkingHours? = null, // For change detection
    
    // UI State
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isSyncing: Boolean = false,
    val isOnline: Boolean = true,
    
    // Edit State
    val isEditing: Boolean = false,
    val editingDay: DayOfWeek? = null,
    val editingDayHours: DayHours? = null,
    val showEditDialog: Boolean = false,
    val showApplyToAllDialog: Boolean = false,
    
    // Messages
    val successMessage: String? = null,
    val errorMessage: String? = null,
    
    // Quick Actions
    val lastAppliedHours: DayHours? = null,
    val hasUnsavedChanges: Boolean = false
) {
    /**
     * Checks if working hours exist
     * Memory efficient: computed property
     */
    val hasWorkingHours: Boolean
        get() = workingHours != null
    
    /**
     * Gets working hours for a specific day
     * Memory efficient: direct property access
     */
    fun getDayHours(dayOfWeek: DayOfWeek): DayHours {
        return workingHours?.getDayHours(dayOfWeek) ?: DayHours()
    }
    
    /**
     * Checks if a specific day is being edited
     * Memory efficient: direct comparison
     */
    fun isDayBeingEdited(dayOfWeek: DayOfWeek): Boolean {
        return isEditing && editingDay == dayOfWeek
    }
    
    /**
     * Gets display text for working status
     * Memory efficient: computed string
     */
    fun getWorkingStatusText(): String {
        val workingDays = workingHours?.getWorkingDays()?.size ?: 0
        return when (workingDays) {
            0 -> "Tüm günler kapalı"
            7 -> "Her gün açık"
            in 1..6 -> "$workingDays gün açık"
            else -> "Bilinmiyor"
        }
    }
    
    /**
     * Gets the most common working hours pattern
     * Memory efficient: computed result, no caching
     */
    fun getMostCommonHours(): DayHours? {
        val workingHours = this.workingHours ?: return null
        
        val hoursList = DayOfWeek.values()
            .map { workingHours.getDayHours(it) }
            .filter { it.isWorking }
        
        if (hoursList.isEmpty()) return null
        
        // Find most common start and end times
        val startTimes = hoursList.map { it.startTime }
        val endTimes = hoursList.map { it.endTime }
        
        val mostCommonStart = startTimes.groupBy { it }.maxByOrNull { it.value.size }?.key
        val mostCommonEnd = endTimes.groupBy { it }.maxByOrNull { it.value.size }?.key
        
        return if (mostCommonStart != null && mostCommonEnd != null) {
            DayHours(isWorking = true, startTime = mostCommonStart, endTime = mostCommonEnd)
        } else null
    }
    
    /**
     * Checks if there are validation errors
     * Memory efficient: computed property
     */
    val hasValidationErrors: Boolean
        get() = workingHours?.isValid() == false
    
    /**
     * Gets list of days with validation errors
     * Memory efficient: computed list
     */
    fun getDaysWithErrors(): List<DayOfWeek> {
        val workingHours = this.workingHours ?: return emptyList()
        return DayOfWeek.values().filter { day ->
            !workingHours.getDayHours(day).isValidTimeRange()
        }
    }
} 