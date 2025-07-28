package com.borayildirim.beautydate.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index
import com.borayildirim.beautydate.data.models.Appointment
import com.borayildirim.beautydate.data.models.AppointmentStatus
import com.google.firebase.Timestamp

/**
 * Room entity for appointment storage
 * Optimized for local database storage with indexing for efficient queries
 * Memory efficient: primitive types and indexed fields for quick searches
 */
@Entity(
    tableName = "appointments",
    indices = [
        Index(value = ["customerId"]),
        Index(value = ["businessId"]),
        Index(value = ["appointmentDate"]),
        Index(value = ["status"]),
        Index(value = ["customerName"]),
        Index(value = ["serviceId"])
    ]
)
data class AppointmentEntity(
    @PrimaryKey
    val id: String,
    val customerId: String,
    val customerName: String,
    val customerPhone: String,
    val serviceId: String?,
    val serviceName: String,
    val servicePrice: Double,
    val appointmentDate: String, // dd/MM/yyyy format
    val appointmentTime: String, // HH:mm format
    val status: String, // Store as String for Room
    val notes: String,
    val createdAt: Long, // Timestamp as Long for Room
    val updatedAt: Long, // Timestamp as Long for Room
    val businessId: String,
    val isDeleted: Boolean = false,
    val needsSync: Boolean = false // For sync management
) {
    companion object {
        /**
         * Converts domain model to Room entity
         * Memory efficient: direct field mapping
         */
        fun fromDomainModel(appointment: Appointment, needsSync: Boolean = false): AppointmentEntity {
            return AppointmentEntity(
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
                createdAt = appointment.createdAt?.toDate()?.time ?: System.currentTimeMillis(),
                updatedAt = appointment.updatedAt?.toDate()?.time ?: System.currentTimeMillis(),
                businessId = appointment.businessId,
                needsSync = needsSync
            )
        }
        
        /**
         * Creates a new AppointmentEntity with default values
         * Memory efficient: minimal object creation
         */
        fun createNew(
            customerId: String,
            customerName: String,
            customerPhone: String,
            serviceId: String?,
            serviceName: String,
            servicePrice: Double,
            appointmentDate: String,
            appointmentTime: String,
            notes: String,
            businessId: String
        ): AppointmentEntity {
            val now = System.currentTimeMillis()
            return AppointmentEntity(
                id = Appointment.generateAppointmentId(),
                customerId = customerId,
                customerName = customerName,
                customerPhone = customerPhone,
                serviceId = serviceId,
                serviceName = serviceName,
                servicePrice = servicePrice,
                appointmentDate = appointmentDate,
                appointmentTime = appointmentTime,
                status = AppointmentStatus.SCHEDULED.name,
                notes = notes,
                createdAt = now,
                updatedAt = now,
                businessId = businessId,
                needsSync = true // New entities need sync
            )
        }
    }
    
    /**
     * Converts Room entity to domain model
     * Memory efficient: direct field mapping with type conversion
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
            status = AppointmentStatus.valueOf(status),
            notes = notes,
            createdAt = Timestamp(createdAt / 1000, ((createdAt % 1000) * 1000000).toInt()),
            updatedAt = Timestamp(updatedAt / 1000, ((updatedAt % 1000) * 1000000).toInt()),
            businessId = businessId
        )
    }
} 