package com.borayildirim.beautydate.data.repository

import com.borayildirim.beautydate.data.models.Employee
import com.borayildirim.beautydate.data.models.EmployeeGender
import com.borayildirim.beautydate.data.models.EmployeePermission
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for employee management
 * Follows Repository pattern and Interface Segregation Principle
 * Multi-tenant architecture: All operations use authenticated businessId via AuthUtil
 * Memory efficient: uses Flow for reactive data and Result for error handling
 */
interface EmployeeRepository {
    
    /**
     * Gets all employees for current authenticated business as Flow (offline-first)
     * BusinessId filtering applied automatically via AuthUtil
     */
    fun getAllEmployees(): Flow<List<Employee>>
    
    /**
     * Gets active employees only for current authenticated business
     * BusinessId filtering applied automatically via AuthUtil
     */
    fun getActiveEmployees(): Flow<List<Employee>>
    
    /**
     * Gets employees by gender for current authenticated business
     * BusinessId filtering applied automatically via AuthUtil
     */
    fun getEmployeesByGender(gender: EmployeeGender): Flow<List<Employee>>
    
    /**
     * Gets employees with specific permission for current authenticated business
     * BusinessId filtering applied automatically via AuthUtil
     */
    fun getEmployeesWithPermission(permission: EmployeePermission): Flow<List<Employee>>
    
    /**
     * Gets employees with specific skill for current authenticated business
     * BusinessId filtering applied automatically via AuthUtil
     */
    fun getEmployeesWithSkill(skill: String): Flow<List<Employee>>
    
    /**
     * Searches employees by query (name, phone, or email) for current authenticated business
     * BusinessId filtering applied automatically via AuthUtil
     */
    fun searchEmployees(query: String): Flow<List<Employee>>
    
    /**
     * Gets a specific employee by ID
     */
    suspend fun getEmployeeById(employeeId: String): Employee?
    
    /**
     * Adds a new employee to current authenticated business (local + sync)
     * BusinessId assigned automatically via AuthUtil
     */
    suspend fun addEmployee(employee: Employee): Result<Employee>
    
    /**
     * Updates an existing employee (local + sync)
     */
    suspend fun updateEmployee(employee: Employee): Result<Employee>
    
    /**
     * Deletes an employee (soft delete + sync)
     */
    suspend fun deleteEmployee(employeeId: String): Result<Unit>
    
    /**
     * Toggles employee active status
     */
    suspend fun toggleEmployeeStatus(employeeId: String, isActive: Boolean): Result<Unit>
    
    /**
     * Updates employee permissions
     */
    suspend fun updateEmployeePermissions(employeeId: String, permissions: List<EmployeePermission>): Result<Unit>
    
    /**
     * Updates employee skills
     */
    suspend fun updateEmployeeSkills(employeeId: String, skills: List<String>): Result<Unit>
    
    /**
     * Manually syncs employees with Firestore for current authenticated business
     * BusinessId applied automatically via AuthUtil
     */
    suspend fun syncWithFirestore(): Result<Unit>
    
    /**
     * Performs initial sync when app starts for current authenticated business
     * BusinessId applied automatically via AuthUtil
     */
    suspend fun performInitialSync(): Result<Unit>
    
    /**
     * Checks if employee phone number already exists in current authenticated business
     * BusinessId filtering applied automatically via AuthUtil
     */
    suspend fun phoneNumberExists(phoneNumber: String, excludeEmployeeId: String = ""): Boolean
    
    /**
     * Checks if employee email already exists in current authenticated business
     * BusinessId filtering applied automatically via AuthUtil
     */
    suspend fun emailExists(email: String, excludeEmployeeId: String = ""): Boolean
    
    /**
     * Gets total employee count for current authenticated business
     * BusinessId filtering applied automatically via AuthUtil
     */
    suspend fun getEmployeeCount(): Int
    
    /**
     * Gets employee count by status for current authenticated business
     * BusinessId filtering applied automatically via AuthUtil
     */
    suspend fun getEmployeeCountByStatus(isActive: Boolean): Int
    
    /**
     * Gets employees by hiring date range for current authenticated business
     * BusinessId filtering applied automatically via AuthUtil
     */
    fun getEmployeesByHiringDateRange(startDate: String, endDate: String): Flow<List<Employee>>
    
    /**
     * Bulk update employee status for multiple employees
     * BusinessId filtering applied automatically via AuthUtil
     */
    suspend fun bulkUpdateEmployeeStatus(employeeIds: List<String>, isActive: Boolean): Result<Int>
    
    /**
     * Hard delete employee (only for testing, use soft delete in production)
     * BusinessId filtering applied automatically via AuthUtil
     */
    suspend fun hardDeleteEmployee(employeeId: String): Result<Unit>
}

/**
 * Employee statistics data class
 * Memory efficient: aggregated data for dashboard display
 */
data class EmployeeStats(
    val totalEmployees: Int,
    val activeEmployees: Int,
    val inactiveEmployees: Int,
    val averageSkillsPerEmployee: Double
) {
    /**
     * Returns formatted active percentage
     */
    val activePercentage: Int
        get() = if (totalEmployees > 0) ((activeEmployees * 100) / totalEmployees) else 0
} 