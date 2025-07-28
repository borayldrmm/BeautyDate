package com.borayildirim.beautydate.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index
import com.borayildirim.beautydate.data.models.CustomerNote
import com.google.firebase.Timestamp

/**
 * Room entity for customer notes
 * Optimized for local database storage with indexing
 * Memory efficient: primitive types and indexed fields
 */
@Entity(
    tableName = "customer_notes",
    indices = [
        Index(value = ["customerId"]),
        Index(value = ["businessId"]),
        Index(value = ["customerName"]),
        Index(value = ["createdAt"]),
        Index(value = ["isImportant"])
    ]
)
data class CustomerNoteEntity(
    @PrimaryKey
    val id: String,
    val customerId: String,
    val customerName: String,
    val customerPhone: String,
    val title: String,
    val content: String,
    val businessId: String,
    val createdAt: Long, // Timestamp as Long for Room
    val updatedAt: Long, // Timestamp as Long for Room
    val isImportant: Boolean = false
) {
    companion object {
        /**
         * Converts domain model to entity
         * Memory efficient: direct field mapping
         */
        fun fromDomain(note: CustomerNote): CustomerNoteEntity {
            return CustomerNoteEntity(
                id = note.id,
                customerId = note.customerId,
                customerName = note.customerName,
                customerPhone = note.customerPhone,
                title = note.title,
                content = note.content,
                businessId = note.businessId,
                createdAt = note.createdAt?.seconds ?: 0L,
                updatedAt = note.updatedAt?.seconds ?: 0L,
                isImportant = note.isImportant
            )
        }
    }
    
    /**
     * Converts entity to domain model
     * Memory efficient: direct field mapping with Timestamp conversion
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
            createdAt = if (createdAt > 0) Timestamp(createdAt, 0) else null,
            updatedAt = if (updatedAt > 0) Timestamp(updatedAt, 0) else null,
            isImportant = isImportant
        )
    }
} 