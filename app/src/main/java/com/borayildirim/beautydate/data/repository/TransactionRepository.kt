package com.borayildirim.beautydate.data.repository

import kotlinx.coroutines.flow.Flow
import com.borayildirim.beautydate.data.models.Transaction
import com.borayildirim.beautydate.data.models.TransactionType
import com.borayildirim.beautydate.data.models.TransactionCategory

/**
 * Transaction repository interface
 * Follows exact same pattern as CustomerRepository
 * Handles transaction data operations with offline-first approach
 */
interface TransactionRepository {
    
    /**
     * Gets all transactions for a business
     * Returns Flow for reactive UI updates
     */
    fun getAllTransactions(businessId: String): Flow<List<Transaction>>
    
    /**
     * Gets transactions by type
     */
    fun getTransactionsByType(type: TransactionType, businessId: String): Flow<List<Transaction>>
    
    /**
     * Gets transactions by category
     */
    fun getTransactionsByCategory(category: TransactionCategory, businessId: String): Flow<List<Transaction>>
    
    /**
     * Gets transactions by payment
     */
    fun getTransactionsByPayment(paymentId: String, businessId: String): Flow<List<Transaction>>
    
    /**
     * Gets transaction by ID
     */
    suspend fun getTransactionById(id: String, businessId: String): Transaction?
    
    /**
     * Gets total income for business
     */
    suspend fun getTotalIncome(businessId: String): Double
    
    /**
     * Gets total expenses for business
     */
    suspend fun getTotalExpenses(businessId: String): Double
    
    /**
     * Gets amount by category
     */
    suspend fun getAmountByCategory(category: TransactionCategory, businessId: String): Double
    
    /**
     * Gets transaction count by type
     */
    suspend fun getTransactionCountByType(type: TransactionType, businessId: String): Int
    
    /**
     * Adds new transaction
     */
    suspend fun addTransaction(transaction: Transaction): Result<String>
    
    /**
     * Updates existing transaction
     */
    suspend fun updateTransaction(transaction: Transaction): Result<Transaction>
    
    /**
     * Deletes transaction
     */
    suspend fun deleteTransaction(id: String, businessId: String): Result<Unit>
    
    /**
     * Syncs transactions with Firestore
     */
    suspend fun syncTransactions(businessId: String): Result<Unit>
    
    /**
     * Searches transactions
     */
    fun searchTransactions(query: String, businessId: String): Flow<List<Transaction>>
} 