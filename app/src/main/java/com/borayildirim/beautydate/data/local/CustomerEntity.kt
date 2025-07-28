package com.borayildirim.beautydate.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.borayildirim.beautydate.data.models.Customer
import com.borayildirim.beautydate.data.models.CustomerGender
import com.google.firebase.Timestamp

/**
 * Room entity for local customer storage
 * Includes sync management fields for offline-first approach
 */
@Entity(tableName = "customers")
data class CustomerEntity(
    @PrimaryKey 
    val id: String,
    val firstName: String,
    val lastName: String,
    val phoneNumber: String,
    val email: String,
    val birthDate: String,
    val gender: String, // Store as String for Room
    val fileNumber: String,
    val notes: String,
    val createdAt: Long, // Store as timestamp for Room
    val updatedAt: Long,
    val businessId: String,
    val isDeleted: Boolean = false,
    val needsSync: Boolean = false // For sync management
) {
    /**
     * Converts Room entity to domain model
     */
    fun toDomainModel(): Customer {
        return Customer(
            id = id,
            firstName = firstName,
            lastName = lastName,
            phoneNumber = phoneNumber,
            email = email,
            birthDate = birthDate,
            gender = CustomerGender.valueOf(gender),
            fileNumber = fileNumber,
            notes = notes,
            createdAt = Timestamp(createdAt / 1000, ((createdAt % 1000) * 1000000).toInt()),
            updatedAt = Timestamp(updatedAt / 1000, ((updatedAt % 1000) * 1000000).toInt()),
            businessId = businessId
        )
    }
    
    companion object {
        /**
         * Converts domain model to Room entity
         */
        fun fromDomainModel(customer: Customer, needsSync: Boolean = false): CustomerEntity {
            return CustomerEntity(
                id = customer.id,
                firstName = customer.firstName,
                lastName = customer.lastName,
                phoneNumber = customer.phoneNumber,
                email = customer.email,
                birthDate = customer.birthDate,
                gender = customer.gender.name,
                fileNumber = customer.fileNumber,
                notes = customer.notes,
                createdAt = customer.createdAt?.toDate()?.time ?: System.currentTimeMillis(),
                updatedAt = customer.updatedAt?.toDate()?.time ?: System.currentTimeMillis(),
                businessId = customer.businessId,
                needsSync = needsSync
            )
        }
        
        /**
         * Creates a new CustomerEntity with default values
         */
        fun createNew(
            firstName: String,
            lastName: String,
            phoneNumber: String,
            email: String,
            birthDate: String,
            gender: CustomerGender,
            notes: String,
            businessId: String
        ): CustomerEntity {
            val now = System.currentTimeMillis()
            return CustomerEntity(
                id = Customer.generateCustomerId(),
                firstName = firstName,
                lastName = lastName,
                phoneNumber = phoneNumber,
                email = email,
                birthDate = birthDate,
                gender = gender.name,
                fileNumber = Customer.generateFileNumber(),
                notes = notes,
                createdAt = now,
                updatedAt = now,
                businessId = businessId,
                needsSync = true // New entities need sync
            )
        }
    }
} 