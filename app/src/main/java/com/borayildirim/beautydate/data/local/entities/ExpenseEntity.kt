package com.borayildirim.beautydate.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index
import com.google.firebase.Timestamp
import com.borayildirim.beautydate.data.models.Expense
import com.borayildirim.beautydate.data.models.ExpenseCategory

/**
 * Expense Room entity for local database storage
 * Follows exact same pattern as other entities
 * Memory efficient: indexed fields for fast queries
 */
@Entity(
    tableName = "expenses",
    indices = [
        Index(value = ["businessId"]),
        Index(value = ["category"]),
        Index(value = ["expenseDate"]),
        Index(value = ["createdAt"])
    ]
)
data class ExpenseEntity(
    @PrimaryKey
    val id: String,
    val category: String, // Store enum as String
    val subcategory: String,
    val amount: Double,
    val description: String,
    val expenseDate: String, // dd/MM/yyyy format
    val notes: String,
    val createdAt: Long, // Timestamp as Long for Room
    val updatedAt: Long, // Timestamp as Long for Room
    val businessId: String,
    val isDeleted: Boolean = false,
    val needsSync: Boolean = false // For sync management
) {
    companion object {
        /**
         * Creates ExpenseEntity from domain model
         * Memory efficient: single conversion operation
         */
        fun fromDomainModel(expense: Expense, needsSync: Boolean = false): ExpenseEntity {
            return ExpenseEntity(
                id = expense.id,
                category = expense.category.name,
                subcategory = expense.subcategory,
                amount = expense.amount,
                description = expense.description,
                expenseDate = expense.expenseDate,
                notes = expense.notes,
                createdAt = expense.createdAt?.toDate()?.time ?: System.currentTimeMillis(),
                updatedAt = expense.updatedAt?.toDate()?.time ?: System.currentTimeMillis(),
                businessId = expense.businessId,
                isDeleted = expense.isDeleted,
                needsSync = needsSync
            )
        }
    }
    
    /**
     * Converts entity to domain model
     * Memory efficient: single conversion operation
     */
    fun toDomainModel(): Expense {
        return Expense(
            id = id,
            category = ExpenseCategory.valueOf(category),
            subcategory = subcategory,
            amount = amount,
            description = description,
            expenseDate = expenseDate,
            notes = notes,
            createdAt = Timestamp(java.util.Date(createdAt)),
            updatedAt = Timestamp(java.util.Date(updatedAt)),
            businessId = businessId,
            isDeleted = isDeleted
        )
    }
} 