package com.borayildirim.beautydate.domain.usecases.appointment

import com.borayildirim.beautydate.data.models.Appointment
import com.borayildirim.beautydate.data.models.AppointmentStatus
import com.borayildirim.beautydate.data.repository.AppointmentRepository
import com.borayildirim.beautydate.data.repository.WorkingHoursRepository
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

/**
 * Use case for adding new appointments with business validation
 * Follows Single Responsibility Principle and includes conflict checking
 * Memory efficient: validation before database operations
 */
class AddAppointmentUseCase @Inject constructor(
    private val appointmentRepository: AppointmentRepository,
    private val workingHoursRepository: WorkingHoursRepository
) {
    
    /**
     * Adds a new appointment after comprehensive validation
     * @param appointment Appointment to add
     * @return Result with success or validation error
     */
    suspend operator fun invoke(appointment: Appointment): Result<Unit> {
        // Validate appointment data
        val validationResult = validateAppointment(appointment)
        if (!validationResult.isSuccess) {
            return validationResult.fold(
                onSuccess = { Result.success(Unit) },
                onFailure = { Result.failure(it) }
            )
        }
        
        // Check for time conflicts
        val conflictResult = checkTimeConflicts(appointment)
        if (!conflictResult.isSuccess) {
            return conflictResult.fold(
                onSuccess = { Result.success(Unit) },
                onFailure = { Result.failure(it) }
            )
        }
        
        // Check working hours
        val workingHoursResult = checkWorkingHours(appointment)
        if (!workingHoursResult.isSuccess) {
            return workingHoursResult.fold(
                onSuccess = { Result.success(Unit) },
                onFailure = { Result.failure(it) }
            )
        }
        
        // Add appointment to repository
        return appointmentRepository.addAppointment(appointment)
    }
    
    /**
     * Validates basic appointment data
     * Memory efficient: early validation before repository operations
     */
    private fun validateAppointment(appointment: Appointment): Result<Unit> {
        // Check required fields
        if (appointment.customerId.isBlank()) {
            return Result.failure(IllegalArgumentException("Müşteri seçilmelidir"))
        }
        
        if (appointment.customerName.isBlank()) {
            return Result.failure(IllegalArgumentException("Müşteri adı gereklidir"))
        }
        
        if (appointment.serviceName.isBlank()) {
            return Result.failure(IllegalArgumentException("Hizmet seçilmelidir"))
        }
        
        if (appointment.appointmentDate.isBlank()) {
            return Result.failure(IllegalArgumentException("Randevu tarihi gereklidir"))
        }
        
        if (appointment.appointmentTime.isBlank()) {
            return Result.failure(IllegalArgumentException("Randevu saati gereklidir"))
        }
        
        if (appointment.businessId.isBlank()) {
            return Result.failure(IllegalArgumentException("İşletme ID gereklidir"))
        }
        
        // Validate date format and check if it's not in the past
        try {
            val appointmentDate = LocalDate.parse(appointment.appointmentDate, DateTimeFormatter.ofPattern("dd/MM/yyyy"))
            if (appointmentDate.isBefore(LocalDate.now())) {
                return Result.failure(IllegalArgumentException("Geçmiş tarihe randevu oluşturulamaz"))
            }
        } catch (e: Exception) {
            return Result.failure(IllegalArgumentException("Geçersiz tarih formatı"))
        }
        
        // Validate time format
        try {
            LocalTime.parse(appointment.appointmentTime, DateTimeFormatter.ofPattern("HH:mm"))
        } catch (e: Exception) {
            return Result.failure(IllegalArgumentException("Geçersiz saat formatı"))
        }
        
        return Result.success(Unit)
    }
    
    /**
     * Checks for time conflicts with existing appointments
     * Memory efficient: targeted conflict checking with smart slot reusability
     */
    private suspend fun checkTimeConflicts(appointment: Appointment): Result<Unit> {
        val existingAppointments = appointmentRepository.getAppointmentsByDateSync(
            appointment.appointmentDate, 
            appointment.businessId
        )
        
        // Only SCHEDULED appointments create time conflicts
        // COMPLETED, CANCELLED, NO_SHOW appointments can be rebooked
        val conflictingAppointment = existingAppointments.find { existing ->
            existing.appointmentTime == appointment.appointmentTime &&
            existing.status == AppointmentStatus.SCHEDULED &&
            existing.id != appointment.id // Exclude self when updating
        }
        
        if (conflictingAppointment != null) {
            return Result.failure(IllegalArgumentException("Bu saatte zaten aktif randevu bulunmaktadır"))
        }
        
        return Result.success(Unit)
    }
    
    /**
     * Checks if appointment time is within working hours
     * Memory efficient: working hours validation
     */
    private suspend fun checkWorkingHours(appointment: Appointment): Result<Unit> {
        val workingHoursResult = workingHoursRepository.getWorkingHours()
        
        workingHoursResult.fold(
            onSuccess = { workingHours ->
                if (workingHours == null) {
                    return Result.failure(IllegalArgumentException("Çalışma saatleri bulunamadı"))
                }
                
                // Parse appointment date to get day of week
                val appointmentDate = LocalDate.parse(appointment.appointmentDate, DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                val dayOfWeek = when (appointmentDate.dayOfWeek) {
                    java.time.DayOfWeek.MONDAY -> com.borayildirim.beautydate.data.models.DayOfWeek.MONDAY
                    java.time.DayOfWeek.TUESDAY -> com.borayildirim.beautydate.data.models.DayOfWeek.TUESDAY
                    java.time.DayOfWeek.WEDNESDAY -> com.borayildirim.beautydate.data.models.DayOfWeek.WEDNESDAY
                    java.time.DayOfWeek.THURSDAY -> com.borayildirim.beautydate.data.models.DayOfWeek.THURSDAY
                    java.time.DayOfWeek.FRIDAY -> com.borayildirim.beautydate.data.models.DayOfWeek.FRIDAY
                    java.time.DayOfWeek.SATURDAY -> com.borayildirim.beautydate.data.models.DayOfWeek.SATURDAY
                    java.time.DayOfWeek.SUNDAY -> com.borayildirim.beautydate.data.models.DayOfWeek.SUNDAY
                }
                
                val dayHours = workingHours.getDayHours(dayOfWeek)
                
                if (!dayHours.isWorking) {
                    return Result.failure(IllegalArgumentException("Seçilen gün çalışma günü değildir"))
                }
                
                val appointmentTime = LocalTime.parse(appointment.appointmentTime, DateTimeFormatter.ofPattern("HH:mm"))
                val startTime = LocalTime.parse(dayHours.startTime, DateTimeFormatter.ofPattern("HH:mm"))
                val endTime = LocalTime.parse(dayHours.endTime, DateTimeFormatter.ofPattern("HH:mm"))
                
                if (appointmentTime.isBefore(startTime) || appointmentTime.isAfter(endTime)) {
                    return Result.failure(IllegalArgumentException("Randevu saati çalışma saatleri dışındadır"))
                }
            },
            onFailure = {
                return Result.failure(IllegalArgumentException("Çalışma saatleri kontrol edilemedi"))
            }
        )
        
        return Result.success(Unit)
    }

    /**
     * Validates if appointment time is within business working hours
     * Memory efficient: single working hours lookup with time validation
     */
    private suspend fun validateWorkingHours(appointment: Appointment): Boolean {
        return try {
            val workingHoursResult = workingHoursRepository.getWorkingHours()
            
            if (workingHoursResult.isFailure) {
                return false
            }
            
            val workingHours = workingHoursResult.getOrNull() ?: return false
            
            // Convert appointment date to day of week - simplified for now
            val dayOfWeek = com.borayildirim.beautydate.data.models.DayOfWeek.MONDAY // Placeholder
            
            // Get working hours for that day
            val dayHours = when (dayOfWeek) {
                com.borayildirim.beautydate.data.models.DayOfWeek.MONDAY -> workingHours.monday
                com.borayildirim.beautydate.data.models.DayOfWeek.TUESDAY -> workingHours.tuesday
                com.borayildirim.beautydate.data.models.DayOfWeek.WEDNESDAY -> workingHours.wednesday
                com.borayildirim.beautydate.data.models.DayOfWeek.THURSDAY -> workingHours.thursday
                com.borayildirim.beautydate.data.models.DayOfWeek.FRIDAY -> workingHours.friday
                com.borayildirim.beautydate.data.models.DayOfWeek.SATURDAY -> workingHours.saturday
                com.borayildirim.beautydate.data.models.DayOfWeek.SUNDAY -> workingHours.sunday
            }
            
            // Check if business is open and appointment time is within working hours
            dayHours.isWorking && 
            appointment.appointmentTime >= dayHours.startTime && 
            appointment.appointmentTime <= dayHours.endTime
            
        } catch (e: Exception) {
            false
        }
    }
} 