package com.borayildirim.beautydate.data.models

import com.google.firebase.Timestamp
import java.util.UUID

/**
 * Customer note domain model for BeautyDate business app
 * Represents notes created for specific customers
 * Memory efficient: immutable data class with business logic
 */
data class CustomerNote(
    val id: String = "",
    val customerId: String = "",
    val customerName: String = "",
    val customerPhone: String = "",
    val title: String = "",
    val content: String = "",
    val businessId: String = "",
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null,
    val isImportant: Boolean = false
) {
    companion object {
        /**
         * Generates a unique note ID for new notes
         * Memory efficient: UUID generation
         */
        fun generateNoteId(): String = UUID.randomUUID().toString()
        
        /**
         * Creates an empty note for a specific customer
         * Memory efficient: single object creation
         */
        fun createForCustomer(
            customer: Customer,
            businessId: String
        ): CustomerNote {
            return CustomerNote(
                id = generateNoteId(),
                customerId = customer.id,
                customerName = customer.fullName,
                customerPhone = customer.phoneNumber,
                businessId = businessId,
                createdAt = Timestamp.now(),
                updatedAt = Timestamp.now()
            )
        }
    }
    
    /**
     * Returns formatted creation date
     * Memory efficient: string formatting
     */
    val formattedCreatedDate: String
        get() {
            return createdAt?.let { timestamp ->
                val date = timestamp.toDate()
                val formatter = java.text.SimpleDateFormat("dd.MM.yyyy HH:mm", java.util.Locale.getDefault())
                formatter.format(date)
            } ?: ""
        }
    
    /**
     * Returns formatted last update date
     * Memory efficient: string formatting
     */
    val formattedUpdatedDate: String
        get() {
            return updatedAt?.let { timestamp ->
                val date = timestamp.toDate()
                val formatter = java.text.SimpleDateFormat("dd.MM.yyyy HH:mm", java.util.Locale.getDefault())
                formatter.format(date)
            } ?: ""
        }
    
    /**
     * Returns note preview (first 100 characters)
     * Memory efficient: substring operation
     */
    val preview: String
        get() = if (content.length > 100) {
            content.take(100) + "..."
        } else {
            content
        }
    
    /**
     * Validates note data for required fields
     * Business logic: ensures all required fields are properly filled
     */
    fun isValid(): Boolean {
        return customerId.isNotBlank() &&
                customerName.isNotBlank() &&
                title.isNotBlank() &&
                content.isNotBlank() &&
                businessId.isNotBlank()
    }
    
    /**
     * Creates a copy with updated timestamp
     * Memory efficient: minimal object creation
     */
    fun withUpdatedTimestamp(): CustomerNote {
        return copy(updatedAt = Timestamp.now())
    }
    
    /**
     * Returns true if note contains search query
     * Memory efficient: case-insensitive search
     */
    fun matchesSearchQuery(query: String): Boolean {
        if (query.isBlank()) return true
        
        val searchQuery = query.lowercase()
        return customerName.lowercase().contains(searchQuery) ||
                customerPhone.contains(searchQuery) ||
                title.lowercase().contains(searchQuery) ||
                content.lowercase().contains(searchQuery)
    }
} 