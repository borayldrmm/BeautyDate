package com.borayildirim.beautydate.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * Room DAO for Customer entity operations
 * Provides all database operations with Flow support for reactive UI
 */
@Dao
interface CustomerDao {
    
    /**
     * Gets all customers for a business as Flow
     * @param businessId Business ID to filter customers
     * @return Flow of customer list for reactive UI updates
     */
    @Query("SELECT * FROM customers WHERE businessId = :businessId AND isDeleted = 0 ORDER BY firstName ASC")
    fun getAllCustomers(businessId: String): Flow<List<CustomerEntity>>
    
    /**
     * Gets all customers for a business as List (for sync operations)
     * @param businessId Business ID to filter customers
     * @return List of customer entities
     */
    @Query("SELECT * FROM customers WHERE businessId = :businessId AND isDeleted = 0 ORDER BY firstName ASC")
    suspend fun getAllCustomersSync(businessId: String): List<CustomerEntity>
    
    /**
     * Searches customers by name or phone number
     * @param businessId Business ID to filter customers  
     * @param searchQuery Search query for name or phone
     * @return Flow of filtered customer list
     */
    @Query("""
        SELECT * FROM customers 
        WHERE businessId = :businessId 
        AND isDeleted = 0 
        AND (firstName LIKE '%' || :searchQuery || '%' 
             OR lastName LIKE '%' || :searchQuery || '%'
             OR phoneNumber LIKE '%' || :searchQuery || '%')
        ORDER BY firstName ASC
    """)
    fun searchCustomers(businessId: String, searchQuery: String): Flow<List<CustomerEntity>>
    
    /**
     * Gets a specific customer by ID
     * @param customerId Customer ID
     * @return Customer entity or null
     */
    @Query("SELECT * FROM customers WHERE id = :customerId AND isDeleted = 0")
    suspend fun getCustomerById(customerId: String): CustomerEntity?
    
    /**
     * Gets customers that need sync with Firestore
     * @param businessId Business ID to filter customers
     * @return List of customers needing sync
     */
    @Query("SELECT * FROM customers WHERE businessId = :businessId AND needsSync = 1")
    suspend fun getCustomersNeedingSync(businessId: String): List<CustomerEntity>
    
    /**
     * Inserts a new customer
     * @param customer Customer entity to insert
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: CustomerEntity)
    
    /**
     * Inserts multiple customers (for sync operations)
     * @param customers List of customer entities
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomers(customers: List<CustomerEntity>)
    
    /**
     * Updates an existing customer
     * @param customer Customer entity to update
     */
    @Update
    suspend fun updateCustomer(customer: CustomerEntity)
    
    /**
     * Soft delete a customer (mark as deleted)
     * @param customerId Customer ID to delete
     */
    @Query("UPDATE customers SET isDeleted = 1, needsSync = 1, updatedAt = :timestamp WHERE id = :customerId")
    suspend fun deleteCustomer(customerId: String, timestamp: Long = System.currentTimeMillis())
    
    /**
     * Hard delete a customer from local database
     * @param customerId Customer ID to permanently delete
     */
    @Query("DELETE FROM customers WHERE id = :customerId")
    suspend fun hardDeleteCustomer(customerId: String)
    
    /**
     * Marks customer as synced with Firestore
     * @param customerId Customer ID
     */
    @Query("UPDATE customers SET needsSync = 0 WHERE id = :customerId")
    suspend fun markAsSynced(customerId: String)
    
    /**
     * Marks customer as needing sync
     * @param customerId Customer ID
     */
    @Query("UPDATE customers SET needsSync = 1, updatedAt = :timestamp WHERE id = :customerId")
    suspend fun markAsNeedsSync(customerId: String, timestamp: Long = System.currentTimeMillis())
    
    /**
     * Gets total customer count for a business
     * @param businessId Business ID
     * @return Total customer count
     */
    @Query("SELECT COUNT(*) FROM customers WHERE businessId = :businessId AND isDeleted = 0")
    suspend fun getCustomerCount(businessId: String): Int
    
    /**
     * Checks if a phone number already exists for a business
     * @param businessId Business ID
     * @param phoneNumber Phone number to check
     * @param excludeCustomerId Customer ID to exclude (for updates)
     * @return True if phone number exists
     */
    @Query("""
        SELECT COUNT(*) > 0 FROM customers 
        WHERE businessId = :businessId 
        AND phoneNumber = :phoneNumber 
        AND isDeleted = 0 
        AND id != :excludeCustomerId
    """)
    suspend fun phoneNumberExists(businessId: String, phoneNumber: String, excludeCustomerId: String = ""): Boolean
    
    /**
     * Clears all customers for a business (for fresh sync)
     * @param businessId Business ID
     */
    @Query("DELETE FROM customers WHERE businessId = :businessId")
    suspend fun clearAllCustomers(businessId: String)
    
    /**
     * Marks customer as deleted (soft delete)
     * @param customerId Customer ID to mark as deleted
     */
    @Query("UPDATE customers SET isDeleted = 1, updatedAt = :timestamp WHERE id = :customerId")
    suspend fun markAsDeleted(customerId: String, timestamp: Long = System.currentTimeMillis())
} 