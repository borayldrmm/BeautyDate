package com.borayildirim.beautydate.data.models

import com.google.firebase.Timestamp

/**
 * Firestore-specific employee model
 * Optimized for Firebase storage and retrieval
 * Separate from domain model to allow independent evolution
 * Memory efficient: primitive types and JSON serialization for Firestore
 */
data class EmployeeFirestore(
    val id: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val gender: String = "", // Store as String for Firestore
    val phoneNumber: String = "",
    val email: String = "",
    val address: String = "",
    val hireDate: String = "", // dd/MM/yyyy format
    val skills: List<String> = emptyList(), // Firestore supports arrays
    val permissions: List<String> = emptyList(), // Store permission names as strings
    val notes: String = "",
    val salary: Double = 0.0, // Monthly salary in TRY
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
         * Converts domain Employee model to Firestore model
         * Memory efficient: direct field mapping with array conversion
         */
        fun fromDomainModel(employee: Employee, lastModifiedBy: String = ""): EmployeeFirestore {
            return EmployeeFirestore(
                id = employee.id,
                firstName = employee.firstName,
                lastName = employee.lastName,
                gender = employee.gender.name,
                phoneNumber = employee.phoneNumber,
                email = employee.email,
                address = employee.address,
                hireDate = employee.hireDate,
                skills = employee.skills,
                permissions = employee.permissions.map { it.name },
                notes = employee.notes,
                salary = employee.salary,
                isActive = employee.isActive,
                createdAt = employee.createdAt,
                updatedAt = employee.updatedAt,
                businessId = employee.businessId,
                syncVersion = System.currentTimeMillis(),
                lastModifiedBy = lastModifiedBy,
                isDeleted = false
            )
        }
    }
    
    /**
     * Converts Firestore model to domain Employee model
     * Memory efficient: single object creation with enum parsing
     */
    fun toDomainModel(): Employee {
        return Employee(
            id = id,
            firstName = firstName,
            lastName = lastName,
            gender = try {
                EmployeeGender.valueOf(gender)
            } catch (e: Exception) {
                EmployeeGender.OTHER // Default fallback for data integrity
            },
            phoneNumber = phoneNumber,
            email = email,
            address = address,
            hireDate = hireDate,
            skills = skills,
            permissions = permissions.mapNotNull { permissionName ->
                try {
                    EmployeePermission.valueOf(permissionName)
                } catch (e: Exception) {
                    null
                }
            },
            notes = notes,
            salary = salary,
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
        get() = "$firstName $lastName".trim()
    
    /**
     * Returns skills as comma-separated string
     */
    val skillsText: String
        get() = skills.joinToString(", ")
    
    /**
     * Returns permissions as comma-separated string with display names
     */
    val permissionsText: String
        get() = permissions.mapNotNull { permissionName ->
            try {
                EmployeePermission.valueOf(permissionName).getDisplayName()
            } catch (e: Exception) {
                null
            }
        }.joinToString(", ")
    
    /**
     * Validates Firestore data integrity
     * Business logic: ensures required fields are present
     */
    fun isValid(): Boolean {
        return firstName.isNotBlank() &&
                lastName.isNotBlank() &&
                phoneNumber.isNotBlank() &&
                hireDate.isNotBlank() &&
                businessId.isNotBlank() &&
                id.isNotBlank()
    }
    
    /**
     * Returns searchable text for Firestore search
     * Memory efficient: string concatenation
     */
    val searchableText: String
        get() = "$firstName $lastName $phoneNumber $email ${skills.joinToString(" ")}"
    
    /**
     * Returns employee summary for Firestore display
     */
    val summary: String
        get() = "$displayName (${skillsText.take(50)}${if (skillsText.length > 50) "..." else ""})"
} 