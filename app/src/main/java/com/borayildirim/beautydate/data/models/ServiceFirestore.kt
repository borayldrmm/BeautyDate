package com.borayildirim.beautydate.data.models

import com.google.firebase.Timestamp

/**
 * Firestore-specific service model
 * Optimized for Firebase storage and retrieval
 * Separate from domain model to allow independent evolution
 * Memory efficient: primitive types for Firestore serialization
 */
data class ServiceFirestore(
    val id: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val category: String = "", // Store as String for Firestore
    val subcategory: String? = null, // Store as String for Firestore
    val description: String = "",
    val isActive: Boolean = true,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null,
    val businessId: String = "",
    // Firestore-specific metadata
    val syncVersion: Long = 1,
    val lastModifiedBy: String = "",
    val isDeleted: Boolean = false
) {
    companion object {
        /**
         * Converts domain Service model to Firestore model
         * Memory efficient: direct field mapping
         */
        fun fromDomainModel(service: Service, lastModifiedBy: String = ""): ServiceFirestore {
            return ServiceFirestore(
                id = service.id,
                name = service.name,
                price = service.price,
                category = service.category.name,
                subcategory = service.subcategory?.displayName,
                description = service.description,
                isActive = service.isActive,
                createdAt = service.createdAt,
                updatedAt = service.updatedAt,
                businessId = service.businessId,
                syncVersion = System.currentTimeMillis(),
                lastModifiedBy = lastModifiedBy,
                isDeleted = false
            )
        }
    }
    
    /**
     * Converts Firestore model to domain Service model
     * Memory efficient: single object creation with enum parsing
     */
    fun toDomainModel(): Service {
        return Service(
            id = id,
            name = name,
            price = price,
            category = try {
                ServiceCategory.valueOf(category)
            } catch (e: Exception) {
                ServiceCategory.NAIL // Default fallback for data integrity
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
            createdAt = createdAt,
            updatedAt = updatedAt,
            businessId = businessId
        )
    }
    
    /**
     * Returns formatted display name for Firestore queries
     * Memory efficient: cached property access
     */
    val displayName: String
        get() = "$name (${ServiceCategory.valueOf(category).getDisplayName()})"
    
    /**
     * Returns formatted price for Firestore display
     */
    val formattedPrice: String
        get() = "${price.toInt()} ₺"
    
    /**
     * Validates Firestore data integrity
     * Business logic: ensures required fields are present
     */
    fun isValid(): Boolean {
        return name.isNotBlank() &&
                price > 0.0 &&
                category.isNotBlank() &&
                businessId.isNotBlank() &&
                id.isNotBlank()
    }
    
    /**
     * Returns service summary for Firestore search
     * Memory efficient: string concatenation
     */
    val searchableText: String
        get() = "$name $description ${ServiceCategory.valueOf(category).getDisplayName()}"
} 