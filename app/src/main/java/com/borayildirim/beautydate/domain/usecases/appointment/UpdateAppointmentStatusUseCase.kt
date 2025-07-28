package com.borayildirim.beautydate.domain.usecases.appointment

import com.borayildirim.beautydate.data.models.AppointmentStatus
import com.borayildirim.beautydate.data.repository.AppointmentRepository
import javax.inject.Inject

/**
 * Use case for updating appointment status
 * Handles status transitions for appointment lifecycle management
 * Memory efficient: targeted status updates with validation
 */
class UpdateAppointmentStatusUseCase @Inject constructor(
    private val appointmentRepository: AppointmentRepository
) {
    
    /**
     * Updates appointment status with validation
     * @param appointmentId Appointment ID to update
     * @param newStatus New status to set
     * @param businessId Business ID for validation
     * @return Result indicating success or failure
     */
    suspend operator fun invoke(
        appointmentId: String,
        newStatus: AppointmentStatus,
        businessId: String
    ): Result<Unit> {
        
        // Validate inputs
        if (appointmentId.isBlank()) {
            return Result.failure(IllegalArgumentException("Randevu ID gereklidir"))
        }
        
        if (businessId.isBlank()) {
            return Result.failure(IllegalArgumentException("İşletme ID gereklidir"))
        }
        
        // Get current appointment to validate ownership and current status
        val currentAppointment = appointmentRepository.getAppointmentById(appointmentId)
        if (currentAppointment == null) {
            return Result.failure(IllegalArgumentException("Randevu bulunamadı"))
        }
        
        if (currentAppointment.businessId != businessId) {
            return Result.failure(IllegalArgumentException("Bu randevu size ait değil"))
        }
        
        // Validate status transition
        val transitionResult = validateStatusTransition(currentAppointment.status, newStatus)
        if (!transitionResult.isSuccess) {
            return transitionResult
        }
        
        // Update status in repository
        return appointmentRepository.updateAppointmentStatus(appointmentId, newStatus)
    }
    
    /**
     * Validates status transition rules
     * Memory efficient: business logic validation
     */
    private fun validateStatusTransition(
        currentStatus: AppointmentStatus,
        newStatus: AppointmentStatus
    ): Result<Unit> {
        
        // Define allowed transitions
        val allowedTransitions = when (currentStatus) {
            AppointmentStatus.SCHEDULED -> setOf(
                AppointmentStatus.COMPLETED,
                AppointmentStatus.CANCELLED,
                AppointmentStatus.NO_SHOW
            )
            AppointmentStatus.COMPLETED -> emptySet() // Completed appointments cannot be changed
            AppointmentStatus.CANCELLED -> setOf(AppointmentStatus.SCHEDULED) // Can reschedule cancelled appointments
            AppointmentStatus.NO_SHOW -> setOf(AppointmentStatus.SCHEDULED) // Can reschedule no-show appointments
        }
        
        if (newStatus !in allowedTransitions) {
            val currentStatusText = when (currentStatus) {
                AppointmentStatus.SCHEDULED -> "Planlandı"
                AppointmentStatus.COMPLETED -> "Tamamlandı"
                AppointmentStatus.CANCELLED -> "İptal edildi"
                AppointmentStatus.NO_SHOW -> "Gelmedi"
            }
            
            val newStatusText = when (newStatus) {
                AppointmentStatus.SCHEDULED -> "Planlandı"
                AppointmentStatus.COMPLETED -> "Tamamlandı"
                AppointmentStatus.CANCELLED -> "İptal edildi"
                AppointmentStatus.NO_SHOW -> "Gelmedi"
            }
            
            return Result.failure(
                IllegalArgumentException("$currentStatusText durumundan $newStatusText durumuna geçiş yapılamaz")
            )
        }
        
        return Result.success(Unit)
    }
    
    /**
     * Marks appointment as completed
     * Convenience method for common operation
     */
    suspend fun markAsCompleted(appointmentId: String, businessId: String): Result<Unit> {
        return invoke(appointmentId, AppointmentStatus.COMPLETED, businessId)
    }
    
    /**
     * Marks appointment as cancelled
     * Convenience method for common operation
     */
    suspend fun markAsCancelled(appointmentId: String, businessId: String): Result<Unit> {
        return invoke(appointmentId, AppointmentStatus.CANCELLED, businessId)
    }
    
    /**
     * Marks appointment as no-show
     * Convenience method for common operation
     */
    suspend fun markAsNoShow(appointmentId: String, businessId: String): Result<Unit> {
        return invoke(appointmentId, AppointmentStatus.NO_SHOW, businessId)
    }
    
    /**
     * Reschedules appointment back to scheduled
     * Convenience method for rescheduling cancelled/no-show appointments
     */
    suspend fun reschedule(appointmentId: String, businessId: String): Result<Unit> {
        return invoke(appointmentId, AppointmentStatus.SCHEDULED, businessId)
    }
} 