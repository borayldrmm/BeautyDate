package com.borayildirim.beautydate.domain.usecases.appointment

import com.borayildirim.beautydate.data.models.Appointment
import com.borayildirim.beautydate.data.models.AppointmentStatus
import com.borayildirim.beautydate.data.repository.AppointmentRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for retrieving appointments with various filtering options
 * Follows Single Responsibility Principle and reactive data patterns
 * Memory efficient: Flow-based reactive queries
 */
class GetAppointmentsUseCase @Inject constructor(
    private val appointmentRepository: AppointmentRepository
) {
    
    /**
     * Gets all appointments for a business
     * @param businessId Business ID to filter appointments
     * @return Flow of appointment list
     */
    operator fun invoke(businessId: String): Flow<List<Appointment>> {
        return appointmentRepository.getAllAppointments(businessId)
    }
    
    /**
     * Gets appointments for a specific date
     * @param date Date in dd/MM/yyyy format
     * @param businessId Business ID to filter appointments
     * @return Flow of daily appointments
     */
    fun forDate(date: String, businessId: String): Flow<List<Appointment>> {
        return appointmentRepository.getAppointmentsByDate(date, businessId)
    }
    
    /**
     * Gets appointments for a specific customer
     * @param customerId Customer ID to filter appointments
     * @param businessId Business ID to filter appointments
     * @return Flow of customer appointments
     */
    fun forCustomer(customerId: String, businessId: String): Flow<List<Appointment>> {
        return appointmentRepository.getAppointmentsByCustomer(customerId, businessId)
    }
    
    /**
     * Gets appointments by status
     * @param status Appointment status to filter
     * @param businessId Business ID to filter appointments
     * @return Flow of filtered appointments
     */
    fun byStatus(status: AppointmentStatus, businessId: String): Flow<List<Appointment>> {
        return appointmentRepository.getAppointmentsByStatus(status, businessId)
    }
    
    /**
     * Gets appointments for a date range
     * @param startDate Start date in dd/MM/yyyy format
     * @param endDate End date in dd/MM/yyyy format
     * @param businessId Business ID to filter appointments
     * @return Flow of appointments in date range
     */
    fun forDateRange(startDate: String, endDate: String, businessId: String): Flow<List<Appointment>> {
        return appointmentRepository.getAppointmentsByDateRange(startDate, endDate, businessId)
    }
} 