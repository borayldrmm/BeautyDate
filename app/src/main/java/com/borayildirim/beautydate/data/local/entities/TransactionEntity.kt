package com.borayildirim.beautydate.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index
import com.google.firebase.Timestamp
import com.borayildirim.beautydate.data.models.Transaction
import com.borayildirim.beautydate.data.models.TransactionType
import com.borayildirim.beautydate.data.models.TransactionCategory

/**
 * Transaction Room entity for local database storage
 * Follows exact same pattern as CustomerEntity
 * Memory efficient: indexed fields for fast queries
 */
@Entity(
    tableName = "transactions",
    indices = [
        Index(value = ["paymentId"]),
        Index(value = ["appointmentId"]),
        Index(value = ["customerId"]),
        Index(value = ["businessId"]),
        Index(value = ["type"]),
        Index(value = ["category"])
    ]
)
data class TransactionEntity(
    @PrimaryKey
    val id: String,
    val paymentId: String,
    val appointmentId: String,
    val customerId: String,
    val type: String,
    val category: String,
    val amount: Double,
    val description: String,
    val reference: String,
    val createdAt: Long,
    val updatedAt: Long,
    val businessId: String
) {
    companion object {
        /**
         * Converts domain Transaction model to entity
         */
        fun fromDomain(transaction: Transaction): TransactionEntity {
            return TransactionEntity(
                id = transaction.id,
                paymentId = transaction.paymentId,
                appointmentId = transaction.appointmentId,
                customerId = transaction.customerId,
                type = transaction.type.name,
                category = transaction.category.name,
                amount = transaction.amount,
                description = transaction.description,
                reference = transaction.reference,
                createdAt = transaction.createdAt?.seconds ?: 0L,
                updatedAt = transaction.updatedAt?.seconds ?: 0L,
                businessId = transaction.businessId
            )
        }
    }
    
    /**
     * Converts entity to domain Transaction model
     */
    fun toDomain(): Transaction {
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
            createdAt = if (createdAt > 0) Timestamp(createdAt, 0) else null,
            updatedAt = if (updatedAt > 0) Timestamp(updatedAt, 0) else null,
            businessId = businessId
        )
    }
} 