package com.borayildirim.beautydate.data.models

import com.google.firebase.Timestamp
import java.util.UUID

/**
 * Customer domain model for BeautyDate business app
 * Represents a customer with their personal and business information
 */
data class Customer(
    val id: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val phoneNumber: String = "", // TR format: 0(5--) --- ----
    val email: String = "",
    val birthDate: String = "", // dd/MM/yyyy format
    val gender: CustomerGender = CustomerGender.OTHER,
    val fileNumber: String = "", // Auto-generated custom format
    val notes: String = "",
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null,
    val businessId: String = "" // Owner business ID
) {
    companion object {
        /**
         * Generates a unique file number for new customers
         * Format: "CUST-YYYY-XXXX" where XXXX is random 4-digit number
         */
        fun generateFileNumber(): String {
            val year = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
            val randomNumber = (1000..9999).random()
            return "CUST-$year-$randomNumber"
        }
        
        /**
         * Generates a UUID for new customers
         */
        fun generateCustomerId(): String = UUID.randomUUID().toString()
    }
    
    /**
     * Returns full name of the customer
     */
    val fullName: String
        get() = "$firstName $lastName".trim()
    
    /**
     * Validates customer data for required fields
     */
    fun isValid(): Boolean {
        return firstName.isNotBlank() &&
                lastName.isNotBlank() &&
                phoneNumber.isNotBlank() &&
                birthDate.isNotBlank()
                // Gender validation removed - OTHER is acceptable
    }
}

/**
 * Customer gender enumeration
 * Used for icon selection and categorization
 */
enum class CustomerGender {
    MALE,
    FEMALE,
    OTHER;
    
    /**
     * Returns Turkish display name for gender
     */
    fun getDisplayName(): String {
        return when (this) {
            MALE -> "Erkek"
            FEMALE -> "Kadın"
            OTHER -> "Belirtilmemiş"
        }
    }
} 