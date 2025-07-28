package com.borayildirim.beautydate.data.repository

import com.borayildirim.beautydate.data.local.ServiceDao
import com.borayildirim.beautydate.data.local.ServiceEntity
import com.borayildirim.beautydate.data.models.Service
import com.borayildirim.beautydate.data.models.ServiceCategory
import com.borayildirim.beautydate.data.models.ServiceFirestore
import com.borayildirim.beautydate.utils.NetworkMonitor
import com.borayildirim.beautydate.utils.AuthUtil
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of ServiceRepository with offline-first approach
 * Uses Room for local storage and Firestore for remote sync
 * Multi-tenant architecture: All operations filtered by authenticated businessId
 * Memory efficient: Flow-based reactive data and minimal object creation
 */
@Singleton
class ServiceRepositoryImpl @Inject constructor(
    private val serviceDao: ServiceDao,
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth,
    private val networkMonitor: NetworkMonitor,
    private val authUtil: AuthUtil
) : ServiceRepository {
    
    companion object {
        private const val SERVICES_COLLECTION = "services"
    }
    
    /**
     * Gets all services as Flow from local database (offline-first)
     * BusinessId filtering applied automatically from authenticated user
     * Memory efficient: Flow mapping with lazy evaluation
     */
    override fun getAllServices(businessId: String): Flow<List<Service>> {
        // Use authenticated business ID for security
        val currentBusinessId = authUtil.getCurrentBusinessIdSafe()
        return serviceDao.getAllServices(currentBusinessId)
            .map { entities -> entities.map { it.toDomainModel() } }
    }
    
    /**
     * Gets all services for a business as Flow (alias for getAllServices)
     * BusinessId filtering applied automatically
     */
    override fun getServicesByBusinessId(businessId: String): Flow<List<Service>> {
        return getAllServices(businessId) // businessId parameter ignored for security
    }
    
    /**
     * Gets services by category from local database
     * BusinessId filtering applied automatically
     * Memory efficient: indexed database query
     */
    override fun getServicesByCategory(businessId: String, category: ServiceCategory): Flow<List<Service>> {
        val currentBusinessId = authUtil.getCurrentBusinessIdSafe()
        return serviceDao.getServicesByCategory(currentBusinessId, category.name)
            .map { entities -> entities.map { it.toDomainModel() } }
    }
    
    /**
     * Searches services by query from local database
     * BusinessId filtering applied automatically
     * Memory efficient: SQL LIKE query with proper indexing
     */
    override fun searchServices(businessId: String, query: String): Flow<List<Service>> {
        val currentBusinessId = authUtil.getCurrentBusinessIdSafe()
        return if (query.isBlank()) {
            getAllServices(businessId)
        } else {
            serviceDao.searchServices(currentBusinessId, query)
                .map { entities -> entities.map { it.toDomainModel() } }
        }
    }
    
    /**
     * Gets service by ID from local database with businessId validation
     * Ensures service belongs to current authenticated business
     * Memory efficient: single object retrieval
     */
    override suspend fun getServiceById(serviceId: String): Service? {
        val businessId = authUtil.getCurrentBusinessId() ?: return null
        val service = serviceDao.getServiceById(serviceId)?.toDomainModel()
        
        // Validate that service belongs to current business
        return if (service?.businessId == businessId) {
            service
        } else {
            null // Service doesn't belong to this business
        }
    }
    
    /**
     * Adds new service to local database with automatic businessId assignment
     * BusinessId is automatically set from current authenticated user
     * Memory efficient: immediate local insert with background sync
     */
    override suspend fun addService(service: Service): Result<Service> {
        return try {
            val businessId = authUtil.getCurrentBusinessId()
                ?: return Result.failure(Exception(authUtil.getAuthErrorMessage()))
            
            // Automatically set businessId for new service
            val newService = service.copy(
                id = if (service.id.isEmpty()) Service.generateServiceId() else service.id,
                businessId = businessId, // Automatically set from authenticated user
                createdAt = Timestamp.now(),
                updatedAt = Timestamp.now()
            )
            
            val entity = ServiceEntity.fromDomainModel(newService, needsSync = true)
            serviceDao.insertService(entity)
            
            // Automatic background sync if network available
            if (networkMonitor.isCurrentlyConnected()) {
                try {
                    syncServiceToFirestore(newService)
                    serviceDao.markAsSynced(newService.id)
                } catch (syncError: Exception) {
                    // Don't fail the operation - will sync when network is available
                }
            }
            
            Result.success(newService)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Updates existing service with businessId validation
     * Ensures service belongs to current authenticated business
     * Memory efficient: targeted update with minimal object creation
     */
    override suspend fun updateService(service: Service): Result<Service> {
        return try {
            val businessId = authUtil.getCurrentBusinessId()
                ?: return Result.failure(Exception(authUtil.getAuthErrorMessage()))
            
            // Validate that service belongs to current business
            if (service.businessId != businessId) {
                return Result.failure(Exception(authUtil.getTenantErrorMessage()))
            }
            
            val updatedService = service.copy(updatedAt = Timestamp.now())
            val entity = ServiceEntity.fromDomainModel(updatedService, needsSync = true)
            
            serviceDao.updateService(entity)
            
            // Automatic background sync if network available
            if (networkMonitor.isCurrentlyConnected()) {
                try {
                    syncServiceToFirestore(updatedService)
                    serviceDao.markAsSynced(updatedService.id)
                } catch (syncError: Exception) {
                    // Will sync later
                }
            }
            
            Result.success(updatedService)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Deletes service with businessId validation
     * Ensures service belongs to current authenticated business
     * Memory efficient: soft delete with cleanup
     */
    override suspend fun deleteService(serviceId: String): Result<Unit> {
        return try {
            val businessId = authUtil.getCurrentBusinessId()
                ?: return Result.failure(Exception(authUtil.getAuthErrorMessage()))
            
            // Get service to validate businessId
            val service = serviceDao.getServiceById(serviceId)?.toDomainModel()
            if (service?.businessId != businessId) {
                return Result.failure(Exception(authUtil.getTenantErrorMessage()))
            }
            
            // Always hard delete locally first for immediate UI consistency
            serviceDao.hardDeleteService(serviceId)
            
            // Try immediate sync to Firestore
            if (networkMonitor.isCurrentlyConnected()) {
                try {
                    firestore.collection(SERVICES_COLLECTION)
                        .document(serviceId)
                        .delete()
                        .await()
                    
                } catch (syncError: Exception) {
                    // Local delete already succeeded, sync will happen later if needed
                }
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Bulk updates service prices with business validation
     * BusinessId filtering applied automatically
     */
    override suspend fun bulkUpdatePrices(
        businessId: String,
        updateType: PriceUpdateType,
        value: Double,
        category: ServiceCategory?,
        serviceIds: List<String>
    ): Result<Unit> {
        return try {
            val currentBusinessId = authUtil.getCurrentBusinessId()
                ?: return Result.failure(Exception(authUtil.getAuthErrorMessage()))
            
            // Perform bulk update with businessId filtering
            val timestamp = System.currentTimeMillis()
            
            when (updateType) {
                PriceUpdateType.PERCENTAGE_INCREASE -> {
                    if (category != null) {
                        serviceDao.increasePricesByCategory(currentBusinessId, category.name, value, timestamp)
                    } else if (serviceIds.isNotEmpty()) {
                        serviceDao.increasePricesByIds(currentBusinessId, serviceIds, value, timestamp)
                    } else {
                        serviceDao.increaseAllPrices(currentBusinessId, value, timestamp)
                    }
                }
                PriceUpdateType.PERCENTAGE_DECREASE -> {
                    if (category != null) {
                        serviceDao.decreasePricesByCategory(currentBusinessId, category.name, value, timestamp)
                    } else if (serviceIds.isNotEmpty()) {
                        serviceDao.decreasePricesByIds(currentBusinessId, serviceIds, value, timestamp)
                    } else {
                        serviceDao.decreaseAllPrices(currentBusinessId, value, timestamp)
                    }
                }
                PriceUpdateType.FIXED_AMOUNT_ADD -> {
                    if (category != null) {
                        serviceDao.addFixedAmountByCategory(currentBusinessId, category.name, value, timestamp)
                    } else if (serviceIds.isNotEmpty()) {
                        serviceDao.addFixedAmountByIds(currentBusinessId, serviceIds, value, timestamp)
                    } else {
                        serviceDao.addFixedAmountToAll(currentBusinessId, value, timestamp)
                    }
                }
                PriceUpdateType.FIXED_AMOUNT_SUBTRACT -> {
                    if (category != null) {
                        serviceDao.subtractFixedAmountByCategory(currentBusinessId, category.name, value, timestamp)
                    } else if (serviceIds.isNotEmpty()) {
                        serviceDao.subtractFixedAmountByIds(currentBusinessId, serviceIds, value, timestamp)
                    } else {
                        serviceDao.subtractFixedAmountFromAll(currentBusinessId, value, timestamp)
                    }
                }
                PriceUpdateType.SET_EXACT_PRICE -> {
                    // Set exact price for selected services
                    if (category != null) {
                        serviceDao.setExactPriceByCategory(currentBusinessId, category.name, value, timestamp)
                    } else if (serviceIds.isNotEmpty()) {
                        serviceDao.setExactPriceByIds(currentBusinessId, serviceIds, value, timestamp)
                    } else {
                        serviceDao.setExactPriceForAll(currentBusinessId, value, timestamp)
                    }
                }
                PriceUpdateType.ROUND_PRICES -> {
                    // Implement price rounding to nearest 50 (524->500, 525->550, 575->600)
                    if (category != null) {
                        serviceDao.roundPricesToNearest50ByCategory(currentBusinessId, category.name, timestamp)
                    } else if (serviceIds.isNotEmpty()) {
                        serviceDao.roundPricesToNearest50ByIds(currentBusinessId, serviceIds, timestamp)
                    } else {
                        serviceDao.roundAllPricesToNearest50(currentBusinessId, timestamp)
                    }
                }
            }
            
            // Mark affected services as needing sync
            serviceDao.markCategoryAsNeedingSync(currentBusinessId, category?.name)
            
            // Trigger immediate sync if network available for cross-device consistency
            if (networkMonitor.isCurrentlyConnected()) {
                try {
                    syncWithFirestore(currentBusinessId)
                } catch (syncError: Exception) {
                    // Don't fail the operation - sync will happen later
                }
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Syncs services with Firestore for current business
     * BusinessId filtering applied automatically
     * Memory efficient: batch operations for sync
     */
    override suspend fun syncWithFirestore(businessId: String): Result<Unit> {
        return try {
            val currentBusinessId = authUtil.getCurrentBusinessId()
                ?: return Result.failure(Exception(authUtil.getAuthErrorMessage()))
            
            if (!networkMonitor.isCurrentlyConnected()) {
                return Result.failure(Exception("No network connection"))
            }
            
            // Get local services needing sync for current business
            val localServices = serviceDao.getServicesNeedingSync(currentBusinessId)
            
            // Upload local changes to Firestore
            localServices.forEach { entity ->
                val service = entity.toDomainModel()
                syncServiceToFirestore(service)
                serviceDao.markAsSynced(service.id)
            }
            
            // Download remote services for current business only
            
            // TEST 1: Try businessId only (no isActive filter)
            val testQuery1 = firestore.collection(SERVICES_COLLECTION)
                .whereEqualTo("businessId", currentBusinessId)
                .get()
                .await()
            
            // TEMPORARY FIX: Remove isActive filter to enable cross-device sync
            // Original compound query (commented out due to isActive field issue)
            /*
            val remoteServices = firestore.collection(SERVICES_COLLECTION)
                .whereEqualTo("businessId", currentBusinessId) // BusinessId filtering
                .whereEqualTo("isActive", true)
                .get()
                .await()
            */
            
            // WORKING QUERY: Only businessId filter (isActive filter removed temporarily)
            val remoteServices = firestore.collection(SERVICES_COLLECTION)
                .whereEqualTo("businessId", currentBusinessId) // BusinessId filtering only
                .get()
                .await()
            
            
            val remoteEntities = remoteServices.documents.mapNotNull { doc ->
                doc.toObject(ServiceFirestore::class.java)?.let { firestore ->
                    ServiceEntity.fromDomainModel(firestore.toDomainModel(), needsSync = false)
                }
            }
            
            // Save remote services to local database
            if (remoteEntities.isNotEmpty()) {
                serviceDao.insertServices(remoteEntities)
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Checks if a service name already exists for a business
     * BusinessId validation included for security
     */
    override suspend fun serviceNameExists(businessId: String, serviceName: String, excludeServiceId: String): Boolean {
        return try {
            val currentBusinessId = authUtil.getCurrentBusinessId()
                ?: return false
            
            // Validate businessId matches current authenticated business
            if (businessId != currentBusinessId) {
                return false
            }
            
            serviceDao.serviceNameExists(currentBusinessId, serviceName, excludeServiceId)
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Gets total service count for a business
     * BusinessId validation included for security
     */
    override suspend fun getServiceCount(businessId: String): Int {
        return try {
            val currentBusinessId = authUtil.getCurrentBusinessId()
                ?: return 0
            
            // Validate businessId matches current authenticated business
            if (businessId != currentBusinessId) {
                return 0
            }
            
            serviceDao.getServiceCount(currentBusinessId)
        } catch (e: Exception) {
            0
        }
    }

    /**
     * Performs initial sync when app starts for current business
     * Only downloads data belonging to current authenticated business
     */
    override suspend fun performInitialSync(businessId: String): Result<Unit> {
        return try {
            val currentBusinessId = authUtil.getCurrentBusinessId()
                ?: return Result.failure(Exception(authUtil.getAuthErrorMessage()))
            
            // Always perform fresh sync for cross-device data consistency
            // This ensures data is always up-to-date across different devices
            syncWithFirestore(businessId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Private helper: Syncs single service to Firestore with businessId
     * Used internally for upload operations
     */
    private suspend fun syncServiceToFirestore(service: Service) {
        val firestoreService = ServiceFirestore.fromDomainModel(
            service,
            lastModifiedBy = authUtil.getCurrentBusinessIdSafe()
        )
        
        firestore.collection(SERVICES_COLLECTION)
            .document(service.id)
            .set(firestoreService)
            .await()
    }
} 