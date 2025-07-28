package com.borayildirim.beautydate.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * Room DAO for Service entity operations
 * Provides all database operations with Flow support for reactive UI
 * Memory efficient: Flow-based reactive queries
 */
@Dao
interface ServiceDao {
    
    /**
     * Gets all services for a business as Flow (active only)
     * Memory efficient: Flow for reactive UI updates
     */
    @Query("SELECT * FROM services WHERE businessId = :businessId AND isDeleted = 0 AND isActive = 1 ORDER BY name ASC")
    fun getAllServices(businessId: String): Flow<List<ServiceEntity>>
    
    /**
     * Gets all services for a business as List (for sync operations)
     * Includes inactive services for comprehensive sync
     */
    @Query("SELECT * FROM services WHERE businessId = :businessId AND isDeleted = 0 ORDER BY name ASC")
    suspend fun getAllServicesSync(businessId: String): List<ServiceEntity>
    
    /**
     * Gets services by category
     * Memory efficient: indexed category filtering
     */
    @Query("SELECT * FROM services WHERE businessId = :businessId AND category = :category AND isDeleted = 0 AND isActive = 1 ORDER BY name ASC")
    fun getServicesByCategory(businessId: String, category: String): Flow<List<ServiceEntity>>
    
    /**
     * Searches services by name or description
     * Memory efficient: LIKE query with proper indexing
     */
    @Query("""
        SELECT * FROM services 
        WHERE businessId = :businessId 
        AND isDeleted = 0 
        AND isActive = 1
        AND (name LIKE '%' || :searchQuery || '%' 
             OR description LIKE '%' || :searchQuery || '%')
        ORDER BY name ASC
    """)
    fun searchServices(businessId: String, searchQuery: String): Flow<List<ServiceEntity>>
    
    /**
     * Gets a specific service by ID
     */
    @Query("SELECT * FROM services WHERE id = :serviceId AND isDeleted = 0")
    suspend fun getServiceById(serviceId: String): ServiceEntity?
    
    /**
     * Inserts a new service
     * Memory efficient: single insert operation
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertService(service: ServiceEntity)
    
    /**
     * Inserts multiple services (for sync operations)
     * Memory efficient: batch insert
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertServices(services: List<ServiceEntity>)
    
    /**
     * Updates an existing service
     */
    @Update
    suspend fun updateService(service: ServiceEntity)
    
    /**
     * Updates service price by ID
     * Memory efficient: direct field update without object loading
     */
    @Query("UPDATE services SET price = :newPrice, updatedAt = :timestamp, needsSync = 1 WHERE id = :serviceId")
    suspend fun updateServicePrice(serviceId: String, newPrice: Double, timestamp: Long = System.currentTimeMillis())
    
    /**
     * Bulk update prices for specific category
     * Memory efficient: single SQL operation for bulk update
     */
    @Query("UPDATE services SET price = price * :multiplier, updatedAt = :timestamp, needsSync = 1 WHERE businessId = :businessId AND category = :category AND isDeleted = 0")
    suspend fun updatePricesByCategory(businessId: String, category: String, multiplier: Double, timestamp: Long = System.currentTimeMillis())
    
    /**
     * Bulk update all prices for a business
     * Memory efficient: single SQL operation for business-wide update
     */
    @Query("UPDATE services SET price = price * :multiplier, updatedAt = :timestamp, needsSync = 1 WHERE businessId = :businessId AND isDeleted = 0")
    suspend fun updateAllPrices(businessId: String, multiplier: Double, timestamp: Long = System.currentTimeMillis())
    
    /**
     * Soft delete a service (mark as deleted)
     */
    @Query("UPDATE services SET isDeleted = 1, updatedAt = :timestamp WHERE id = :serviceId")
    suspend fun deleteService(serviceId: String, timestamp: Long = System.currentTimeMillis())
    
    /**
     * Toggle service active status
     * Memory efficient: direct boolean toggle
     */
    @Query("UPDATE services SET isActive = :isActive, updatedAt = :timestamp, needsSync = 1 WHERE id = :serviceId")
    suspend fun toggleServiceStatus(serviceId: String, isActive: Boolean, timestamp: Long = System.currentTimeMillis())
    
    /**
     * Gets total service count for a business (active only)
     */
    @Query("SELECT COUNT(*) FROM services WHERE businessId = :businessId AND isDeleted = 0 AND isActive = 1")
    suspend fun getServiceCount(businessId: String): Int
    
