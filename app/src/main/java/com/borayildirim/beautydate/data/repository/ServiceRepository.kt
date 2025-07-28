package com.borayildirim.beautydate.data.repository

import com.borayildirim.beautydate.data.models.Service
import com.borayildirim.beautydate.data.models.ServiceCategory
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for service management
 * Follows Repository pattern and Interface Segregation Principle
 * Memory efficient: uses Flow for reactive data and Result for error handling
 */
interface ServiceRepository {
    
    /**
     * Gets all services for a business as Flow (offline-first)
     */
    fun getAllServices(businessId: String): Flow<List<Service>>
    
    /**
     * Gets all services for a business as Flow (alias for getAllServices)
     */
    fun getServicesByBusinessId(businessId: String): Flow<List<Service>>
    
    /**
     * Gets services by category
     */
    fun getServicesByCategory(businessId: String, category: ServiceCategory): Flow<List<Service>>
    
    /**
     * Searches services by query (name or description)
     */
    fun searchServices(businessId: String, query: String): Flow<List<Service>>
    
    /**
     * Gets a specific service by ID
     */
    suspend fun getServiceById(serviceId: String): Service?
    
    /**
     * Adds a new service (local + sync)
     */
    suspend fun addService(service: Service): Result<Service>
    
    /**
     * Updates an existing service (local + sync)
     */
    suspend fun updateService(service: Service): Result<Service>
    
    /**
     * Deletes a service (soft delete + sync)
     */
    suspend fun deleteService(serviceId: String): Result<Unit>
    
    /**
     * Bulk update service prices with strategy
     */
    suspend fun bulkUpdatePrices(
        businessId: String,
        updateType: PriceUpdateType,
        value: Double,
        category: ServiceCategory? = null,
        serviceIds: List<String> = emptyList()
    ): Result<Unit>
    
    /**
     * Checks if a service name already exists for a business
     * @param businessId Business ID to check in
     * @param serviceName Service name to check
     * @param excludeServiceId Service ID to exclude (for updates)
     * @return True if service name exists
     */
    suspend fun serviceNameExists(businessId: String, serviceName: String, excludeServiceId: String = ""): Boolean
    
    /**
     * Gets total service count for a business
     * @param businessId Business ID to get count for
     * @return Total service count
     */
    suspend fun getServiceCount(businessId: String): Int
    
    /**
     * Manually syncs services with Firestore
     */
    suspend fun syncWithFirestore(businessId: String): Result<Unit>
    
    /**
     * Performs initial sync when app starts
     */
    suspend fun performInitialSync(businessId: String): Result<Unit>
}

/**
 * Price update strategies for bulk operations
 * Memory efficient: enum for type safety
 */
enum class PriceUpdateType {
    PERCENTAGE_INCREASE,  // Percentage increase
    PERCENTAGE_DECREASE,  // Percentage decrease
    FIXED_AMOUNT_ADD,     // Add fixed amount
    FIXED_AMOUNT_SUBTRACT,// Subtract fixed amount
    SET_EXACT_PRICE,      // Set exact price
    ROUND_PRICES          // Round prices to nearest 50
} 