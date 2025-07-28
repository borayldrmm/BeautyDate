package com.borayildirim.beautydate.data.remote.models

import com.borayildirim.beautydate.data.models.Appointment
import com.borayildirim.beautydate.data.models.AppointmentStatus
import com.google.firebase.Timestamp

/**
 * Firestore-specific appointment model
 * Optimized for Firebase storage and retrieval
 * Separate from domain model to allow independent evolution
 * Memory efficient: primitive types for Firestore serialization
 */
data class AppointmentFirestore(
    val id: String = "",
    val customerId: String = "",
    val customerName: String = "",
    val customerPhone: String = "",
    val serviceId: String? = null,
    val serviceName: String = "",
    val servicePrice: Double = 0.0,
    val appointmentDate: String = "", // dd/MM/yyyy format
    val appointmentTime: String = "", // HH:mm format
    val status: String = "", // Store as String for Firestore
    val notes: String = "",
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null,
    val businessId: String = "",
    // Firestore-specific metadata
    val syncVersion: Long = 1,
    val lastModifiedBy: String = "",
    val isDeleted: Boolean = false
) {
    companion object {
        /**
         * Converts domain model to Firestore model
         * Memory efficient: direct field mapping
         */
        fun fromDomainModel(appointment: Appointment, lastModifiedBy: String = ""): AppointmentFirestore {
            return AppointmentFirestore(
                id = appointment.id,
                customerId = appointment.customerId,
                customerName = appointment.customerName,
                customerPhone = appointment.customerPhone,
                serviceId = appointment.serviceId,
                serviceName = appointment.serviceName,
                servicePrice = appointment.servicePrice,
                appointmentDate = appointment.appointmentDate,
                appointmentTime = appointment.appointmentTime,
                status = appointment.status.name,
                notes = appointment.notes,
                createdAt = appointment.createdAt,
                updatedAt = appointment.updatedAt,
                businessId = appointment.businessId,
                syncVersion = 1,
                lastModifiedBy = lastModifiedBy,
                isDeleted = false
            )
        }
    }
    
    /**
     * Converts Firestore model to domain model
     * Memory efficient: direct field mapping with enum conversion
     */
    fun toDomainModel(): Appointment {
        return Appointment(
            id = id,
            customerId = customerId,
            customerName = customerName,
            customerPhone = customerPhone,
            serviceId = serviceId,
            serviceName = serviceName,
            servicePrice = servicePrice,
            appointmentDate = appointmentDate,
            appointmentTime = appointmentTime,
            status = try {
                AppointmentStatus.valueOf(status)
            } catch (e: IllegalArgumentException) {
                AppointmentStatus.SCHEDULED // Default fallback
            },
            notes = notes,
            createdAt = createdAt,
            updatedAt = updatedAt,
            businessId = businessId
        )
    }
} 