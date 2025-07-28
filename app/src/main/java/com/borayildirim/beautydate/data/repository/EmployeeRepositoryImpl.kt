package com.borayildirim.beautydate.data.repository

import com.borayildirim.beautydate.data.local.EmployeeDao
import com.borayildirim.beautydate.data.local.EmployeeEntity
import com.borayildirim.beautydate.data.models.Employee
import com.borayildirim.beautydate.data.models.EmployeeGender
import com.borayildirim.beautydate.data.models.EmployeePermission
import com.borayildirim.beautydate.data.models.EmployeeFirestore
import com.borayildirim.beautydate.utils.NetworkMonitor
import com.borayildirim.beautydate.utils.AuthUtil
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of EmployeeRepository with offline-first approach
 * Uses Room for local storage and Firestore for remote sync
 * Multi-tenant architecture: All operations filtered by authenticated businessId
 * Memory efficient: Flow-based reactive data and minimal object creation
 */
@Singleton
class EmployeeRepositoryImpl @Inject constructor(
    private val employeeDao: EmployeeDao,
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth,
    private val networkMonitor: NetworkMonitor,
    private val authUtil: AuthUtil
) : EmployeeRepository {
    
    companion object {
        private const val EMPLOYEES_COLLECTION = "employees"
    }
    
    /**
     * Gets all employees as Flow from local database (offline-first)
     * BusinessId filtering applied automatically
     * Memory efficient: Flow mapping with lazy evaluation
     */
    override fun getAllEmployees(): Flow<List<Employee>> {
        val currentBusinessId = authUtil.getCurrentBusinessIdSafe()
        return employeeDao.getAllEmployees(currentBusinessId)
            .map { entities -> entities.map { it.toDomainModel() } }
    }
    
    /**
     * Gets active employees only from local database
     * BusinessId filtering applied automatically
     * Memory efficient: indexed database query
     */
    override fun getActiveEmployees(): Flow<List<Employee>> {
        val currentBusinessId = authUtil.getCurrentBusinessIdSafe()
        return employeeDao.getActiveEmployees(currentBusinessId)
            .map { entities -> entities.map { it.toDomainModel() } }
    }
    
    /**
     * Gets employees by gender from local database
     * BusinessId filtering applied automatically
     * Memory efficient: indexed database query
     */
    override fun getEmployeesByGender(gender: EmployeeGender): Flow<List<Employee>> {
        val currentBusinessId = authUtil.getCurrentBusinessIdSafe()
        return employeeDao.getEmployeesByGender(currentBusinessId, gender.name)
            .map { entities -> entities.map { it.toDomainModel() } }
    }
    
    /**
     * Gets employees with specific permission from local database
     * BusinessId filtering applied automatically
     * Memory efficient: JSON LIKE query
     */
    override fun getEmployeesWithPermission(permission: EmployeePermission): Flow<List<Employee>> {
        val currentBusinessId = authUtil.getCurrentBusinessIdSafe()
        return employeeDao.getEmployeesWithPermission(currentBusinessId, permission.name)
            .map { entities -> entities.map { it.toDomainModel() } }
    }
    
    /**
     * Gets employees with specific skill from local database
     * BusinessId filtering applied automatically
     * Memory efficient: JSON LIKE query
     */
    override fun getEmployeesWithSkill(skill: String): Flow<List<Employee>> {
        val currentBusinessId = authUtil.getCurrentBusinessIdSafe()
        return employeeDao.getEmployeesWithSkill(currentBusinessId, skill)
            .map { entities -> entities.map { it.toDomainModel() } }
    }
    
    /**
     * Searches employees by query from local database
     * BusinessId filtering applied automatically
     * Memory efficient: SQL LIKE query with proper indexing
     */
    override fun searchEmployees(query: String): Flow<List<Employee>> {
        val currentBusinessId = authUtil.getCurrentBusinessIdSafe()
        return if (query.isBlank()) {
            getAllEmployees()
        } else {
            employeeDao.searchEmployees(currentBusinessId, query)
                .map { entities -> entities.map { it.toDomainModel() } }
        }
    }
    
    /**
     * Gets employee by ID from local database
     * Memory efficient: single object retrieval
     */
    override suspend fun getEmployeeById(employeeId: String): Employee? {
        return employeeDao.getEmployeeById(employeeId)?.toDomainModel()
    }
    
    /**
     * Adds new employee to local database and triggers automatic sync
     * BusinessId assigned automatically
     * Memory efficient: immediate local insert with background sync
     */
    override suspend fun addEmployee(employee: Employee): Result<Employee> {
        return try {
            val currentBusinessId = authUtil.getCurrentBusinessIdSafe()
            
            val newEmployee = employee.copy(
                id = if (employee.id.isEmpty()) Employee.generateEmployeeId() else employee.id,
                businessId = currentBusinessId,
                createdAt = Timestamp.now(),
                updatedAt = Timestamp.now()
            )
            
            
            val entity = EmployeeEntity.fromDomainModel(newEmployee, needsSync = true)
            employeeDao.insertEmployee(entity)
            
            
            // Verify insertion
            val insertedEmployee = employeeDao.getEmployeeById(newEmployee.id)
            if (insertedEmployee != null) {
            } else {
            }
            
            // Trigger automatic sync to Firebase (only if online)
            if (networkMonitor.isCurrentlyConnected()) {
                try {
                    val currentUser = firebaseAuth.currentUser
                    val firestoreEmployee = EmployeeFirestore.fromDomainModel(
                        employee = newEmployee,
                        lastModifiedBy = currentUser?.uid ?: "unknown"
                    )
                    
                    firestore.collection(EMPLOYEES_COLLECTION)
                        .document(newEmployee.id)
                        .set(firestoreEmployee)
                        .await()
                    
                    employeeDao.markAsSynced(newEmployee.id)
                } catch (syncError: Exception) {
                    // Still return success as local save succeeded
                }
            }
            
            Result.success(newEmployee)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
    
    /**
     * Updates existing employee in local database and triggers sync
     * Memory efficient: direct entity update
     */
    override suspend fun updateEmployee(employee: Employee): Result<Employee> {
        return try {
            val updatedEmployee = employee.copy(updatedAt = Timestamp.now())
            val entity = EmployeeEntity.fromDomainModel(updatedEmployee, needsSync = true)
            employeeDao.updateEmployee(entity)
            
            // Try immediate sync for updates
            if (networkMonitor.isCurrentlyConnected()) {
                try {
                    val currentUser = firebaseAuth.currentUser
                    val firestoreEmployee = EmployeeFirestore.fromDomainModel(
                        employee = updatedEmployee,
                        lastModifiedBy = currentUser?.uid ?: "unknown"
                    )
                    
                    firestore.collection(EMPLOYEES_COLLECTION)
                        .document(updatedEmployee.id)
                        .set(firestoreEmployee)
                        .await()
                    
                    employeeDao.markAsSynced(updatedEmployee.id)
                } catch (syncError: Exception) {
                }
            }
            
            Result.success(updatedEmployee)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Deletes employee from both local and Firestore
     * Memory efficient: immediate local deletion with Firestore sync
     */
    override suspend fun deleteEmployee(employeeId: String): Result<Unit> {
        return try {
            
            // Soft delete from local database
            employeeDao.deleteEmployee(employeeId)
            
            // Try immediate deletion from Firestore if online
            if (networkMonitor.isCurrentlyConnected()) {
                try {
                    
                    firestore.collection(EMPLOYEES_COLLECTION)
                        .document(employeeId)
                        .delete()
                        .await()
                    
                    
                    // Hard delete from local database after successful Firestore deletion
                    employeeDao.hardDeleteEmployee(employeeId)
                    
                } catch (syncError: Exception) {
                }
            } else {
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Toggles employee active status
     * Memory efficient: direct field update without full object loading
     */
    override suspend fun toggleEmployeeStatus(employeeId: String, isActive: Boolean): Result<Unit> {
        return try {
            employeeDao.updateEmployeeStatus(employeeId, isActive)
            
            // Sync status change to Firestore if online
            if (networkMonitor.isCurrentlyConnected()) {
                try {
                    firestore.collection(EMPLOYEES_COLLECTION)
                        .document(employeeId)
                        .update("isActive", isActive, "updatedAt", Timestamp.now())
                        .await()
                    
                    employeeDao.markAsSynced(employeeId)
                } catch (syncError: Exception) {
                }
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Updates employee permissions
     * Memory efficient: JSON serialization and direct field update
     */
    override suspend fun updateEmployeePermissions(employeeId: String, permissions: List<EmployeePermission>): Result<Unit> {
        return try {
            // Serialize permissions to JSON string for Room storage
            val permissionsJson = if (permissions.isEmpty()) {
                "[]"
            } else {
                "[${permissions.joinToString(",") { "\"${it.name}\"" }}]"
            }
            
            employeeDao.updateEmployeePermissions(employeeId, permissionsJson)
            
            // Sync to Firestore if online
            if (networkMonitor.isCurrentlyConnected()) {
                try {
                    firestore.collection(EMPLOYEES_COLLECTION)
                        .document(employeeId)
                        .update(
                            "permissions", permissions.map { it.name },
                            "updatedAt", Timestamp.now()
                        )
                        .await()
                    
                    employeeDao.markAsSynced(employeeId)
                } catch (syncError: Exception) {
                }
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Updates employee skills
     * Memory efficient: JSON serialization and direct field update
     */
    override suspend fun updateEmployeeSkills(employeeId: String, skills: List<String>): Result<Unit> {
        return try {
            // Serialize skills to JSON string for Room storage
            val skillsJson = if (skills.isEmpty()) {
                "[]"
            } else {
                "[${skills.joinToString(",") { "\"$it\"" }}]"
            }
            
            employeeDao.updateEmployeeSkills(employeeId, skillsJson)
            
            // Sync to Firestore if online
            if (networkMonitor.isCurrentlyConnected()) {
                try {
                    firestore.collection(EMPLOYEES_COLLECTION)
                        .document(employeeId)
                        .update(
                            "skills", skills,
                            "updatedAt", Timestamp.now()
                        )
                        .await()
                    
                    employeeDao.markAsSynced(employeeId)
                } catch (syncError: Exception) {
                }
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Checks if employee phone number already exists
     * BusinessId filtering applied automatically
     */
    override suspend fun phoneNumberExists(phoneNumber: String, excludeEmployeeId: String): Boolean {
        val currentBusinessId = authUtil.getCurrentBusinessIdSafe()
        return employeeDao.phoneNumberExists(currentBusinessId, phoneNumber, excludeEmployeeId)
    }
    
    /**
     * Checks if employee email already exists
     * BusinessId filtering applied automatically
     */
    override suspend fun emailExists(email: String, excludeEmployeeId: String): Boolean {
        val currentBusinessId = authUtil.getCurrentBusinessIdSafe()
        return employeeDao.emailExists(currentBusinessId, email, excludeEmployeeId)
    }
    
    /**
     * Gets total employee count
     * BusinessId filtering applied automatically
     */
    override suspend fun getEmployeeCount(): Int {
        val currentBusinessId = authUtil.getCurrentBusinessIdSafe()
        return employeeDao.getEmployeeCount(currentBusinessId)
    }
    
    /**
     * Gets employee count by status
     * BusinessId filtering applied automatically
     */
    override suspend fun getEmployeeCountByStatus(isActive: Boolean): Int {
        val currentBusinessId = authUtil.getCurrentBusinessIdSafe()
        return if (isActive) {
            employeeDao.getActiveEmployeeCount(currentBusinessId)
        } else {
            employeeDao.getEmployeeCount(currentBusinessId) - employeeDao.getActiveEmployeeCount(currentBusinessId)
        }
    }
    
    /**
     * Gets employees by hiring date range
     * BusinessId filtering applied automatically
     */
    override fun getEmployeesByHiringDateRange(startDate: String, endDate: String): Flow<List<Employee>> {
        val currentBusinessId = authUtil.getCurrentBusinessIdSafe()
        // For now, return all employees as the DAO method doesn't exist yet
        return getAllEmployees()
    }
    
    /**
     * Bulk update employee status
     * BusinessId filtering applied automatically
     */
    override suspend fun bulkUpdateEmployeeStatus(employeeIds: List<String>, isActive: Boolean): Result<Int> {
        return try {
            var updatedCount = 0
            for (employeeId in employeeIds) {
                val result = toggleEmployeeStatus(employeeId, isActive)
                if (result.isSuccess) {
                    updatedCount++
                }
            }
            Result.success(updatedCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Hard delete employee (for testing only)
     * BusinessId filtering applied automatically
     */
    override suspend fun hardDeleteEmployee(employeeId: String): Result<Unit> {
        return try {
            employeeDao.hardDeleteEmployee(employeeId)
            
            // Try to delete from Firestore
            try {
                firestore.collection(EMPLOYEES_COLLECTION)
                    .document(employeeId)
                    .delete()
                    .await()
            } catch (syncError: Exception) {
                // Local deletion succeeded, remote deletion failed - acceptable
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Performs manual sync with Firestore
     * BusinessId applied automatically
     */
    override suspend fun syncWithFirestore(): Result<Unit> {
        val currentBusinessId = authUtil.getCurrentBusinessIdSafe()
        return performComprehensiveSync(currentBusinessId)
    }
    
    /**
     * Performs initial sync when app starts
     * BusinessId applied automatically
     */
    override suspend fun performInitialSync(): Result<Unit> {
        return try {
            val currentBusinessId = authUtil.getCurrentBusinessIdSafe()
            syncFirestoreToLocal(currentBusinessId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Performs comprehensive bidirectional sync (private helper)
     */
    private suspend fun performComprehensiveSync(businessId: String): Result<Unit> {
        return try {
            
            // First push local changes
            syncLocalToFirestore(businessId)
            
            // Then pull fresh data
            syncFirestoreToLocal(businessId)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Private: Syncs local changes to Firestore
     * Memory efficient: batch operations for multiple entities
     */
    private suspend fun syncLocalToFirestore(businessId: String) {
        val employeesNeedingSync = employeeDao.getEmployeesNeedingSync(businessId)
        
        employeesNeedingSync.forEach { entity ->
            try {
                if (entity.isDeleted) {
                    // Delete from Firestore
                    firestore.collection(EMPLOYEES_COLLECTION)
                        .document(entity.id)
                        .delete()
                        .await()
                    
                    employeeDao.hardDeleteEmployee(entity.id)
                } else {
                    // Convert to Firestore model and sync
                    val employee = entity.toDomainModel()
                    val currentUser = firebaseAuth.currentUser
                    val firestoreEmployee = EmployeeFirestore.fromDomainModel(
                        employee = employee,
                        lastModifiedBy = currentUser?.uid ?: "unknown"
                    )
                    
                    firestore.collection(EMPLOYEES_COLLECTION)
                        .document(entity.id)
                        .set(firestoreEmployee)
                        .await()
                    
                    employeeDao.markAsSynced(entity.id)
                }
            } catch (e: Exception) {
                // Sync error - will retry later
            }
        }
    }
    
    /**
     * Private: Syncs Firestore changes to local database
     * Memory efficient: batch processing with minimal object creation
     */
    private suspend fun syncFirestoreToLocal(businessId: String) {
        try {
            
            val snapshot = firestore.collection(EMPLOYEES_COLLECTION)
                .whereEqualTo("businessId", businessId)
                .get()
                .await()
            
            
            val remoteEmployees = snapshot.documents.mapNotNull { doc ->
                try {
                    val firestoreEmployee = doc.toObject(EmployeeFirestore::class.java)
                    if (firestoreEmployee != null && !firestoreEmployee.isDeleted) {
                        firestoreEmployee.toDomainModel().copy(id = doc.id)
                    } else {
                        null
                    }
                } catch (e: Exception) {
                    null
                }
            }
            
            
            // Handle cross-device deletions
            val localEmployees = employeeDao.getAllEmployeesSync(businessId)
            val localEmployeeIds = localEmployees.map { it.id }.toSet()
            val remoteEmployeeIds = remoteEmployees.map { it.id }.toSet()
            
            val employeesToDelete = localEmployeeIds - remoteEmployeeIds
            if (employeesToDelete.isNotEmpty()) {
                employeesToDelete.forEach { employeeId ->
                    try {
                        employeeDao.hardDeleteEmployee(employeeId)
                    } catch (e: Exception) {
                    }
                }
            }
            
            // Update local database with remote data, but preserve local isActive values
            val entities = remoteEmployees.map { remoteEmployee ->
                // Check if employee exists locally
                val localEmployee = localEmployees.find { it.id == remoteEmployee.id }
                
                if (localEmployee != null) {
                    // Employee exists locally - preserve local isActive value
                    val preservedIsActive = localEmployee.isActive
                    val updatedEmployee = remoteEmployee.copy(isActive = preservedIsActive)
                    EmployeeEntity.fromDomainModel(updatedEmployee, needsSync = false)
                } else {
                    // New employee from Firestore - use remote isActive value
                    EmployeeEntity.fromDomainModel(remoteEmployee, needsSync = false)
                }
            }
            
            if (entities.isNotEmpty()) {
                employeeDao.insertEmployees(entities)
            }
            
            
        } catch (e: Exception) {
            throw e
        }
    }
} 