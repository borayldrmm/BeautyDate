package com.borayildirim.beautydate.data.local.dao

import androidx.room.*
import com.borayildirim.beautydate.data.local.entities.WorkingHoursEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for working hours operations
 * Memory efficient: Flow-based reactive queries and indexed operations
 * Supports offline-first architecture with local data persistence
 */
@Dao
interface WorkingHoursDao {
    
    /**
     * Gets working hours for a specific business as Flow
     * Memory efficient: reactive updates, single query result
     */
    @Query("SELECT * FROM working_hours WHERE businessId = :businessId LIMIT 1")
    fun getWorkingHoursFlow(businessId: String): Flow<WorkingHoursEntity?>
    
    /**
     * Gets working hours for a specific business (suspend function)
     * Memory efficient: single query result
     */
    @Query("SELECT * FROM working_hours WHERE businessId = :businessId LIMIT 1")
    suspend fun getWorkingHours(businessId: String): WorkingHoursEntity?
    
    /**
     * Gets working hours by ID
     * Memory efficient: primary key lookup
     */
    @Query("SELECT * FROM working_hours WHERE id = :id LIMIT 1")
    suspend fun getWorkingHoursById(id: String): WorkingHoursEntity?
    
    /**
     * Inserts or updates working hours
     * Memory efficient: single operation with conflict resolution
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateWorkingHours(workingHours: WorkingHoursEntity)
    
    /**
     * Updates working hours
     * Memory efficient: direct entity update
     */
    @Update
    suspend fun updateWorkingHours(workingHours: WorkingHoursEntity)
    
    /**
     * Deletes working hours for a specific business
     * Memory efficient: indexed deletion by businessId
     */
    @Query("DELETE FROM working_hours WHERE businessId = :businessId")
    suspend fun deleteWorkingHoursByBusinessId(businessId: String)
    
    /**
     * Deletes working hours by ID
     * Memory efficient: primary key deletion
     */
    @Query("DELETE FROM working_hours WHERE id = :id")
    suspend fun deleteWorkingHoursById(id: String)
    
    /**
     * Checks if working hours exist for a business
     * Memory efficient: count query, returns boolean result
     */
    @Query("SELECT COUNT(*) > 0 FROM working_hours WHERE businessId = :businessId")
    suspend fun hasWorkingHours(businessId: String): Boolean
    
    /**
     * Gets the last update timestamp for working hours
     * Memory efficient: single field query with index usage
     */
    @Query("SELECT updatedAt FROM working_hours WHERE businessId = :businessId LIMIT 1")
    suspend fun getLastUpdatedTime(businessId: String): String?
    
    /**
     * Gets all working hours (for admin/debug purposes)
     * Memory efficient: Flow-based reactive list
     */
    @Query("SELECT * FROM working_hours ORDER BY updatedAt DESC")
    fun getAllWorkingHoursFlow(): Flow<List<WorkingHoursEntity>>
    
    /**
     * Deletes all working hours (for testing/reset purposes)
     * Memory efficient: bulk delete operation
     */
    @Query("DELETE FROM working_hours")
    suspend fun deleteAllWorkingHours()
    
    /**
     * Updates only the timestamp of working hours
     * Memory efficient: partial update, single field modification
     */
    @Query("UPDATE working_hours SET updatedAt = :updatedAt WHERE businessId = :businessId")
    suspend fun updateTimestamp(businessId: String, updatedAt: String)
    
    /**
     * Gets working hours updated after a specific timestamp
     * Memory efficient: indexed query with timestamp comparison
     */
    @Query("SELECT * FROM working_hours WHERE updatedAt > :timestamp ORDER BY updatedAt DESC")
    suspend fun getWorkingHoursUpdatedAfter(timestamp: String): List<WorkingHoursEntity>
} 