package com.borayildirim.beautydate.data.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import com.borayildirim.beautydate.data.local.entities.ExpenseEntity

/**
 * Expense DAO for database operations
 * Follows exact same pattern as other DAOs
 * Memory efficient: Flow-based reactive queries
 */
@Dao
interface ExpenseDao {
    
    /**
     * Gets all expenses for a business
     * Memory efficient: Flow for reactive updates
     */
    @Query("SELECT * FROM expenses WHERE businessId = :businessId AND isDeleted = 0 ORDER BY expenseDate DESC, createdAt DESC")
    fun getAllExpenses(businessId: String): Flow<List<ExpenseEntity>>
    
    /**
     * Gets expenses by category
     */
    @Query("SELECT * FROM expenses WHERE category = :category AND businessId = :businessId AND isDeleted = 0 ORDER BY expenseDate DESC")
    fun getExpensesByCategory(category: String, businessId: String): Flow<List<ExpenseEntity>>
    
    /**
     * Gets expense by ID
     */
    @Query("SELECT * FROM expenses WHERE id = :id AND businessId = :businessId AND isDeleted = 0")
    suspend fun getExpenseById(id: String, businessId: String): ExpenseEntity?
    
    /**
     * Gets total expenses for business
     */
    @Query("SELECT COALESCE(SUM(amount), 0) FROM expenses WHERE businessId = :businessId AND isDeleted = 0")
    suspend fun getTotalExpenses(businessId: String): Double
    
    /**
     * Gets expenses count for business
     */
    @Query("SELECT COUNT(*) FROM expenses WHERE businessId = :businessId AND isDeleted = 0")
    suspend fun getExpenseCount(businessId: String): Int
    
    /**
     * Gets amount by category
     */
    @Query("SELECT COALESCE(SUM(amount), 0) FROM expenses WHERE category = :category AND businessId = :businessId AND isDeleted = 0")
    suspend fun getAmountByCategory(category: String, businessId: String): Double
    
    /**
     * Gets expenses that need sync
     */
    @Query("SELECT * FROM expenses WHERE needsSync = 1 AND businessId = :businessId")
    suspend fun getExpensesNeedingSync(businessId: String): List<ExpenseEntity>
    
    /**
     * Inserts an expense
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity)
    
    /**
     * Inserts multiple expenses
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpenses(expenses: List<ExpenseEntity>)
    
    /**
     * Updates an expense
     */
    @Update
    suspend fun updateExpense(expense: ExpenseEntity)
    
    /**
     * Marks expense as synced
     */
    @Query("UPDATE expenses SET needsSync = 0 WHERE id = :id")
    suspend fun markAsSynced(id: String)
    
    /**
     * Marks expense as deleted (soft delete)
     */
    @Query("UPDATE expenses SET isDeleted = 1, needsSync = 1 WHERE id = :id AND businessId = :businessId")
    suspend fun markAsDeleted(id: String, businessId: String)
    
    /**
     * Hard deletes an expense
     */
    @Query("DELETE FROM expenses WHERE id = :id AND businessId = :businessId")
    suspend fun hardDeleteExpense(id: String, businessId: String)
    
    /**
     * Searches expenses by description
     */
    @Query("""
        SELECT * FROM expenses 
        WHERE businessId = :businessId 
        AND isDeleted = 0
        AND (description LIKE '%' || :query || '%' OR notes LIKE '%' || :query || '%' OR subcategory LIKE '%' || :query || '%')
        ORDER BY expenseDate DESC
    """)
    fun searchExpenses(query: String, businessId: String): Flow<List<ExpenseEntity>>
} 