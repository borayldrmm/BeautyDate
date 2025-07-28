package com.borayildirim.beautydate.data.remote.models

import com.borayildirim.beautydate.data.models.CustomerNote
import com.google.firebase.Timestamp

/**
 * Firestore model for customer notes
 * Optimized for Firestore serialization/deserialization
 * Memory efficient: nullable fields with default values
 */
data class CustomerNoteFirestore(
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
         * Converts domain model to Firestore model
         * Memory efficient: direct field mapping
         */
        fun fromDomain(note: CustomerNote): CustomerNoteFirestore {
            return CustomerNoteFirestore(
                id = note.id,
                customerId = note.customerId,
                customerName = note.customerName,
                customerPhone = note.customerPhone,
                title = note.title,
                content = note.content,
                businessId = note.businessId,
                createdAt = note.createdAt,
                updatedAt = note.updatedAt,
                isImportant = note.isImportant
            )
        }
    }
    
    /**
     * Converts Firestore model to domain model
     * Memory efficient: direct field mapping
     */
    fun toDomain(): CustomerNote {
        return CustomerNote(
            id = id,
            customerId = customerId,
            customerName = customerName,
            customerPhone = customerPhone,
            title = title,
            content = content,
            businessId = businessId,
            createdAt = createdAt,
            updatedAt = updatedAt,
            isImportant = isImportant
        )
    }
    
    /**
     * Converts to Map for Firestore operations
     * Memory efficient: HashMap creation for batch operations
     */
    fun toMap(): Map<String, Any?> {
        return hashMapOf(
            "id" to id,
            "customerId" to customerId,
            "customerName" to customerName,
            "customerPhone" to customerPhone,
            "title" to title,
            "content" to content,
            "businessId" to businessId,
            "createdAt" to createdAt,
            "updatedAt" to updatedAt,
            "isImportant" to isImportant
        )
    }
} 