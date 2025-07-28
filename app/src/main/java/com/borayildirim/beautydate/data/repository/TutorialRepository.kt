package com.borayildirim.beautydate.data.repository

import com.borayildirim.beautydate.data.models.TutorialData
import com.borayildirim.beautydate.data.models.TutorialCategory
import com.borayildirim.beautydate.data.models.TutorialStep
import kotlinx.coroutines.flow.Flow

/**
 * Tutorial repository interface
 * Handles tutorial data management and user progress tracking
 * Memory efficient: Flow-based reactive data access
 */
interface TutorialRepository {
    
    /**
     * Gets all available tutorials
     */
    fun getAllTutorials(): Flow<List<TutorialData>>
    
    /**
     * Gets tutorials by category
     */
    fun getTutorialsByCategory(category: TutorialCategory): Flow<List<TutorialData>>
    
    /**
     * Gets tutorial by ID
     */
    suspend fun getTutorialById(id: String): TutorialData?
    
    /**
     * Gets completed tutorials
     */
    fun getCompletedTutorials(): Flow<List<TutorialData>>
    
    /**
     * Gets available/uncompleted tutorials
     */
    fun getAvailableTutorials(): Flow<List<TutorialData>>
    
    /**
     * Gets recommended tutorials for user
     */
    fun getRecommendedTutorials(): Flow<List<TutorialData>>
    
    /**
     * Gets tutorial steps for a specific tutorial
     */
    suspend fun getTutorialSteps(tutorialId: String): List<TutorialStep>
    
    /**
     * Marks tutorial step as completed
     */
    suspend fun markStepCompleted(tutorialId: String, stepId: String): Result<Unit>
    
    /**
     * Marks entire tutorial as completed
     */
    suspend fun markTutorialCompleted(tutorialId: String): Result<Unit>
    
    /**
     * Marks tutorial as skipped
     */
    suspend fun markTutorialSkipped(tutorialId: String): Result<Unit>
    
    /**
     * Resets tutorial progress
     */
    suspend fun resetTutorialProgress(tutorialId: String): Result<Unit>
    
    /**
     * Resets all tutorial progress
     */
    suspend fun resetAllProgress(): Result<Unit>
    
    /**
     * Gets tutorial completion status
     */
    suspend fun getTutorialCompletionStatus(tutorialId: String): Boolean
    
    /**
     * Gets overall tutorial progress percentage
     */
    fun getTutorialProgress(): Flow<Double>
    
    /**
     * Gets tutorial preferences
     */
    fun getTutorialPreferences(): Flow<com.borayildirim.beautydate.data.models.TutorialPreferences>
    
    /**
     * Updates tutorial preferences
     */
    suspend fun updateTutorialPreferences(preferences: com.borayildirim.beautydate.data.models.TutorialPreferences): Result<Unit>
    
    /**
     * Creates default tutorial data
     */
    suspend fun initializeDefaultTutorials(): Result<Unit>
    
    /**
     * Searches tutorials by query
     */
    fun searchTutorials(query: String): Flow<List<TutorialData>>
    
    /**
     * Gets tutorial statistics
     */
    suspend fun getTutorialStatistics(): TutorialStatistics
}

/**
 * Tutorial statistics data class
 */
data class TutorialStatistics(
    val totalTutorials: Int = 0,
    val completedTutorials: Int = 0,
    val skippedTutorials: Int = 0,
    val inProgressTutorials: Int = 0,
    val totalSteps: Int = 0,
    val completedSteps: Int = 0,
    val averageCompletionTime: Int = 0, // minutes
    val mostPopularCategory: TutorialCategory? = null,
    val completionRate: Double = 0.0
) {
    
    /**
     * Returns completion percentage
     */
    val completionPercentage: Double
        get() = if (totalTutorials > 0) (completedTutorials.toDouble() / totalTutorials) * 100 else 0.0
    
    /**
     * Returns steps completion percentage
     */
    val stepsCompletionPercentage: Double
        get() = if (totalSteps > 0) (completedSteps.toDouble() / totalSteps) * 100 else 0.0
    
    /**
     * Returns formatted completion text
     */
    val completionText: String
        get() = "$completedTutorials / $totalTutorials tutorial tamamlandı"
    
    /**
     * Returns formatted steps text
     */
    val stepsText: String
        get() = "$completedSteps / $totalSteps adım tamamlandı"
} 