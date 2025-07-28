package com.borayildirim.beautydate.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * Room DAO for Employee entity operations
 * Provides all database operations with Flow support for reactive UI
 * Memory efficient: Flow-based reactive queries with optimized indexing
 */
@Dao
interface EmployeeDao {
    
    /**
     * Gets all employees for a business as Flow (active and inactive)
     * Memory efficient: Flow for reactive UI updates
     */
    @Query("SELECT * FROM employees WHERE businessId = :businessId AND isDeleted = 0 ORDER BY firstName ASC")
    fun getAllEmployees(businessId: String): Flow<List<EmployeeEntity>>
    
    /**
     * Gets all employees for a business as List (for sync operations)
     */
    @Query("SELECT * FROM employees WHERE businessId = :businessId AND isDeleted = 0 ORDER BY firstName ASC")
    suspend fun getAllEmployeesSync(businessId: String): List<EmployeeEntity>
    
    /**
     * Gets active employees only
     * Memory efficient: indexed isActive filtering
     */
    @Query("SELECT * FROM employees WHERE businessId = :businessId AND isDeleted = 0 AND isActive = 1 ORDER BY firstName ASC")
    fun getActiveEmployees(businessId: String): Flow<List<EmployeeEntity>>
    
    /**
     * Gets employees by gender
     * Memory efficient: indexed gender filtering
     */
    @Query("SELECT * FROM employees WHERE businessId = :businessId AND gender = :gender AND isDeleted = 0 AND isActive = 1 ORDER BY firstName ASC")
    fun getEmployeesByGender(businessId: String, gender: String): Flow<List<EmployeeEntity>>
    
    /**
     * Gets employees with specific permission (JSON search)
     * Memory efficient: JSON LIKE query for permission matching
     */
    @Query("SELECT * FROM employees WHERE businessId = :businessId AND permissions LIKE '%' || :permission || '%' AND isDeleted = 0 AND isActive = 1 ORDER BY firstName ASC")
    fun getEmployeesWithPermission(businessId: String, permission: String): Flow<List<EmployeeEntity>>
    
    /**
     * Gets employees with specific skill (JSON search)
     * Memory efficient: JSON LIKE query for skill matching
     */
    @Query("SELECT * FROM employees WHERE businessId = :businessId AND skills LIKE '%' || :skill || '%' AND isDeleted = 0 AND isActive = 1 ORDER BY firstName ASC")
    fun getEmployeesWithSkill(businessId: String, skill: String): Flow<List<EmployeeEntity>>
    
    /**
     * Searches employees by name, phone or email
     * Memory efficient: LIKE query with proper indexing
     */
    @Query("""
        SELECT * FROM employees 
        WHERE businessId = :businessId 
        AND isDeleted = 0 
        AND isActive = 1
        AND (firstName LIKE '%' || :searchQuery || '%' 
             OR lastName LIKE '%' || :searchQuery || '%'
             OR phoneNumber LIKE '%' || :searchQuery || '%'
             OR email LIKE '%' || :searchQuery || '%')
        ORDER BY firstName ASC
    """)
    fun searchEmployees(businessId: String, searchQuery: String): Flow<List<EmployeeEntity>>
    
    /**
     * Gets a specific employee by ID
     */
    @Query("SELECT * FROM employees WHERE id = :employeeId AND isDeleted = 0")
    suspend fun getEmployeeById(employeeId: String): EmployeeEntity?
    
    /**
     * Gets employees that need sync with Firestore
     */
    @Query("SELECT * FROM employees WHERE businessId = :businessId AND needsSync = 1")
    suspend fun getEmployeesNeedingSync(businessId: String): List<EmployeeEntity>
    
    /**
     * Inserts a new employee
     * Memory efficient: single insert operation
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmployee(employee: EmployeeEntity)
    
    /**
     * Inserts multiple employees (for sync operations)
     * Memory efficient: batch insert
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmployees(employees: List<EmployeeEntity>)
    
    /**
     * Updates an existing employee
     */
    @Update
    suspend fun updateEmployee(employee: EmployeeEntity)
    
