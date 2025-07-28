package com.borayildirim.beautydate.data.models

import com.google.firebase.Timestamp
import java.util.UUID

/**
 * Expense domain model for BeautyDate business app
 * Represents business expenses with categorization and tracking
 * Memory efficient: immutable data class with business logic
 */
data class Expense(
    val id: String = "",
    val category: ExpenseCategory = ExpenseCategory.GENERAL_BUSINESS_EXPENSES,
    val subcategory: String = "",
    val amount: Double = 0.0,
    val description: String = "",
    val expenseDate: String = "", // dd/MM/yyyy format
    val notes: String = "",
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null,
    val businessId: String = "",
    val isDeleted: Boolean = false
) {
    companion object {
        /**
         * Generates a unique expense ID
         * Memory efficient: UUID generation
         */
        fun generateExpenseId(): String = UUID.randomUUID().toString()
        
        /**
         * Creates a new expense with validation
         * Memory efficient: single object creation with validation
         */
        fun createNew(
            category: ExpenseCategory,
            subcategory: String,
            amount: Double,
            description: String,
            expenseDate: String,
            businessId: String,
            notes: String = ""
        ): Expense {
            return Expense(
                id = generateExpenseId(),
                category = category,
                subcategory = subcategory,
                amount = amount,
                description = description,
                expenseDate = expenseDate,
                notes = notes,
                createdAt = Timestamp.now(),
                updatedAt = Timestamp.now(),
                businessId = businessId,
                isDeleted = false
            )
        }
    }
    
    /**
     * Returns formatted price with currency symbol
     * Memory efficient: string formatting
     */
    val formattedAmount: String
        get() = "${amount.toInt()} ₺"
    
    /**
     * Returns formatted category with subcategory
     * UI support: category display
     */
    val formattedCategory: String
        get() = "${category.displayName} - $subcategory"
    
    /**
     * Validates expense data for required fields
     * Business logic: ensures all required fields are present
     */
    fun isValid(): Boolean {
        return category != null &&
                subcategory.isNotBlank() &&
                amount > 0.0 &&
                description.isNotBlank() &&
                expenseDate.isNotBlank() &&
                businessId.isNotBlank()
    }
    
    /**
     * Checks if expense is from current month
     * Business logic: monthly expense filtering
     */
    fun isFromCurrentMonth(): Boolean {
        val currentMonth = java.time.LocalDate.now().monthValue
        val currentYear = java.time.LocalDate.now().year
        
        return try {
            val expenseLocalDate = java.time.LocalDate.parse(
                expenseDate,
                java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")
            )
            expenseLocalDate.monthValue == currentMonth && expenseLocalDate.year == currentYear
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Gets expense month for grouping
     * Business logic: monthly expense grouping
     */
    fun getExpenseMonth(): String {
        return try {
            val expenseLocalDate = java.time.LocalDate.parse(
                expenseDate,
                java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")
            )
            "${expenseLocalDate.monthValue}/${expenseLocalDate.year}"
        } catch (e: Exception) {
            "Bilinmeyen"
        }
    }
    
    /**
     * Checks if expense matches search query
     * Business logic: search functionality
     */
    fun matchesSearch(query: String): Boolean {
        val lowercaseQuery = query.lowercase()
        return description.lowercase().contains(lowercaseQuery) ||
                subcategory.lowercase().contains(lowercaseQuery) ||
                category.displayName.lowercase().contains(lowercaseQuery) ||
                notes.lowercase().contains(lowercaseQuery)
    }
} 