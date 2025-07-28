package com.borayildirim.beautydate.data.models

import com.google.firebase.Timestamp
import java.util.UUID

/**
 * Payment domain model for BeautyDate business app
 * Represents payment transactions with comprehensive tracking
 * Follows exact same pattern as Customer model
 */
data class Payment(
    val id: String = "",
    val appointmentId: String = "",
    val customerId: String = "",
    val customerName: String = "",
    val serviceName: String = "",
    val amount: Double = 0.0,
    val paymentMethod: PaymentMethod = PaymentMethod.CASH,
    val status: PaymentStatus = PaymentStatus.PENDING,
    val notes: String = "",
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null,
    val businessId: String = ""
) {
    companion object {
        /**
         * Generates a unique payment ID for new payments
         */
        fun generatePaymentId(): String = UUID.randomUUID().toString()
        
        /**
         * Creates a payment from appointment data
         */
        fun fromAppointment(
            appointment: Appointment,
            paymentMethod: PaymentMethod,
            businessId: String
        ): Payment {
            return Payment(
                id = generatePaymentId(),
                appointmentId = appointment.id,
                customerId = appointment.customerId,
                customerName = appointment.customerName,
                serviceName = appointment.serviceName,
                amount = appointment.servicePrice,
                paymentMethod = paymentMethod,
                status = PaymentStatus.PENDING,
                notes = "",
                createdAt = Timestamp.now(),
                updatedAt = Timestamp.now(),
                businessId = businessId
            )
        }
    }
    
    /**
     * Returns formatted amount with currency symbol
     */
    val formattedAmount: String
        get() = "${amount.toInt()} ₺"
    
    /**
     * Returns formatted creation date
     */
    val formattedCreatedDate: String
        get() {
            return createdAt?.let { timestamp ->
                val date = timestamp.toDate()
                val formatter = java.text.SimpleDateFormat("dd.MM.yyyy HH:mm", java.util.Locale.getDefault())
                formatter.format(date)
            } ?: ""
        }
    
    /**
     * Validates payment data for required fields
     */
    fun isValid(): Boolean {
        return appointmentId.isNotBlank() &&
                customerId.isNotBlank() &&
                customerName.isNotBlank() &&
                serviceName.isNotBlank() &&
                amount > 0.0 &&
                businessId.isNotBlank()
    }
    
    /**
     * Creates a copy with updated timestamp
     */
    fun withUpdatedTimestamp(): Payment {
        return copy(updatedAt = Timestamp.now())
    }
    
    /**
     * Returns true if payment is completed
     */
    val isCompleted: Boolean
        get() = status == PaymentStatus.COMPLETED
}

/**
 * Payment method enumeration
 */
enum class PaymentMethod {
    CASH,
    CREDIT_CARD,
    BANK_TRANSFER;
    
    fun getDisplayName(): String {
        return when (this) {
            CASH -> "Nakit"
            CREDIT_CARD -> "Kredi Kartı"
            BANK_TRANSFER -> "Banka Transferi"
        }
    }
}

/**
 * Payment status enumeration
 */
enum class PaymentStatus {
    PENDING,
    COMPLETED,
    FAILED,
    REFUNDED;
    
    fun getDisplayName(): String {
        return when (this) {
            PENDING -> "Beklemede"
            COMPLETED -> "Tamamlandı"
            FAILED -> "Başarısız"
            REFUNDED -> "İade Edildi"
        }
    }
} 