    /**
     * Gets service count by category
     * Memory efficient: count aggregation
     */
    @Query("SELECT COUNT(*) FROM services WHERE businessId = :businessId AND category = :category AND isDeleted = 0 AND isActive = 1")
    suspend fun getServiceCountByCategory(businessId: String, category: String): Int
    
    /**
     * Checks if a service name already exists for a business
     * Business logic: prevent duplicate service names
     */
    @Query("""
        SELECT COUNT(*) > 0 FROM services 
        WHERE businessId = :businessId 
        AND name = :serviceName 
        AND isDeleted = 0 
        AND id != :excludeServiceId
    """)
    suspend fun serviceNameExists(businessId: String, serviceName: String, excludeServiceId: String = ""): Boolean
    
    /**
     * Gets price range for a business
     * Memory efficient: aggregation query
     */
    @Query("SELECT MIN(price) FROM services WHERE businessId = :businessId AND isDeleted = 0 AND isActive = 1")
    suspend fun getMinPrice(businessId: String): Double?
    
    @Query("SELECT MAX(price) FROM services WHERE businessId = :businessId AND isDeleted = 0 AND isActive = 1")
    suspend fun getMaxPrice(businessId: String): Double?
    
    /**
     * Clears all services for a business (for fresh sync)
     */
    @Query("DELETE FROM services WHERE businessId = :businessId")
    suspend fun clearAllServices(businessId: String)
    
    /**
     * Marks service as deleted (soft delete)
     */
    @Query("UPDATE services SET isDeleted = 1, updatedAt = :timestamp WHERE id = :serviceId")
    suspend fun markAsDeleted(serviceId: String, timestamp: Long = System.currentTimeMillis())
    
    /**
     * Hard deletes service from database
     */
    @Query("DELETE FROM services WHERE id = :serviceId")
    suspend fun hardDeleteService(serviceId: String)
    
    /**
     * Marks service as synced with Firestore
     */
    @Query("UPDATE services SET needsSync = 0 WHERE id = :serviceId")
    suspend fun markAsSynced(serviceId: String)
    
    /**
     * Gets services that need to be synced with Firestore
     */
    @Query("SELECT * FROM services WHERE businessId = :businessId AND needsSync = 1")
    suspend fun getServicesNeedingSync(businessId: String): List<ServiceEntity>
    
    /**
     * Marks category services as needing sync
     */
    @Query("UPDATE services SET needsSync = 1, updatedAt = :timestamp WHERE businessId = :businessId AND category = :category")
    suspend fun markCategoryAsNeedingSync(businessId: String, category: String?, timestamp: Long = System.currentTimeMillis())
    
    // Price bulk update methods with proper sync and timestamp updates
    @Query("UPDATE services SET price = price * (1 + :percentage/100), updatedAt = :timestamp, needsSync = 1 WHERE businessId = :businessId AND category = :category AND isDeleted = 0")
    suspend fun increasePricesByCategory(businessId: String, category: String, percentage: Double, timestamp: Long = System.currentTimeMillis())
    
    @Query("UPDATE services SET price = price * (1 + :percentage/100), updatedAt = :timestamp, needsSync = 1 WHERE businessId = :businessId AND id IN (:serviceIds) AND isDeleted = 0")
    suspend fun increasePricesByIds(businessId: String, serviceIds: List<String>, percentage: Double, timestamp: Long = System.currentTimeMillis())
    
    @Query("UPDATE services SET price = price * (1 + :percentage/100), updatedAt = :timestamp, needsSync = 1 WHERE businessId = :businessId AND isDeleted = 0")
    suspend fun increaseAllPrices(businessId: String, percentage: Double, timestamp: Long = System.currentTimeMillis())
    
    @Query("UPDATE services SET price = price * (1 - :percentage/100), updatedAt = :timestamp, needsSync = 1 WHERE businessId = :businessId AND category = :category AND isDeleted = 0")
    suspend fun decreasePricesByCategory(businessId: String, category: String, percentage: Double, timestamp: Long = System.currentTimeMillis())
    
    @Query("UPDATE services SET price = price * (1 - :percentage/100), updatedAt = :timestamp, needsSync = 1 WHERE businessId = :businessId AND id IN (:serviceIds) AND isDeleted = 0")
    suspend fun decreasePricesByIds(businessId: String, serviceIds: List<String>, percentage: Double, timestamp: Long = System.currentTimeMillis())
    
    @Query("UPDATE services SET price = price * (1 - :percentage/100), updatedAt = :timestamp, needsSync = 1 WHERE businessId = :businessId AND isDeleted = 0")
    suspend fun decreaseAllPrices(businessId: String, percentage: Double, timestamp: Long = System.currentTimeMillis())
    
