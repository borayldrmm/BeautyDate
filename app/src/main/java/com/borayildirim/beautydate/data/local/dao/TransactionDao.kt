package com.borayildirim.beautydate.data.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import com.borayildirim.beautydate.data.local.entities.TransactionEntity

/**
 * Transaction DAO for database operations
 * Follows exact same pattern as CustomerDao
 * Memory efficient: Flow-based reactive queries
 */
@Dao
interface TransactionDao {
    
    /**
     * Gets all transactions for a business
     * Memory efficient: Flow for reactive updates
     */
    @Query("SELECT * FROM transactions WHERE businessId = :businessId ORDER BY createdAt DESC")
    fun getAllTransactions(businessId: String): Flow<List<TransactionEntity>>
    
    /**
     * Gets transactions by type
     */
    @Query("SELECT * FROM transactions WHERE type = :type AND businessId = :businessId ORDER BY createdAt DESC")
    fun getTransactionsByType(type: String, businessId: String): Flow<List<TransactionEntity>>
    
    /**
     * Gets transactions by category
     */
    @Query("SELECT * FROM transactions WHERE category = :category AND businessId = :businessId ORDER BY createdAt DESC")
    fun getTransactionsByCategory(category: String, businessId: String): Flow<List<TransactionEntity>>
    
    /**
     * Gets transactions by payment
     */
    @Query("SELECT * FROM transactions WHERE paymentId = :paymentId AND businessId = :businessId")
    fun getTransactionsByPayment(paymentId: String, businessId: String): Flow<List<TransactionEntity>>
    
    /**
     * Gets transaction by ID
     */
    @Query("SELECT * FROM transactions WHERE id = :id AND businessId = :businessId")
    suspend fun getTransactionById(id: String, businessId: String): TransactionEntity?
    
    /**
     * Gets total income for business
     */
    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE type = 'INCOME' AND businessId = :businessId")
    suspend fun getTotalIncome(businessId: String): Double
    
    /**
     * Gets total expenses for business
     */
    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE type = 'EXPENSE' AND businessId = :businessId")
    suspend fun getTotalExpenses(businessId: String): Double
    
    /**
     * Gets amount by category
     */
    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE category = :category AND businessId = :businessId")
    suspend fun getAmountByCategory(category: String, businessId: String): Double
    
    /**
     * Gets transaction count by type
     */
    @Query("SELECT COUNT(*) FROM transactions WHERE type = :type AND businessId = :businessId")
    suspend fun getTransactionCountByType(type: String, businessId: String): Int
    
    /**
     * Inserts a transaction
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)
    
    /**
     * Inserts multiple transactions
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<TransactionEntity>)
    
    /**
     * Updates a transaction
     */
    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)
    
    /**
     * Deletes a transaction by ID
     */
    @Query("DELETE FROM transactions WHERE id = :id AND businessId = :businessId")
    suspend fun deleteTransaction(id: String, businessId: String)
    
    /**
     * Searches transactions by description
     */
    @Query("""
        SELECT * FROM transactions 
        WHERE businessId = :businessId 
        AND (description LIKE :query OR reference LIKE :query)
        ORDER BY createdAt DESC
    """)
    fun searchTransactions(query: String, businessId: String): Flow<List<TransactionEntity>>
} 