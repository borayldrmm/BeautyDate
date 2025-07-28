package com.borayildirim.beautydate.data.repository

import com.borayildirim.beautydate.data.models.WorkingHours
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for working hours management
 * Follows Repository pattern with clean architecture principles
 * Multi-tenant architecture: All operations use authenticated businessId via AuthUtil
 * Memory efficient: Flow-based reactive data and Result-based error handling
 */
interface WorkingHoursRepository {
    
    /**
     * Gets working hours for current authenticated business as Flow (reactive)
     * BusinessId filtering applied automatically via AuthUtil
     * Memory efficient: single source of truth with reactive updates
     */
    fun getWorkingHoursFlow(): Flow<WorkingHours?>
    
    /**
     * Gets working hours for current authenticated business (one-time fetch)
     * BusinessId filtering applied automatically via AuthUtil
     * Memory efficient: single query result
     */
    suspend fun getWorkingHours(): Result<WorkingHours?>
    
    /**
     * Saves or updates working hours for current authenticated business
     * BusinessId assigned automatically via AuthUtil
     * Memory efficient: single operation with optimistic updates
     */
    suspend fun saveWorkingHours(workingHours: WorkingHours): Result<WorkingHours>
    
    /**
     * Creates default working hours for current authenticated business
     * BusinessId assigned automatically via AuthUtil
     * Memory efficient: pre-configured default with single save operation
     */
    suspend fun createDefaultWorkingHours(): Result<WorkingHours>
    
    /**
     * Updates working hours for a specific day in current authenticated business
     * BusinessId applied automatically via AuthUtil
     * Memory efficient: partial update, single day modification
     */
    suspend fun updateDayHours(
        dayOfWeek: com.borayildirim.beautydate.data.models.DayOfWeek,
        dayHours: com.borayildirim.beautydate.data.models.DayHours
    ): Result<WorkingHours>
    
    /**
     * Applies same working hours to all days for current authenticated business
     * BusinessId applied automatically via AuthUtil
     * Memory efficient: bulk update with single operation
     */
    suspend fun applyToAllDays(
        dayHours: com.borayildirim.beautydate.data.models.DayHours
    ): Result<WorkingHours>
    
    /**
     * Resets working hours to default for current authenticated business
     * BusinessId applied automatically via AuthUtil
     * Memory efficient: single operation with predefined values
     */
    suspend fun resetToDefault(): Result<WorkingHours>
    
    /**
     * Checks if business is open at specific time for current authenticated business
     * BusinessId applied automatically via AuthUtil
     * Memory efficient: cached calculation with minimal object creation
     */
    suspend fun isBusinessOpen(
        dayOfWeek: com.borayildirim.beautydate.data.models.DayOfWeek,
        time: String
    ): Boolean
    
    /**
     * Gets next open time for current authenticated business
     * BusinessId applied automatically via AuthUtil
     * Memory efficient: calculated result without unnecessary iterations
     */
    suspend fun getNextOpenTime(): com.borayildirim.beautydate.data.models.DayHours?
    
    /**
     * Syncs working hours with Firestore for current authenticated business
     * BusinessId applied automatically via AuthUtil
     */
    suspend fun syncWithFirestore(): Result<Unit>
    
    /**
     * Performs initial sync for current authenticated business
     * BusinessId applied automatically via AuthUtil
     */
    suspend fun performInitialSync(): Result<Unit>
    
    /**
     * Gets all working hours as Flow (for admin/multi-business scenarios)
     * Memory efficient: reactive list with entity mapping
     */
    fun getAllWorkingHoursFlow(): Flow<List<WorkingHours>>
} 