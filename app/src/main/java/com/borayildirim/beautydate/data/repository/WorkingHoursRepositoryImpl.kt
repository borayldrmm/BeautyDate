package com.borayildirim.beautydate.data.repository

import com.borayildirim.beautydate.data.local.dao.WorkingHoursDao
import com.borayildirim.beautydate.data.local.entities.WorkingHoursEntity
import com.borayildirim.beautydate.data.models.DayHours
import com.borayildirim.beautydate.data.models.DayOfWeek
import com.borayildirim.beautydate.data.models.WorkingHours
import com.borayildirim.beautydate.data.remote.models.WorkingHoursFirestore
import com.borayildirim.beautydate.utils.NetworkMonitor
import com.borayildirim.beautydate.utils.AuthUtil
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of WorkingHoursRepository
 * Follows offline-first approach with Firestore sync
 * Multi-tenant architecture: All operations filtered by authenticated businessId
 * Memory efficient: minimal object creation and Flow-based reactive updates
 */
@Singleton
class WorkingHoursRepositoryImpl @Inject constructor(
    private val workingHoursDao: WorkingHoursDao,
    private val firestore: FirebaseFirestore,
    private val networkMonitor: NetworkMonitor,
    private val authUtil: AuthUtil
) : WorkingHoursRepository {

    companion object {
        private const val WORKING_HOURS_COLLECTION = "working_hours"
    }

    /**
     * Gets working hours as Flow for current authenticated business
     * BusinessId filtering applied automatically
     * Memory efficient: reactive single source of truth
     */
    override fun getWorkingHoursFlow(): Flow<WorkingHours?> {
        val currentBusinessId = authUtil.getCurrentBusinessIdSafe()
        return workingHoursDao.getWorkingHoursFlow(currentBusinessId).map { entity ->
            entity?.toWorkingHours()
        }
    }

    /**
     * Gets working hours for current authenticated business (one-time fetch)
     * BusinessId filtering applied automatically
     * Memory efficient: single database query
     */
    override suspend fun getWorkingHours(): Result<WorkingHours?> {
        return try {
            val currentBusinessId = authUtil.getCurrentBusinessIdSafe()
            val entity = workingHoursDao.getWorkingHours(currentBusinessId)
            val workingHours = entity?.toWorkingHours()
            
            // If no working hours exist, create default ones
            if (workingHours == null) {
                return createDefaultWorkingHours()
            }
            
            Result.success(workingHours)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Saves or updates working hours for current authenticated business
     * BusinessId assigned automatically
     * Memory efficient: single operation with optimistic updates
     */
    override suspend fun saveWorkingHours(workingHours: WorkingHours): Result<WorkingHours> {
        return try {
            val currentBusinessId = authUtil.getCurrentBusinessIdSafe()
            val updatedWorkingHours = workingHours.copy(
                businessId = currentBusinessId
            ).withUpdatedTimestamp()
            val entity = WorkingHoursEntity.fromWorkingHours(updatedWorkingHours)
            
            // Save locally first (offline-first)
            workingHoursDao.insertOrUpdateWorkingHours(entity)
            
            // Sync to remote if online
            if (networkMonitor.isCurrentlyConnected()) {
                try {
                    syncToRemote(updatedWorkingHours)
                } catch (e: Exception) {
                    // Local save succeeded, remote sync failed - will retry later
                }
            }
            
            Result.success(updatedWorkingHours)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Creates default working hours for current authenticated business
     * BusinessId assigned automatically
     * Memory efficient: single operation with pre-configured defaults
     */
    override suspend fun createDefaultWorkingHours(): Result<WorkingHours> {
        return try {
            val currentBusinessId = authUtil.getCurrentBusinessIdSafe()
            val defaultWorkingHours = WorkingHours.createDefault(currentBusinessId)
            saveWorkingHours(defaultWorkingHours)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Updates working hours for specific day in current authenticated business
     * BusinessId applied automatically
     * Memory efficient: partial update with single day modification
     */
    override suspend fun updateDayHours(
        dayOfWeek: DayOfWeek,
        dayHours: DayHours
    ): Result<WorkingHours> {
        return try {
            val currentBusinessId = authUtil.getCurrentBusinessIdSafe()
            val currentWorkingHours = workingHoursDao.getWorkingHours(currentBusinessId)?.toWorkingHours()
                ?: WorkingHours.createDefault(currentBusinessId)
            
            val updatedWorkingHours = when (dayOfWeek) {
                DayOfWeek.MONDAY -> currentWorkingHours.copy(monday = dayHours)
                DayOfWeek.TUESDAY -> currentWorkingHours.copy(tuesday = dayHours)
                DayOfWeek.WEDNESDAY -> currentWorkingHours.copy(wednesday = dayHours)
                DayOfWeek.THURSDAY -> currentWorkingHours.copy(thursday = dayHours)
                DayOfWeek.FRIDAY -> currentWorkingHours.copy(friday = dayHours)
                DayOfWeek.SATURDAY -> currentWorkingHours.copy(saturday = dayHours)
                DayOfWeek.SUNDAY -> currentWorkingHours.copy(sunday = dayHours)
            }
            
            saveWorkingHours(updatedWorkingHours)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Applies same working hours to all days for current authenticated business
     * BusinessId applied automatically
     * Memory efficient: bulk update with single operation
     */
    override suspend fun applyToAllDays(dayHours: DayHours): Result<WorkingHours> {
        return try {
            val currentBusinessId = authUtil.getCurrentBusinessIdSafe()
            val updatedWorkingHours = WorkingHours(
                businessId = currentBusinessId,
                monday = dayHours,
                tuesday = dayHours,
                wednesday = dayHours,
                thursday = dayHours,
                friday = dayHours,
                saturday = dayHours,
                sunday = dayHours
            )
            
            saveWorkingHours(updatedWorkingHours)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Resets working hours to default for current authenticated business
     * BusinessId applied automatically
     * Memory efficient: single operation with predefined values
     */
    override suspend fun resetToDefault(): Result<WorkingHours> {
        return createDefaultWorkingHours()
    }

    /**
     * Checks if business is open at specific time for current authenticated business
     * BusinessId applied automatically
     * Memory efficient: cached calculation with minimal object creation
     */
    override suspend fun isBusinessOpen(dayOfWeek: DayOfWeek, time: String): Boolean {
        return try {
            val currentBusinessId = authUtil.getCurrentBusinessIdSafe()
            val workingHours = workingHoursDao.getWorkingHours(currentBusinessId)?.toWorkingHours()
                ?: return false
            
            val dayHours = when (dayOfWeek) {
                DayOfWeek.MONDAY -> workingHours.monday
                DayOfWeek.TUESDAY -> workingHours.tuesday
                DayOfWeek.WEDNESDAY -> workingHours.wednesday
                DayOfWeek.THURSDAY -> workingHours.thursday
                DayOfWeek.FRIDAY -> workingHours.friday
                DayOfWeek.SATURDAY -> workingHours.saturday
                DayOfWeek.SUNDAY -> workingHours.sunday
            }
            
            dayHours.isWorking && time >= dayHours.startTime && time <= dayHours.endTime
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Gets next open time for current authenticated business
     * BusinessId applied automatically
     * Memory efficient: calculated result without unnecessary iterations
     */
    override suspend fun getNextOpenTime(): DayHours? {
        return try {
            val currentBusinessId = authUtil.getCurrentBusinessIdSafe()
            val workingHours = workingHoursDao.getWorkingHours(currentBusinessId)?.toWorkingHours()
                ?: return null
            
            // Find first open day
            listOf(
                workingHours.monday,
                workingHours.tuesday,
                workingHours.wednesday,
                workingHours.thursday,
                workingHours.friday,
                workingHours.saturday,
                workingHours.sunday
            ).firstOrNull { it.isWorking }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Syncs working hours with Firestore for current authenticated business
     * BusinessId applied automatically
     */
    override suspend fun syncWithFirestore(): Result<Unit> {
        return try {
            val currentBusinessId = authUtil.getCurrentBusinessIdSafe()
            syncFromRemote(currentBusinessId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Performs initial sync for current authenticated business
     * BusinessId applied automatically
     */
    override suspend fun performInitialSync(): Result<Unit> {
        return syncWithFirestore()
    }

    /**
     * Gets all working hours as Flow
     * Memory efficient: reactive list with entity mapping
     */
    override fun getAllWorkingHoursFlow(): Flow<List<WorkingHours>> {
        return workingHoursDao.getAllWorkingHoursFlow().map { entities ->
            entities.map { it.toWorkingHours() }
        }
    }

    /**
     * Syncs from remote to local
     * Memory efficient: single document fetch with local update
     */
    private suspend fun syncFromRemote(businessId: String) {
        val documentId = "working_hours_$businessId"
        val document = firestore.collection(WORKING_HOURS_COLLECTION)
            .document(documentId)
            .get()
            .await()

        if (document.exists()) {
            val firestoreWorkingHours = document.toObject(WorkingHoursFirestore::class.java)
            firestoreWorkingHours?.let {
                val workingHours = it.toWorkingHours()
                val entity = WorkingHoursEntity.fromWorkingHours(workingHours)
                workingHoursDao.insertOrUpdateWorkingHours(entity)
            }
        }
    }

    /**
     * Syncs local data to remote
     * Memory efficient: direct Firestore document update
     */
    private suspend fun syncToRemote(workingHours: WorkingHours) {
        val firestoreWorkingHours = WorkingHoursFirestore.fromWorkingHours(workingHours)
        val documentId = "working_hours_${workingHours.businessId}"
        
        firestore.collection(WORKING_HOURS_COLLECTION)
            .document(documentId)
            .set(firestoreWorkingHours.toMap())
            .await()
    }
} 