package com.borayildirim.beautydate.data.models

import com.google.firebase.Timestamp
import java.util.UUID

/**
 * Service domain model for BeautyDate business app
 * Represents a service offering with pricing and categorization
 * Memory efficient: immutable data class with value objects
 */
data class Service(
    val id: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val category: ServiceCategory = ServiceCategory.NAIL,
    val subcategory: ServiceSubcategory? = null,
    val description: String = "",
    val isActive: Boolean = true,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null,
    val businessId: String = ""
) {
    companion object {
        /**
         * Generates a unique service ID for new services
         * Memory efficient: UUID generation
         */
        fun generateServiceId(): String = UUID.randomUUID().toString()
        
        /**
         * Creates a service from subcategory with default values
         * Memory efficient: single object creation
         */
        fun fromSubcategory(
            subcategory: ServiceSubcategory,
            businessId: String,
            customPrice: Double? = null
        ): Service {
            return Service(
                id = generateServiceId(),
                name = subcategory.displayName,
                price = customPrice ?: subcategory.defaultPrice,
                category = subcategory.category,
                subcategory = subcategory,
                description = "",
                isActive = true,
                createdAt = Timestamp.now(),
                updatedAt = Timestamp.now(),
                businessId = businessId
            )
        }
    }
    
    /**
     * Returns formatted price with currency symbol
     * Memory efficient: string concatenation
     */
    val formattedPrice: String
        get() = "${price.toInt()} ₺"
    
    /**
     * Validates service data for required fields
     * Business logic: ensures all required fields are properly filled
     */
    fun isValid(): Boolean {
        return name.isNotBlank() &&
                price > 0.0 &&
                businessId.isNotBlank()
    }
    
    /**
     * Creates a copy with updated timestamp
     * Memory efficient: minimal object creation
     */
    fun withUpdatedTimestamp(): Service {
        return copy(updatedAt = Timestamp.now())
    }
} 