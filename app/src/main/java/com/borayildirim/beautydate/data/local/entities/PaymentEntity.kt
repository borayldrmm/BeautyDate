package com.borayildirim.beautydate.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index
import com.google.firebase.Timestamp
import com.borayildirim.beautydate.data.models.Payment
import com.borayildirim.beautydate.data.models.PaymentMethod
import com.borayildirim.beautydate.data.models.PaymentStatus

/**
 * Payment Room entity for local database storage
 * Follows exact same pattern as CustomerEntity
 * Memory efficient: indexed fields for fast queries
 */
@Entity(
    tableName = "payments",
    indices = [
        Index(value = ["appointmentId"]),
        Index(value = ["customerId"]),
        Index(value = ["businessId"]),
        Index(value = ["status"]),
        Index(value = ["paymentMethod"])
    ]
)
data class PaymentEntity(
    @PrimaryKey
    val id: String,
    val appointmentId: String,
    val customerId: String,
    val customerName: String,
    val serviceName: String,
    val amount: Double,
    val paymentMethod: String,
    val status: String,
    val notes: String,
    val createdAt: Long,
    val updatedAt: Long,
    val businessId: String
) {
    companion object {
        /**
         * Converts domain Payment model to entity
         */
        fun fromDomain(payment: Payment): PaymentEntity {
            return PaymentEntity(
                id = payment.id,
                appointmentId = payment.appointmentId,
                customerId = payment.customerId,
                customerName = payment.customerName,
                serviceName = payment.serviceName,
                amount = payment.amount,
                paymentMethod = payment.paymentMethod.name,
                status = payment.status.name,
                notes = payment.notes,
                createdAt = payment.createdAt?.seconds ?: 0L,
                updatedAt = payment.updatedAt?.seconds ?: 0L,
                businessId = payment.businessId
            )
        }
    }
    
    /**
     * Converts entity to domain Payment model
     */
    fun toDomain(): Payment {
        return Payment(
            id = id,
            appointmentId = appointmentId,
            customerId = customerId,
            customerName = customerName,
            serviceName = serviceName,
            amount = amount,
            paymentMethod = try {
                PaymentMethod.valueOf(paymentMethod)
            } catch (e: Exception) {
                PaymentMethod.CASH
            },
            status = try {
                PaymentStatus.valueOf(status)
            } catch (e: Exception) {
                PaymentStatus.PENDING
            },
            notes = notes,
            createdAt = if (createdAt > 0) Timestamp(createdAt, 0) else null,
            updatedAt = if (updatedAt > 0) Timestamp(updatedAt, 0) else null,
            businessId = businessId
        )
    }
} 