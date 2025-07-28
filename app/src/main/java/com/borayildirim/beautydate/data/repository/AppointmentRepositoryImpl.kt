package com.borayildirim.beautydate.data.repository

import com.borayildirim.beautydate.data.local.AppointmentEntity
import com.borayildirim.beautydate.data.local.dao.AppointmentDao
import com.borayildirim.beautydate.data.models.Appointment
import com.borayildirim.beautydate.data.models.AppointmentStatus
import com.borayildirim.beautydate.data.remote.models.AppointmentFirestore
import com.borayildirim.beautydate.utils.NetworkMonitor
import com.borayildirim.beautydate.utils.AuthUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of AppointmentRepository
 * Handles offline-first appointment data management with Firestore sync
 * Multi-tenant architecture: All operations filtered by authenticated businessId
 * Memory efficient: Flow-based reactive data with efficient caching
 */
@Singleton
class AppointmentRepositoryImpl @Inject constructor(
    private val appointmentDao: AppointmentDao,
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth,
    private val networkMonitor: NetworkMonitor,
    private val authUtil: AuthUtil
) : AppointmentRepository {
    
    companion object {
        private const val APPOINTMENTS_COLLECTION = "appointments"
    }
    
    /**
     * Gets all appointments for current authenticated business with reactive updates
     * Offline-first: always returns local data, syncs in background
     * BusinessId filtering applied automatically
     */
    override fun getAllAppointments(businessId: String): Flow<List<Appointment>> {
        // Use authenticated business ID, ignore passed parameter for security
        val currentBusinessId = authUtil.getCurrentBusinessIdSafe()
        return appointmentDao.getAllAppointments(currentBusinessId)
            .map { entities -> entities.map { it.toDomainModel() } }
    }
    
    /**
     * Gets appointments for a specific customer
     * BusinessId filtering applied automatically
     */
    override fun getAppointmentsByCustomer(customerId: String, businessId: String): Flow<List<Appointment>> {
        val currentBusinessId = authUtil.getCurrentBusinessIdSafe()
        return appointmentDao.getAppointmentsByCustomer(customerId, currentBusinessId)
            .map { entities -> entities.map { it.toDomainModel() } }
    }
    
    /**
     * Gets appointments by status
     * BusinessId filtering applied automatically
     */
    override fun getAppointmentsByStatus(status: AppointmentStatus, businessId: String): Flow<List<Appointment>> {
        val currentBusinessId = authUtil.getCurrentBusinessIdSafe()
        return appointmentDao.getAppointmentsByStatus(status.name, currentBusinessId)
            .map { entities -> entities.map { it.toDomainModel() } }
    }
    
    /**
     * Gets appointments for a specific date
     * BusinessId filtering applied automatically
     */
    override fun getAppointmentsByDate(date: String, businessId: String): Flow<List<Appointment>> {
        val currentBusinessId = authUtil.getCurrentBusinessIdSafe()
        return appointmentDao.getAppointmentsByDate(date, currentBusinessId)
            .map { entities -> entities.map { it.toDomainModel() } }
    }
    
    /**
     * Gets appointments for a specific date (synchronous)
     * BusinessId filtering applied automatically
     */
    override suspend fun getAppointmentsByDateSync(date: String, businessId: String): List<Appointment> {
        val currentBusinessId = authUtil.getCurrentBusinessIdSafe()
        return appointmentDao.getAppointmentsByDate(date, currentBusinessId)
            .map { entities -> entities.map { it.toDomainModel() } }
            .first() // Convert Flow to List synchronously
    }
    
    /**
     * Gets appointments for a date range
     * BusinessId filtering applied automatically
     */
    override fun getAppointmentsByDateRange(startDate: String, endDate: String, businessId: String): Flow<List<Appointment>> {
        val currentBusinessId = authUtil.getCurrentBusinessIdSafe()
        return appointmentDao.getAppointmentsByDateRange(startDate, endDate, currentBusinessId)
            .map { entities -> entities.map { it.toDomainModel() } }
    }
    
    /**
     * Search appointments by customer name or phone
     * BusinessId filtering applied automatically
     */
    override fun searchAppointments(query: String, businessId: String): Flow<List<Appointment>> {
        val currentBusinessId = authUtil.getCurrentBusinessIdSafe()
        return if (query.isBlank()) {
            getAllAppointments(businessId)
        } else {
            appointmentDao.searchAppointments(currentBusinessId, query)
                .map { entities -> entities.map { it.toDomainModel() } }
        }
    }
    
    /**
     * Gets appointment by ID with businessId validation
     * Ensures appointment belongs to current authenticated business
     */
    override suspend fun getAppointmentById(appointmentId: String): Appointment? {
        val businessId = authUtil.getCurrentBusinessId() ?: return null
        val appointment = appointmentDao.getAppointmentById(appointmentId)?.toDomainModel()
        
        // Validate that appointment belongs to current business
        return if (appointment?.businessId == businessId) {
            appointment
        } else {
            null // Appointment doesn't belong to this business
        }
    }
    
    /**
     * Adds new appointment with automatic businessId assignment
     * BusinessId is automatically set from current authenticated user
     */
    override suspend fun addAppointment(appointment: Appointment): Result<Unit> {
        return try {
            val businessId = authUtil.getCurrentBusinessId()
                ?: return Result.failure(Exception(authUtil.getAuthErrorMessage()))
            
            // Automatically set businessId for new appointment
            val newAppointment = appointment.copy(
                id = if (appointment.id.isEmpty()) Appointment.generateAppointmentId() else appointment.id,
                businessId = businessId, // Automatically set from authenticated user
                createdAt = com.google.firebase.Timestamp.now(),
                updatedAt = com.google.firebase.Timestamp.now()
            )
            
            val entity = AppointmentEntity.fromDomainModel(newAppointment, needsSync = true)
            appointmentDao.insertAppointment(entity)
            
            // Automatic background sync if network available
            if (networkMonitor.isCurrentlyConnected()) {
                try {
                    syncAppointmentToFirestore(newAppointment)
                    appointmentDao.markAsSynced(newAppointment.id)
                } catch (syncError: Exception) {
                    // Don't fail the operation - will sync when network is available
                }
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Updates existing appointment with businessId validation
     * Ensures appointment belongs to current authenticated business
     */
    override suspend fun updateAppointment(appointment: Appointment): Result<Unit> {
        return try {
            val businessId = authUtil.getCurrentBusinessId()
                ?: return Result.failure(Exception(authUtil.getAuthErrorMessage()))
            
            // Validate that appointment belongs to current business
            if (appointment.businessId != businessId) {
                return Result.failure(Exception(authUtil.getTenantErrorMessage()))
            }
            
            val updatedAppointment = appointment.copy(updatedAt = com.google.firebase.Timestamp.now())
            val entity = AppointmentEntity.fromDomainModel(updatedAppointment, needsSync = true)
            
            appointmentDao.updateAppointment(entity)
            
            // Automatic background sync if network available
            if (networkMonitor.isCurrentlyConnected()) {
                try {
                    syncAppointmentToFirestore(updatedAppointment)
                    appointmentDao.markAsSynced(updatedAppointment.id)
                } catch (syncError: Exception) {
                    // Will sync later
                }
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Updates appointment status with businessId validation
     * Ensures appointment belongs to current authenticated business
     */
    override suspend fun updateAppointmentStatus(appointmentId: String, status: AppointmentStatus): Result<Unit> {
        return try {
            val businessId = authUtil.getCurrentBusinessId()
                ?: return Result.failure(Exception(authUtil.getAuthErrorMessage()))
            
            // Get appointment to validate businessId
            val appointment = appointmentDao.getAppointmentById(appointmentId)?.toDomainModel()
            if (appointment?.businessId != businessId) {
                return Result.failure(Exception(authUtil.getTenantErrorMessage()))
            }
            
            appointmentDao.updateAppointmentStatus(appointmentId, status.name)
            
            // Sync status update to Firestore
            if (networkMonitor.isCurrentlyConnected()) {
                try {
                    val updatedAppointment = appointment.copy(
                        status = status,
                        updatedAt = com.google.firebase.Timestamp.now()
                    )
                    syncAppointmentToFirestore(updatedAppointment)
                } catch (syncError: Exception) {
                    // Will sync later
                }
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Deletes appointment with businessId validation
     * Ensures appointment belongs to current authenticated business
     */
    override suspend fun deleteAppointment(appointmentId: String): Result<Unit> {
        return try {
            val businessId = authUtil.getCurrentBusinessId()
                ?: return Result.failure(Exception(authUtil.getAuthErrorMessage()))
            
            // Get appointment to validate businessId
            val appointment = appointmentDao.getAppointmentById(appointmentId)?.toDomainModel()
            if (appointment?.businessId != businessId) {
                return Result.failure(Exception(authUtil.getTenantErrorMessage()))
            }
            
            // Soft delete - mark as deleted
            appointmentDao.deleteAppointment(appointmentId)
            
            if (networkMonitor.isCurrentlyConnected()) {
                try {
                    firestore.collection(APPOINTMENTS_COLLECTION)
                        .document(appointmentId)
                        .delete()
                        .await()
                } catch (syncError: Exception) {
                    // Will be synced later when network is available
                }
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Syncs appointments with Firestore for current business
     * BusinessId filtering applied automatically
     */
    override suspend fun syncWithFirestore(businessId: String): Result<Unit> {
        return try {
            val currentBusinessId = authUtil.getCurrentBusinessId()
                ?: return Result.failure(Exception(authUtil.getAuthErrorMessage()))
            
            
            if (!networkMonitor.isCurrentlyConnected()) {
                return Result.failure(Exception("No network connection"))
            }
            
            // Get local appointments needing sync for current business
            val localAppointments = appointmentDao.getAppointmentsNeedingSync(currentBusinessId)
            
            // Upload local changes to Firestore
            localAppointments.forEach { entity ->
                val appointment = entity.toDomainModel()
                syncAppointmentToFirestore(appointment)
                appointmentDao.markAsSynced(entity.id)
            }
            
            // Download remote appointments for current business only
            
            // TEST 1: Try businessId only (no isDeleted filter)
            val testQuery1 = firestore.collection(APPOINTMENTS_COLLECTION)
                .whereEqualTo("businessId", currentBusinessId)
                .get()
                .await()
            
                            // Try no filters at all
            val testQuery2 = firestore.collection(APPOINTMENTS_COLLECTION)
                .limit(5) // Limit for safety
                .get()
                .await()
            
            // TEMPORARY FIX: Remove isDeleted filter to enable cross-device sync
            // Original compound query (commented out due to isDeleted field issue)
            /*
            val remoteAppointments = firestore.collection(APPOINTMENTS_COLLECTION)
                .whereEqualTo("businessId", currentBusinessId) // BusinessId filtering
                .whereEqualTo("isDeleted", false)
                .get()
                .await()
            */
            
            // WORKING QUERY: Only businessId filter (isDeleted filter removed temporarily)
            val remoteAppointments = firestore.collection(APPOINTMENTS_COLLECTION)
                .whereEqualTo("businessId", currentBusinessId) // BusinessId filtering only
                .get()
                .await()
            
            
            val remoteEntities = remoteAppointments.documents.mapNotNull { doc ->
                doc.toObject(AppointmentFirestore::class.java)?.let { firestore ->
                    val appointment = firestore.toDomainModel()
                    AppointmentEntity.fromDomainModel(appointment, needsSync = false)
                }
            }
            
            // Save remote appointments to local database
            if (remoteEntities.isNotEmpty()) {
                appointmentDao.insertAppointments(remoteEntities)
            } else {
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
    
    /**
     * Gets appointment statistics for current business
     * BusinessId filtering applied automatically
     */
    override suspend fun getAppointmentStatistics(businessId: String): Map<AppointmentStatus, Int> {
        val currentBusinessId = authUtil.getCurrentBusinessIdSafe()
        return try {
            // Get counts for each status
            val scheduled = appointmentDao.getAppointmentCountByStatus(currentBusinessId, AppointmentStatus.SCHEDULED.name)
            val completed = appointmentDao.getAppointmentCountByStatus(currentBusinessId, AppointmentStatus.COMPLETED.name)
            val cancelled = appointmentDao.getAppointmentCountByStatus(currentBusinessId, AppointmentStatus.CANCELLED.name)
            val noShow = appointmentDao.getAppointmentCountByStatus(currentBusinessId, AppointmentStatus.NO_SHOW.name)
            
            mapOf(
                AppointmentStatus.SCHEDULED to scheduled,
                AppointmentStatus.COMPLETED to completed,
                AppointmentStatus.CANCELLED to cancelled,
                AppointmentStatus.NO_SHOW to noShow
            )
        } catch (e: Exception) {
            // Return empty map on error
            mapOf(
                AppointmentStatus.SCHEDULED to 0,
                AppointmentStatus.COMPLETED to 0,
                AppointmentStatus.CANCELLED to 0,
                AppointmentStatus.NO_SHOW to 0
            )
        }
    }
    
    /**
     * Gets appointment count for current business
     * BusinessId filtering applied automatically
     */
    override suspend fun getAppointmentCount(businessId: String): Int {
        val currentBusinessId = authUtil.getCurrentBusinessIdSafe()
        return appointmentDao.getAppointmentCount(currentBusinessId)
    }
    
    /**
     * Private helper: Syncs single appointment to Firestore with businessId
     * Used internally for upload operations
     */
    private suspend fun syncAppointmentToFirestore(appointment: Appointment) {
        val firestoreAppointment = AppointmentFirestore.fromDomainModel(
            appointment,
            lastModifiedBy = authUtil.getCurrentBusinessIdSafe()
        )
        
        firestore.collection(APPOINTMENTS_COLLECTION)
            .document(appointment.id)
            .set(firestoreAppointment)
            .await()
    }
}