    @Query("UPDATE services SET price = price + :amount, updatedAt = :timestamp, needsSync = 1 WHERE businessId = :businessId AND category = :category AND isDeleted = 0")
    suspend fun addFixedAmountByCategory(businessId: String, category: String, amount: Double, timestamp: Long = System.currentTimeMillis())
    
    @Query("UPDATE services SET price = price + :amount, updatedAt = :timestamp, needsSync = 1 WHERE businessId = :businessId AND id IN (:serviceIds) AND isDeleted = 0")
    suspend fun addFixedAmountByIds(businessId: String, serviceIds: List<String>, amount: Double, timestamp: Long = System.currentTimeMillis())
    
    @Query("UPDATE services SET price = price + :amount, updatedAt = :timestamp, needsSync = 1 WHERE businessId = :businessId AND isDeleted = 0")
    suspend fun addFixedAmountToAll(businessId: String, amount: Double, timestamp: Long = System.currentTimeMillis())
    
    @Query("UPDATE services SET price = price - :amount, updatedAt = :timestamp, needsSync = 1 WHERE businessId = :businessId AND category = :category AND isDeleted = 0")
    suspend fun subtractFixedAmountByCategory(businessId: String, category: String, amount: Double, timestamp: Long = System.currentTimeMillis())
    
    @Query("UPDATE services SET price = price - :amount, updatedAt = :timestamp, needsSync = 1 WHERE businessId = :businessId AND id IN (:serviceIds) AND isDeleted = 0")
    suspend fun subtractFixedAmountByIds(businessId: String, serviceIds: List<String>, amount: Double, timestamp: Long = System.currentTimeMillis())
    
    @Query("UPDATE services SET price = price - :amount, updatedAt = :timestamp, needsSync = 1 WHERE businessId = :businessId AND isDeleted = 0")
    suspend fun subtractFixedAmountFromAll(businessId: String, amount: Double, timestamp: Long = System.currentTimeMillis())
    
    // Set exact price methods
    @Query("UPDATE services SET price = :price, updatedAt = :timestamp, needsSync = 1 WHERE businessId = :businessId AND category = :category AND isDeleted = 0")
    suspend fun setExactPriceByCategory(businessId: String, category: String, price: Double, timestamp: Long = System.currentTimeMillis())
    
    @Query("UPDATE services SET price = :price, updatedAt = :timestamp, needsSync = 1 WHERE businessId = :businessId AND id IN (:serviceIds) AND isDeleted = 0")
    suspend fun setExactPriceByIds(businessId: String, serviceIds: List<String>, price: Double, timestamp: Long = System.currentTimeMillis())
    
    @Query("UPDATE services SET price = :price, updatedAt = :timestamp, needsSync = 1 WHERE businessId = :businessId AND isDeleted = 0")
    suspend fun setExactPriceForAll(businessId: String, price: Double, timestamp: Long = System.currentTimeMillis())
    
    /**
     * Rounds all service prices to nearest 50 for a business
     * Logic: ROUND(price / 50) * 50 - divides by 50, rounds to nearest integer, multiplies by 50
     * Examples: 137→150, 690→700, 976→1000
     */
    @Query("UPDATE services SET price = ROUND(price / 50.0) * 50.0, updatedAt = :timestamp, needsSync = 1 WHERE businessId = :businessId AND isDeleted = 0")
    suspend fun roundAllPricesToNearest50(businessId: String, timestamp: Long = System.currentTimeMillis())
    
    /**
     * Rounds service prices to nearest 50 for specific category
     * Logic: ROUND(price / 50) * 50 - divides by 50, rounds to nearest integer, multiplies by 50
     */
    @Query("UPDATE services SET price = ROUND(price / 50.0) * 50.0, updatedAt = :timestamp, needsSync = 1 WHERE businessId = :businessId AND category = :category AND isDeleted = 0")
    suspend fun roundPricesToNearest50ByCategory(businessId: String, category: String, timestamp: Long = System.currentTimeMillis())
    
    /**
     * Rounds service prices to nearest 50 for specific service IDs
     * Logic: ROUND(price / 50) * 50 - divides by 50, rounds to nearest integer, multiplies by 50
     */
    @Query("UPDATE services SET price = ROUND(price / 50.0) * 50.0, updatedAt = :timestamp, needsSync = 1 WHERE businessId = :businessId AND id IN (:serviceIds) AND isDeleted = 0")
    suspend fun roundPricesToNearest50ByIds(businessId: String, serviceIds: List<String>, timestamp: Long = System.currentTimeMillis())
} 