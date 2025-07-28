package com.borayildirim.beautydate.data.models

import com.google.firebase.Timestamp

/**
 * Firestore-specific transaction model
 * Optimized for Firebase storage and retrieval
 * Follows exact same pattern as CustomerFirestore
 */
data class TransactionFirestore(
    val id: String = "",
    val paymentId: String = "",
    val appointmentId: String = "",
    val customerId: String = "",
    val type: String = "",
    val category: String = "",
    val amount: Double = 0.0,
    val description: String = "",
    val reference: String = "",
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
         * Converts domain Transaction model to Firestore model
         */
        fun fromDomainModel(transaction: Transaction, lastModifiedBy: String = ""): TransactionFirestore {
            return TransactionFirestore(
                id = transaction.id,
                paymentId = transaction.paymentId,
                appointmentId = transaction.appointmentId,
                customerId = transaction.customerId,
                type = transaction.type.name,
                category = transaction.category.name,
                amount = transaction.amount,
                description = transaction.description,
                reference = transaction.reference,
                createdAt = transaction.createdAt,
                updatedAt = transaction.updatedAt,
                businessId = transaction.businessId,
                syncVersion = System.currentTimeMillis(),
                lastModifiedBy = lastModifiedBy,
                isActive = true
            )
        }
    }
    
    /**
     * Converts Firestore model to domain Transaction model
     */
    fun toDomainModel(): Transaction {
        return Transaction(
            id = id,
            paymentId = paymentId,
            appointmentId = appointmentId,
            customerId = customerId,
            type = try {
                TransactionType.valueOf(type)
            } catch (e: Exception) {
                TransactionType.INCOME
            },
            category = try {
                TransactionCategory.valueOf(category)
            } catch (e: Exception) {
                TransactionCategory.SERVICE
            },
            amount = amount,
            description = description,
            reference = reference,
            createdAt = createdAt,
            updatedAt = updatedAt,
            businessId = businessId
        )
    }
    
    /**
     * Returns formatted amount for Firestore queries
     */
    val formattedAmount: String
        get() = when (type) {
            "INCOME" -> "+${amount.toInt()} ₺"
            "EXPENSE" -> "-${amount.toInt()} ₺"
            else -> "${amount.toInt()} ₺"
        }
    
    /**
     * Validates Firestore data
     */
    fun isValid(): Boolean {
        return amount > 0.0 &&
                description.isNotBlank() &&
                businessId.isNotBlank() &&
                id.isNotBlank()
    }
} 