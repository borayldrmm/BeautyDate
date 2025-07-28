package com.borayildirim.beautydate.data.repository

import kotlinx.coroutines.flow.Flow
import com.borayildirim.beautydate.data.models.Expense
import com.borayildirim.beautydate.data.models.ExpenseCategory

/**
 * Expense repository interface
 * Follows exact same pattern as other repositories
 * Handles expense data operations with offline-first approach
 */
interface ExpenseRepository {
    
    /**
     * Gets all expenses for a business
     * Returns Flow for reactive UI updates
     */
    fun getAllExpenses(businessId: String): Flow<List<Expense>>
    
    /**
     * Gets expenses by category
     */
    fun getExpensesByCategory(category: ExpenseCategory, businessId: String): Flow<List<Expense>>
    
    /**
     * Gets expense by ID
     */
    suspend fun getExpenseById(id: String, businessId: String): Expense?
    
    /**
     * Gets total expenses for business
     */
    suspend fun getTotalExpenses(businessId: String): Double
    
    /**
     * Gets expenses count for business
     */
    suspend fun getExpenseCount(businessId: String): Int
    
    /**
     * Gets amount by category
     */
    suspend fun getAmountByCategory(category: ExpenseCategory, businessId: String): Double
    
    /**
     * Adds new expense
     */
    suspend fun addExpense(expense: Expense): Result<String>
    
    /**
     * Updates existing expense
     */
    suspend fun updateExpense(expense: Expense): Result<Expense>
    
    /**
     * Deletes expense
     */
    suspend fun deleteExpense(id: String, businessId: String): Result<Unit>
    
    /**
     * Searches expenses by description
     */
    fun searchExpenses(query: String, businessId: String): Flow<List<Expense>>
    
    /**
     * Syncs expenses with Firestore
     */
    suspend fun syncWithFirestore(businessId: String): Result<Unit>
    
    /**
     * Performs comprehensive sync (both directions)
     */
    suspend fun performComprehensiveSync(businessId: String): Result<Unit>
} 