package com.borayildirim.beautydate.data.models

import com.google.firebase.Timestamp
import java.util.UUID

/**
 * Transaction domain model for BeautyDate business app
 * Represents financial transactions with categorization
 * Follows exact same pattern as Customer model
 */
data class Transaction(
    val id: String = "",
    val paymentId: String = "",
    val appointmentId: String = "",
    val customerId: String = "",
    val type: TransactionType = TransactionType.INCOME,
    val category: TransactionCategory = TransactionCategory.SERVICE,
    val amount: Double = 0.0,
    val description: String = "",
    val reference: String = "",
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null,
    val businessId: String = ""
) {
    companion object {
        /**
         * Generates a unique transaction ID for new transactions
         */
        fun generateTransactionId(): String = UUID.randomUUID().toString()
        
        /**
         * Creates a transaction from payment
         */
        fun fromPayment(
            payment: Payment,
            type: TransactionType = TransactionType.INCOME,
            category: TransactionCategory = TransactionCategory.SERVICE
        ): Transaction {
            return Transaction(
                id = generateTransactionId(),
                paymentId = payment.id,
                appointmentId = payment.appointmentId,
                customerId = payment.customerId,
                type = type,
                category = category,
                amount = payment.amount,
                description = "Ödeme: ${payment.serviceName} - ${payment.customerName}",
                reference = payment.id,
                createdAt = Timestamp.now(),
                updatedAt = Timestamp.now(),
                businessId = payment.businessId
            )
        }
    }
    
    /**
     * Returns formatted amount with currency symbol and sign
     */
    val formattedAmount: String
        get() = when (type) {
            TransactionType.INCOME -> "+${amount.toInt()} ₺"
            TransactionType.EXPENSE -> "-${amount.toInt()} ₺"
        }
    
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
     * Validates transaction data for required fields
     */
    fun isValid(): Boolean {
        return amount > 0.0 &&
                description.isNotBlank() &&
                businessId.isNotBlank()
    }
    
    /**
     * Creates a copy with updated timestamp
     */
    fun withUpdatedTimestamp(): Transaction {
        return copy(updatedAt = Timestamp.now())
    }
    
    /**
     * Returns true if transaction is an income
     */
    val isIncome: Boolean
        get() = type == TransactionType.INCOME
}

/**
 * Transaction type enumeration
 */
enum class TransactionType {
    INCOME,
    EXPENSE;
    
    fun getDisplayName(): String {
        return when (this) {
            INCOME -> "Gelir"
            EXPENSE -> "Gider"
        }
    }
}

/**
 * Transaction category enumeration
 */
enum class TransactionCategory {
    SERVICE,
    PRODUCT,
    SALARY,
    RENT,
    UTILITIES,
    MARKETING,
    OTHER;
    
    fun getDisplayName(): String {
        return when (this) {
            SERVICE -> "Hizmet"
            PRODUCT -> "Ürün"
            SALARY -> "Maaş"
            RENT -> "Kira"
            UTILITIES -> "Faturalar"
            MARKETING -> "Pazarlama"
            OTHER -> "Diğer"
        }
    }
} 