package com.borayildirim.beautydate.data.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName

/**
 * Firestore model for Expense data
 * Follows exact same pattern as other Firestore models
 * Memory efficient: direct field mapping with Firestore annotations
 */
data class ExpenseFirestore(
    @PropertyName("id")
    val id: String = "",
    
    @PropertyName("category")
    val category: String = "",
    
    @PropertyName("subcategory") 
    val subcategory: String = "",
    
    @PropertyName("amount")
    val amount: Double = 0.0,
    
    @PropertyName("description")
    val description: String = "",
    
    @PropertyName("expenseDate")
    val expenseDate: String = "",
    
    @PropertyName("notes")
    val notes: String = "",
    
    @PropertyName("createdAt")
    val createdAt: Timestamp? = null,
    
    @PropertyName("updatedAt")
    val updatedAt: Timestamp? = null,
    
    @PropertyName("businessId")
    val businessId: String = "",
    
    @PropertyName("isDeleted")
    val isDeleted: Boolean = false,
    
    @PropertyName("lastModifiedBy")
    val lastModifiedBy: String = ""
) {
    companion object {
        /**
         * Creates Firestore model from domain model
         * Memory efficient: single conversion operation
         */
        fun fromDomainModel(expense: Expense, lastModifiedBy: String): ExpenseFirestore {
            return ExpenseFirestore(
                id = expense.id,
                category = expense.category.name,
                subcategory = expense.subcategory,
                amount = expense.amount,
                description = expense.description,
                expenseDate = expense.expenseDate,
                notes = expense.notes,
                createdAt = expense.createdAt,
                updatedAt = expense.updatedAt,
                businessId = expense.businessId,
                isDeleted = expense.isDeleted,
                lastModifiedBy = lastModifiedBy
            )
        }
    }
    
    /**
     * Converts Firestore model to domain model
     * Memory efficient: single conversion operation
     */
    fun toDomainModel(): Expense {
        return Expense(
            id = id,
            category = try { ExpenseCategory.valueOf(category) } catch (e: Exception) { ExpenseCategory.GENERAL_BUSINESS_EXPENSES },
            subcategory = subcategory,
            amount = amount,
            description = description,
            expenseDate = expenseDate,
            notes = notes,
            createdAt = createdAt,
            updatedAt = updatedAt,
            businessId = businessId,
            isDeleted = isDeleted
        )
    }
} 