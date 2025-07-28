package com.borayildirim.beautydate.data.models

import com.google.firebase.Timestamp

/**
 * Firestore-specific customer model
 * Optimized for Firebase storage and retrieval
 * Separate from domain model to allow independent evolution
 */
data class CustomerFirestore(
    val id: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val phoneNumber: String = "",
    val email: String = "",
    val birthDate: String = "",
    val gender: String = "", // Store as String for Firestore
    val fileNumber: String = "",
    val notes: String = "",
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null,
    val businessId: String = "",
    // Firestore-specific metadata
    val syncVersion: Long = 1,
    val lastModifiedBy: String = "",
    val isActive: Boolean = true
) {
    companion object {
        /**
         * Converts domain Customer model to Firestore model
         */
        fun fromDomainModel(customer: Customer, lastModifiedBy: String = ""): CustomerFirestore {
            return CustomerFirestore(
                id = customer.id,
                firstName = customer.firstName,
                lastName = customer.lastName,
                phoneNumber = customer.phoneNumber,
                email = customer.email,
                birthDate = customer.birthDate,
                gender = customer.gender.name,
                fileNumber = customer.fileNumber,
                notes = customer.notes,
                createdAt = customer.createdAt,
                updatedAt = customer.updatedAt,
                businessId = customer.businessId,
                syncVersion = System.currentTimeMillis(),
                lastModifiedBy = lastModifiedBy,
                isActive = true
            )
        }
    }
    
    /**
     * Converts Firestore model to domain Customer model
     */
    fun toDomainModel(): Customer {
        return Customer(
            id = id,
            firstName = firstName,
            lastName = lastName,
            phoneNumber = phoneNumber,
            email = email,
            birthDate = birthDate,
            gender = try {
                CustomerGender.valueOf(gender)
            } catch (e: Exception) {
                CustomerGender.OTHER
            },
            fileNumber = fileNumber,
            notes = notes,
            createdAt = createdAt,
            updatedAt = updatedAt,
            businessId = businessId
        )
    }
    
    /**
     * Returns full name for Firestore queries
     */
    val fullName: String
        get() = "$firstName $lastName".trim()
    
    /**
     * Validates Firestore data
     */
    fun isValid(): Boolean {
        return firstName.isNotBlank() &&
                lastName.isNotBlank() &&
                phoneNumber.isNotBlank() &&
                businessId.isNotBlank() &&
                id.isNotBlank()
    }
} 