    /**
     * Updates employee active status
     * Memory efficient: direct field update without object loading
     */
    @Query("UPDATE employees SET isActive = :isActive, updatedAt = :timestamp, needsSync = 1 WHERE id = :employeeId")
    suspend fun updateEmployeeStatus(employeeId: String, isActive: Boolean, timestamp: Long = System.currentTimeMillis())
    
    /**
     * Updates employee permissions
     * Memory efficient: direct JSON field update
     */
    @Query("UPDATE employees SET permissions = :permissions, updatedAt = :timestamp, needsSync = 1 WHERE id = :employeeId")
    suspend fun updateEmployeePermissions(employeeId: String, permissions: String, timestamp: Long = System.currentTimeMillis())
    
    /**
     * Updates employee skills
     * Memory efficient: direct JSON field update
     */
    @Query("UPDATE employees SET skills = :skills, updatedAt = :timestamp, needsSync = 1 WHERE id = :employeeId")
    suspend fun updateEmployeeSkills(employeeId: String, skills: String, timestamp: Long = System.currentTimeMillis())
    
    /**
     * Soft delete an employee (mark as deleted)
     */
    @Query("UPDATE employees SET isDeleted = 1, needsSync = 1, updatedAt = :timestamp WHERE id = :employeeId")
    suspend fun deleteEmployee(employeeId: String, timestamp: Long = System.currentTimeMillis())
    
    /**
     * Hard delete an employee from local database
     */
    @Query("DELETE FROM employees WHERE id = :employeeId")
    suspend fun hardDeleteEmployee(employeeId: String)
    
    /**
     * Marks employee as synced with Firestore
     */
    @Query("UPDATE employees SET needsSync = 0 WHERE id = :employeeId")
    suspend fun markAsSynced(employeeId: String)
    
    /**
     * Marks employee as needing sync
     */
    @Query("UPDATE employees SET needsSync = 1, updatedAt = :timestamp WHERE id = :employeeId")
    suspend fun markAsNeedsSync(employeeId: String, timestamp: Long = System.currentTimeMillis())
    
    /**
     * Gets total employee count for a business
     */
    @Query("SELECT COUNT(*) FROM employees WHERE businessId = :businessId AND isDeleted = 0")
    suspend fun getEmployeeCount(businessId: String): Int
    
    /**
     * Gets active employee count for a business
     */
    @Query("SELECT COUNT(*) FROM employees WHERE businessId = :businessId AND isDeleted = 0 AND isActive = 1")
    suspend fun getActiveEmployeeCount(businessId: String): Int
    
    /**
     * Checks if a phone number already exists for a business
     * Business logic: prevent duplicate phone numbers
     */
    @Query("""
        SELECT COUNT(*) > 0 FROM employees 
        WHERE businessId = :businessId 
        AND phoneNumber = :phoneNumber 
        AND isDeleted = 0 
        AND id != :excludeEmployeeId
    """)
    suspend fun phoneNumberExists(businessId: String, phoneNumber: String, excludeEmployeeId: String = ""): Boolean
    
    /**
     * Checks if an email already exists for a business
     * Business logic: prevent duplicate emails
     */
    @Query("""
        SELECT COUNT(*) > 0 FROM employees 
        WHERE businessId = :businessId 
        AND email = :email 
        AND isDeleted = 0 
        AND id != :excludeEmployeeId
    """)
    suspend fun emailExists(businessId: String, email: String, excludeEmployeeId: String = ""): Boolean
    
    /**
     * Gets all unique skills across employees
     * Memory efficient: aggregation for skills analysis
     */
    @Query("SELECT DISTINCT skills FROM employees WHERE businessId = :businessId AND isDeleted = 0 AND isActive = 1")
    suspend fun getAllSkillsJson(businessId: String): List<String>
    
    /**
     * Gets all permissions usage count
     * For statistics and permission analysis
     */
    @Query("SELECT permissions FROM employees WHERE businessId = :businessId AND isDeleted = 0 AND isActive = 1")
    suspend fun getAllPermissionsJson(businessId: String): List<String>
    
    /**
     * Clears all employees for a business (for fresh sync)
     */
    @Query("DELETE FROM employees WHERE businessId = :businessId")
    suspend fun clearAllEmployees(businessId: String)
} 