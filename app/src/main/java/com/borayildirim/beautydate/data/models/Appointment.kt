package com.borayildirim.beautydate.data.models

import com.google.firebase.Timestamp
import java.util.UUID

/**
 * Appointment domain model for BeautyDate business app
 * Represents appointments between customers and business with status tracking
 * Memory efficient: immutable data class with business logic
 */
data class Appointment(
    val id: String = "",
    val customerId: String = "",
    val customerName: String = "",
    val customerPhone: String = "",
    val serviceId: String? = null,
    val serviceName: String = "",
    val servicePrice: Double = 0.0,
    val appointmentDate: String = "", // dd/MM/yyyy format
    val appointmentTime: String = "", // HH:mm format
    val status: AppointmentStatus = AppointmentStatus.SCHEDULED,
    val notes: String = "",
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null,
    val businessId: String = ""
) {
    companion object {
        /**
         * Generates a unique appointment ID for new appointments
         * Memory efficient: UUID generation
         */
        fun generateAppointmentId(): String = UUID.randomUUID().toString()
        
        /**
         * Creates an appointment for a specific customer
         * Memory efficient: single object creation with customer data
         */
        fun createForCustomer(
            customer: Customer,
            serviceName: String,
            servicePrice: Double,
            appointmentDate: String,
            appointmentTime: String,
            businessId: String,
            notes: String = "",
            serviceId: String? = null
        ): Appointment {
            return Appointment(
                id = generateAppointmentId(),
                customerId = customer.id,
                customerName = customer.fullName,
                customerPhone = customer.phoneNumber,
                serviceId = serviceId,
                serviceName = serviceName,
                servicePrice = servicePrice,
                appointmentDate = appointmentDate,
                appointmentTime = appointmentTime,
                status = AppointmentStatus.SCHEDULED,
                notes = notes,
                createdAt = Timestamp.now(),
                updatedAt = Timestamp.now(),
                businessId = businessId
            )
        }
    }
    
    /**
     * Returns formatted appointment date and time
     * Memory efficient: string concatenation
     */
    val formattedDateTime: String
        get() = "$appointmentDate $appointmentTime"
    
    /**
     * Returns formatted price with currency symbol
     * Memory efficient: string formatting
     */
    val formattedPrice: String
        get() = "${servicePrice.toInt()} ₺"
    
    /**
     * Validates appointment data for required fields
     * Business logic: ensures all required fields are present
     */
    fun isValid(): Boolean {
        return customerId.isNotBlank() &&
                customerName.isNotBlank() &&
                serviceName.isNotBlank() &&
                appointmentDate.isNotBlank() &&
                appointmentTime.isNotBlank() &&
                businessId.isNotBlank()
    }
    
    /**
     * Checks if appointment can be cancelled
     * Business logic: only scheduled appointments can be cancelled
     */
    fun canBeCancelled(): Boolean {
        return status == AppointmentStatus.SCHEDULED
    }
    
    /**
     * Checks if appointment can be completed
     * Business logic: only scheduled appointments can be completed
     */
    fun canBeCompleted(): Boolean {
        return status == AppointmentStatus.SCHEDULED
    }
    
    /**
     * Checks if appointment can be edited
     * Business logic: only scheduled appointments can be edited
     */
    fun canBeEdited(): Boolean {
        return status == AppointmentStatus.SCHEDULED
    }
}

/**
 * Appointment status enumeration
 * Represents the current state of an appointment
 * Memory efficient: enum with Turkish display names
 */
enum class AppointmentStatus {
    SCHEDULED,    // Planlandı
    COMPLETED,    // Tamamlandı
    CANCELLED,    // İptal Edildi
    NO_SHOW;      // Gelmedi
    
    /**
     * Returns Turkish display name for status
     * UI support: localized status names
     */
    fun getDisplayName(): String {
        return when (this) {
            SCHEDULED -> "Planlandı"
            COMPLETED -> "Tamamlandı"
            CANCELLED -> "İptal Edildi"
            NO_SHOW -> "Gelmedi"
        }
    }
    
    /**
     * Returns color for status display
     * UI support: status-based colors for better UX
     */
    fun getColor(): Long {
        return when (this) {
            SCHEDULED -> 0xFF2196F3 // Blue
            COMPLETED -> 0xFF4CAF50 // Green
            CANCELLED -> 0xFFE91E63 // Pink
            NO_SHOW -> 0xFFFF9800   // Orange
        }
    }
    
    /**
     * Returns icon name for status
     * UI support: status-based icons
     */
    fun getIconName(): String {
        return when (this) {
            SCHEDULED -> "Schedule"
            COMPLETED -> "CheckCircle"
            CANCELLED -> "Cancel"
            NO_SHOW -> "PersonOff"
        }
    }
} 