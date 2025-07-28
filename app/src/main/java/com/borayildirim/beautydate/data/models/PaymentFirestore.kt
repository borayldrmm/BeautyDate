package com.borayildirim.beautydate.data.models

import com.google.firebase.Timestamp

/**
 * Firestore-specific payment model
 * Optimized for Firebase storage and retrieval
 * Follows exact same pattern as CustomerFirestore
 */
data class PaymentFirestore(
    val id: String = "",
    val appointmentId: String = "",
    val customerId: String = "",
    val customerName: String = "",
    val serviceName: String = "",
    val amount: Double = 0.0,
    val paymentMethod: String = "",
    val status: String = "",
    val notes: String = "",
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null,
    val businessId: String = "",
    // Firestore-specific metadata
    val syncVersion: Long = 1,
    val lastModifiedBy: String = "",
    val isActive: Boolean = true
) {
    companion object {
        /**
         * Converts domain Payment model to Firestore model
         */
        fun fromDomainModel(payment: Payment, lastModifiedBy: String = ""): PaymentFirestore {
            return PaymentFirestore(
                id = payment.id,
                appointmentId = payment.appointmentId,
                customerId = payment.customerId,
                customerName = payment.customerName,
                serviceName = payment.serviceName,
                amount = payment.amount,
                paymentMethod = payment.paymentMethod.name,
                status = payment.status.name,
                notes = payment.notes,
                createdAt = payment.createdAt,
                updatedAt = payment.updatedAt,
                businessId = payment.businessId,
                syncVersion = System.currentTimeMillis(),
                lastModifiedBy = lastModifiedBy,
                isActive = true
            )
        }
    }
    
    /**
     * Converts Firestore model to domain Payment model
     */
    fun toDomainModel(): Payment {
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
            createdAt = createdAt,
            updatedAt = updatedAt,
            businessId = businessId
        )
    }
    
    /**
     * Returns formatted amount for Firestore queries
     */
    val formattedAmount: String
        get() = "${amount.toInt()} ₺"
    
    /**
     * Validates Firestore data
     */
    fun isValid(): Boolean {
        return appointmentId.isNotBlank() &&
                customerId.isNotBlank() &&
                customerName.isNotBlank() &&
                serviceName.isNotBlank() &&
                businessId.isNotBlank() &&
                id.isNotBlank()
    }
} 