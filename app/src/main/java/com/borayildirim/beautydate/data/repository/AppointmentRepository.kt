package com.borayildirim.beautydate.data.repository

import com.borayildirim.beautydate.data.models.Appointment
import com.borayildirim.beautydate.data.models.AppointmentStatus
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for appointment data operations
 * Defines contract for appointment management following Repository pattern
 * Supports offline-first approach with local and remote sync
 */
interface AppointmentRepository {
    
    /**
     * Gets all appointments for a business
     * @param businessId Business ID to filter appointments
     * @return Flow of appointment list with reactive updates
     */
    fun getAllAppointments(businessId: String): Flow<List<Appointment>>
    
    /**
     * Gets appointments for a specific customer
     * @param customerId Customer ID to filter appointments
     * @param businessId Business ID to filter appointments
     * @return Flow of customer appointments
     */
    fun getAppointmentsByCustomer(customerId: String, businessId: String): Flow<List<Appointment>>
    
    /**
     * Gets appointments by status
     * @param status Appointment status to filter
     * @param businessId Business ID to filter appointments
     * @return Flow of filtered appointments
     */
    fun getAppointmentsByStatus(status: AppointmentStatus, businessId: String): Flow<List<Appointment>>
    
    /**
     * Gets appointments for a specific date
     * @param date Date in dd/MM/yyyy format
     * @param businessId Business ID to filter appointments
     * @return Flow of daily appointments
     */
    fun getAppointmentsByDate(date: String, businessId: String): Flow<List<Appointment>>
    
    /**
     * Gets appointments for a date range
     * @param startDate Start date in dd/MM/yyyy format
     * @param endDate End date in dd/MM/yyyy format
     * @param businessId Business ID to filter appointments
     * @return Flow of appointments in date range
     */
    fun getAppointmentsByDateRange(startDate: String, endDate: String, businessId: String): Flow<List<Appointment>>
    
    /**
     * Gets a single appointment by ID
     * @param appointmentId Appointment ID
     * @return Appointment or null if not found
     */
    suspend fun getAppointmentById(appointmentId: String): Appointment?
    
    /**
     * Gets appointments for a specific date (synchronous)
     * @param date Date in dd/MM/yyyy format
     * @param businessId Business ID to filter appointments
     * @return List of appointments for the date
     */
    suspend fun getAppointmentsByDateSync(date: String, businessId: String): List<Appointment>
    
    /**
     * Search appointments by customer name or phone
     * @param query Search query
     * @param businessId Business ID to filter appointments
     * @return Flow of matching appointments
     */
    fun searchAppointments(query: String, businessId: String): Flow<List<Appointment>>
    
    /**
     * Adds a new appointment
     * @param appointment Appointment to add
     * @return Result indicating success or failure
     */
    suspend fun addAppointment(appointment: Appointment): Result<Unit>
    
    /**
     * Updates an existing appointment
     * @param appointment Updated appointment data
     * @return Result indicating success or failure
     */
    suspend fun updateAppointment(appointment: Appointment): Result<Unit>
    
    /**
     * Updates appointment status
     * @param appointmentId Appointment ID
     * @param status New appointment status
     * @return Result indicating success or failure
     */
    suspend fun updateAppointmentStatus(appointmentId: String, status: AppointmentStatus): Result<Unit>
    
    /**
     * Deletes an appointment (soft delete)
     * @param appointmentId Appointment ID to delete
     * @return Result indicating success or failure
     */
    suspend fun deleteAppointment(appointmentId: String): Result<Unit>
    
    /**
     * Syncs appointments with remote server
     * @param businessId Business ID to sync appointments for
     * @return Result indicating sync success or failure
     */
    suspend fun syncWithFirestore(businessId: String): Result<Unit>
    
    /**
     * Gets total appointment count for a business
     * @param businessId Business ID to get count for
     * @return Total appointment count
     */
    suspend fun getAppointmentCount(businessId: String): Int
    
    /**
     * Gets appointment statistics
     * @param businessId Business ID to get statistics for
     * @return Map of status to count
     */
    suspend fun getAppointmentStatistics(businessId: String): Map<AppointmentStatus, Int>
} 