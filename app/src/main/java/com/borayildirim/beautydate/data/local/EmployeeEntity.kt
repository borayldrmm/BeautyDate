package com.borayildirim.beautydate.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.borayildirim.beautydate.data.models.Employee
import com.borayildirim.beautydate.data.models.EmployeeGender
import com.borayildirim.beautydate.data.models.EmployeePermission
import com.google.firebase.Timestamp

/**
 * Room entity for local employee storage
 * Includes sync management fields for offline-first approach
 * Memory efficient: primitive types and JSON serialization for collections
 */
@Entity(tableName = "employees")
data class EmployeeEntity(
    @PrimaryKey 
    val id: String,
    val firstName: String,
    val lastName: String,
    val gender: String, // Store as String for Room
    val phoneNumber: String,
    val email: String,
    val address: String,
    val hireDate: String, // dd/MM/yyyy format
    val skills: String, // JSON string for List<String>
    val permissions: String, // JSON string for List<EmployeePermission>
    val notes: String,
    val salary: Double = 0.0, // Monthly salary in TRY
    val isActive: Boolean = true,
    val createdAt: Long, // Store as timestamp for Room
    val updatedAt: Long,
    val businessId: String,
    val isDeleted: Boolean = false,
    val needsSync: Boolean = false // For sync management
) {
    /**
     * Converts Room entity to domain model
     * Memory efficient: single object creation with JSON parsing
     */
    fun toDomainModel(): Employee {
        return Employee(
            id = id,
            firstName = firstName,
            lastName = lastName,
            gender = try {
                EmployeeGender.valueOf(gender)
            } catch (e: Exception) {
                EmployeeGender.OTHER // Default fallback
            },
            phoneNumber = phoneNumber,
            email = email,
            address = address,
            hireDate = hireDate,
            skills = parseSkillsFromJson(skills),
            permissions = parsePermissionsFromJson(permissions),
            notes = notes,
            salary = salary,
            isActive = isActive,
            createdAt = Timestamp(createdAt / 1000, ((createdAt % 1000) * 1000000).toInt()),
            updatedAt = Timestamp(updatedAt / 1000, ((updatedAt % 1000) * 1000000).toInt()),
            businessId = businessId
        )
    }
    
    companion object {
        /**
         * Converts domain model to Room entity
         * Memory efficient: direct field mapping with JSON serialization
         */
        fun fromDomainModel(employee: Employee, needsSync: Boolean = false): EmployeeEntity {
            return EmployeeEntity(
                id = employee.id,
                firstName = employee.firstName,
                lastName = employee.lastName,
                gender = employee.gender.name,
                phoneNumber = employee.phoneNumber,
                email = employee.email,
                address = employee.address,
                hireDate = employee.hireDate,
                skills = serializeSkillsToJson(employee.skills),
                permissions = serializePermissionsToJson(employee.permissions),
                notes = employee.notes,
                salary = employee.salary,
                isActive = employee.isActive,
                createdAt = employee.createdAt?.toDate()?.time ?: System.currentTimeMillis(),
                updatedAt = employee.updatedAt?.toDate()?.time ?: System.currentTimeMillis(),
                businessId = employee.businessId,
                needsSync = needsSync
            )
        }
        
        /**
         * Creates a new EmployeeEntity with default values
         * Memory efficient: minimal object creation
         */
        fun createNew(
            firstName: String,
            lastName: String,
            gender: EmployeeGender,
            phoneNumber: String,
            email: String,
            address: String,
            hireDate: String,
            skills: List<String>,
            permissions: List<EmployeePermission>,
            notes: String,
            businessId: String
        ): EmployeeEntity {
            val now = System.currentTimeMillis()
            return EmployeeEntity(
                id = Employee.generateEmployeeId(),
                firstName = firstName,
                lastName = lastName,
                gender = gender.name,
                phoneNumber = phoneNumber,
                email = email,
                address = address,
                hireDate = hireDate,
                skills = serializeSkillsToJson(skills),
                permissions = serializePermissionsToJson(permissions),
                notes = notes,
                isActive = true,
                createdAt = now,
                updatedAt = now,
                businessId = businessId,
                needsSync = true // New entities need sync
            )
        }
        
        /**
         * Serializes skills list to JSON string for Room storage
         * Memory efficient: simple JSON array format
         */
        private fun serializeSkillsToJson(skills: List<String>): String {
            return if (skills.isEmpty()) {
                "[]"
            } else {
                "[${skills.joinToString(",") { "\"$it\"" }}]"
            }
        }
        
        /**
         * Serializes permissions list to JSON string for Room storage
         * Memory efficient: enum name serialization
         */
        private fun serializePermissionsToJson(permissions: List<EmployeePermission>): String {
            return if (permissions.isEmpty()) {
                "[]"
            } else {
                "[${permissions.joinToString(",") { "\"${it.name}\"" }}]"
            }
        }
        
        /**
         * Parses skills from JSON string
         * Memory efficient: minimal string processing
         */
        private fun parseSkillsFromJson(json: String): List<String> {
            return try {
                if (json.isBlank() || json == "[]") {
                    emptyList()
                } else {
                    // Simple JSON parsing for skills array
                    json.removeSurrounding("[", "]")
                        .split(",")
                        .map { it.trim().removeSurrounding("\"") }
                        .filter { it.isNotBlank() }
                }
            } catch (e: Exception) {
                emptyList()
            }
        }
        
        /**
         * Parses permissions from JSON string
         * Memory efficient: enum valueOf with fallback
         */
        private fun parsePermissionsFromJson(json: String): List<EmployeePermission> {
            return try {
                if (json.isBlank() || json == "[]") {
                    emptyList()
                } else {
                    // Simple JSON parsing for permissions array
                    json.removeSurrounding("[", "]")
                        .split(",")
                        .map { it.trim().removeSurrounding("\"") }
                        .filter { it.isNotBlank() }
                        .mapNotNull { permissionName ->
                            try {
                                EmployeePermission.valueOf(permissionName)
                            } catch (e: Exception) {
                                null
                            }
                        }
                }
            } catch (e: Exception) {
                emptyList()
            }
        }
    }
} 