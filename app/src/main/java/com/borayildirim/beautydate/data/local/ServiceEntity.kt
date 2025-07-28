package com.borayildirim.beautydate.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.borayildirim.beautydate.data.models.Service
import com.borayildirim.beautydate.data.models.ServiceCategory
import com.borayildirim.beautydate.data.models.ServiceSubcategory
import com.google.firebase.Timestamp

/**
 * Room entity for local service storage
 * Includes sync management fields for offline-first approach
 * Memory efficient: primitive types for Room storage
 */
@Entity(tableName = "services")
data class ServiceEntity(
    @PrimaryKey 
    val id: String,
    val name: String,
    val price: Double,
    val category: String, // Store as String for Room
    val subcategory: String?, // Store as String for Room
    val description: String,
    val isActive: Boolean = true,
    val createdAt: Long, // Store as timestamp for Room
    val updatedAt: Long,
    val businessId: String,
    val isDeleted: Boolean = false,
    val needsSync: Boolean = false // For sync management
) {
    /**
     * Converts Room entity to domain model
     * Memory efficient: single object creation
     */
    fun toDomainModel(): Service {
        return Service(
            id = id,
            name = name,
            price = price,
            category = try {
                ServiceCategory.valueOf(category)
            } catch (e: Exception) {
                ServiceCategory.NAIL // Default fallback
            },
            subcategory = subcategory?.let { subcategoryName ->
                try {
                    ServiceSubcategory.getByDisplayName(subcategoryName)
                } catch (e: Exception) {
                    null
                }
            },
            description = description,
            isActive = isActive,
            createdAt = Timestamp(createdAt / 1000, ((createdAt % 1000) * 1000000).toInt()),
            updatedAt = Timestamp(updatedAt / 1000, ((updatedAt % 1000) * 1000000).toInt()),
            businessId = businessId
        )
    }
    
    companion object {
        /**
         * Converts domain model to Room entity
         * Memory efficient: direct field mapping
         */
        fun fromDomainModel(service: Service, needsSync: Boolean = false): ServiceEntity {
            return ServiceEntity(
                id = service.id,
                name = service.name,
                price = service.price,
                category = service.category.name,
                subcategory = service.subcategory?.displayName,
                description = service.description,
                isActive = service.isActive,
                createdAt = service.createdAt?.toDate()?.time ?: System.currentTimeMillis(),
                updatedAt = service.updatedAt?.toDate()?.time ?: System.currentTimeMillis(),
                businessId = service.businessId,
                needsSync = needsSync
            )
        }
        
        /**
         * Creates a new ServiceEntity with default values
         * Memory efficient: minimal object creation
         */
        fun createNew(
            name: String,
            price: Double,
            category: ServiceCategory,
            subcategory: ServiceSubcategory?,
            description: String,
            businessId: String
        ): ServiceEntity {
            val now = System.currentTimeMillis()
            return ServiceEntity(
                id = Service.generateServiceId(),
                name = name,
                price = price,
                category = category.name,
                subcategory = subcategory?.name,
                description = description,
                isActive = true,
                createdAt = now,
                updatedAt = now,
                businessId = businessId,
                needsSync = true // New entities need sync
            )
        }
    }
} 