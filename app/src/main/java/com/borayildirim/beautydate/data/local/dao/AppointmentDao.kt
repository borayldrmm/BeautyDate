package com.borayildirim.beautydate.data.local.dao

import androidx.room.*
import com.borayildirim.beautydate.data.local.AppointmentEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO interface for appointment database operations
 * Provides efficient database access with Flow-based reactive queries
 * Memory efficient: indexed queries and targeted operations
 */
@Dao
interface AppointmentDao {
    
    /**
     * Gets all appointments for a business with reactive updates
     * Memory efficient: Flow-based reactive query with indexing
     */
    @Query("SELECT * FROM appointments WHERE businessId = :businessId AND isDeleted = 0 ORDER BY appointmentDate DESC, appointmentTime DESC")
    fun getAllAppointments(businessId: String): Flow<List<AppointmentEntity>>
    
    /**
     * Gets appointments for a specific customer
     * Business logic: customer appointment history
     */
    @Query("SELECT * FROM appointments WHERE customerId = :customerId AND businessId = :businessId AND isDeleted = 0 ORDER BY appointmentDate DESC, appointmentTime DESC")
    fun getAppointmentsByCustomer(customerId: String, businessId: String): Flow<List<AppointmentEntity>>
    
    /**
     * Gets appointments by status
     * Business logic: status-based filtering
     */
    @Query("SELECT * FROM appointments WHERE status = :status AND businessId = :businessId AND isDeleted = 0 ORDER BY appointmentDate ASC, appointmentTime ASC")
    fun getAppointmentsByStatus(status: String, businessId: String): Flow<List<AppointmentEntity>>
    
    /**
     * Gets appointments for a specific date
     * Business logic: daily appointment view
     */
    @Query("SELECT * FROM appointments WHERE appointmentDate = :date AND businessId = :businessId AND isDeleted = 0 ORDER BY appointmentTime ASC")
    fun getAppointmentsByDate(date: String, businessId: String): Flow<List<AppointmentEntity>>
    
    /**
     * Gets appointments for a date range
     * Business logic: weekly/monthly appointment view
     */
    @Query("SELECT * FROM appointments WHERE appointmentDate BETWEEN :startDate AND :endDate AND businessId = :businessId AND isDeleted = 0 ORDER BY appointmentDate ASC, appointmentTime ASC")
    fun getAppointmentsByDateRange(startDate: String, endDate: String, businessId: String): Flow<List<AppointmentEntity>>
    
    /**
     * Gets a single appointment by ID
     * Memory efficient: single object query
     */
    @Query("SELECT * FROM appointments WHERE id = :appointmentId AND isDeleted = 0")
    suspend fun getAppointmentById(appointmentId: String): AppointmentEntity?
    
    /**
     * Inserts a new appointment
     * Memory efficient: single insert operation
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppointment(appointment: AppointmentEntity)
    
    /**
     * Inserts multiple appointments
     * Bulk operation: efficient for sync operations
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppointments(appointments: List<AppointmentEntity>)
    
    /**
     * Updates an existing appointment
     * Memory efficient: targeted update
     */
    @Update
    suspend fun updateAppointment(appointment: AppointmentEntity)
    
    /**
     * Updates appointment status
     * Business logic: status change operations
     */
    @Query("UPDATE appointments SET status = :status, updatedAt = :updatedAt, needsSync = 1 WHERE id = :appointmentId")
    suspend fun updateAppointmentStatus(appointmentId: String, status: String, updatedAt: Long = System.currentTimeMillis())

    /**
     * Deletes appointment (soft delete)
     * Business logic: soft delete for data integrity
     */
    @Query("UPDATE appointments SET isDeleted = 1, updatedAt = :timestamp WHERE id = :appointmentId")
    suspend fun deleteAppointment(appointmentId: String, timestamp: Long = System.currentTimeMillis())

    /**
     * Gets appointment count for business
     * Business logic: statistics
     */
    @Query("SELECT COUNT(*) FROM appointments WHERE businessId = :businessId AND isDeleted = 0")
    suspend fun getAppointmentCount(businessId: String): Int

    /**
     * Gets appointment count by status
     * Business logic: status-based statistics
     */
    @Query("SELECT COUNT(*) FROM appointments WHERE status = :status AND businessId = :businessId AND isDeleted = 0")
    suspend fun getAppointmentCountByStatus(status: String, businessId: String): Int

    /**
     * Searches appointments by customer name or phone
     * Business logic: appointment search functionality
     */
    @Query("""
        SELECT * FROM appointments 
        WHERE businessId = :businessId 
        AND isDeleted = 0 
        AND (customerName LIKE '%' || :query || '%' 
             OR customerPhone LIKE '%' || :query || '%'
             OR serviceName LIKE '%' || :query || '%')
        ORDER BY appointmentDate DESC, appointmentTime DESC
    """)
    fun searchAppointments(businessId: String, query: String): Flow<List<AppointmentEntity>>

    /**
     * Marks appointment as synced with Firestore
     * Sync management: removes sync flag
     */
    @Query("UPDATE appointments SET needsSync = 0 WHERE id = :appointmentId")
    suspend fun markAsSynced(appointmentId: String)

    /**
     * Gets appointments that need to be synced with Firestore
     * Sync management: offline-first sync strategy
     */
    @Query("SELECT * FROM appointments WHERE businessId = :businessId AND needsSync = 1")
    suspend fun getAppointmentsNeedingSync(businessId: String): List<AppointmentEntity>

    /**
     * Marks appointment as deleted (soft delete)
     * Business logic: soft delete for data integrity
     */
    @Query("UPDATE appointments SET isDeleted = 1, updatedAt = :timestamp WHERE id = :appointmentId")
    suspend fun markAsDeleted(appointmentId: String, timestamp: Long = System.currentTimeMillis())

    /**
     * Hard deletes appointment from database
     * Admin operation: permanent removal
     */
    @Query("DELETE FROM appointments WHERE id = :appointmentId")
    suspend fun hardDeleteAppointment(appointmentId: String)
    
    /**
     * Deletes all appointments for a business (hard delete)
     * Admin operation: complete data cleanup
     */
    @Query("DELETE FROM appointments WHERE businessId = :businessId")
    suspend fun deleteAllAppointments(businessId: String)
} 