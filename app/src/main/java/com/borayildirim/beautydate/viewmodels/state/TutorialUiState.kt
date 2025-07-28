package com.borayildirim.beautydate.viewmodels.state

import com.borayildirim.beautydate.data.models.TutorialData
import com.borayildirim.beautydate.data.models.TutorialCategory
import com.borayildirim.beautydate.data.models.TutorialStep
import com.borayildirim.beautydate.data.models.TutorialPreferences
import com.borayildirim.beautydate.viewmodels.TutorialFilterType

/**
 * UI state for Tutorial screen with enhanced computed properties
 * Follows existing UiState pattern with immutable data classes
 * Memory efficient: minimal state with reactive computed properties
 */
data class TutorialUiState(
    // Loading states
    val isLoading: Boolean = false,
    val isInTutorialMode: Boolean = false,
    
    // Data state
    val allTutorials: List<TutorialData> = emptyList(),
    val filteredTutorials: List<TutorialData> = emptyList(),
    val recommendedTutorials: List<TutorialData> = emptyList(),
    val currentTutorial: TutorialData? = null,
    val currentStepIndex: Int = 0,
    
    // Filter states
    val selectedCategory: TutorialCategory? = null,
    val searchQuery: String = "",
    val filterType: TutorialFilterType = TutorialFilterType.ALL,
    
    // Preferences
    val tutorialPreferences: TutorialPreferences = TutorialPreferences(),
    
    // Messages
    val error: String? = null,
    val completionMessage: String? = null
) {
    /**
     * COMPUTED PROPERTIES - Added missing functionality
     */
    
    /**
     * Check if there's any data available
     */
    val hasData: Boolean
        get() = allTutorials.isNotEmpty()
    
    /**
     * Check if filters are active
     */
    val hasActiveFilters: Boolean
        get() = selectedCategory != null || searchQuery.isNotBlank() || filterType != TutorialFilterType.ALL
    
    /**
     * Get tutorials grouped by category
     */
    val tutorialsByCategory: Map<TutorialCategory, List<TutorialData>>
        get() = allTutorials.groupBy { it.category }
    
    /**
     * Get personalized recommendations based on user progress
     */
    val personalizedRecommendations: List<TutorialData>
        get() = recommendedTutorials.ifEmpty {
            // Fallback: show uncompleted beginner tutorials
            allTutorials.filter { 
                !it.isCompleted && it.difficulty == com.borayildirim.beautydate.data.models.TutorialDifficulty.BEGINNER
            }.take(3)
        }
    
    /**
     * Get tutorial statistics for progress tracking
     */
    val tutorialStatistics: TutorialStatistics
        get() {
            val total = allTutorials.size
            val completed = allTutorials.count { it.isCompleted }
            val inProgress = allTutorials.count { it.completionPercentage > 0 && !it.isCompleted }
            val progressPercentage = if (total > 0) (completed * 100 / total) else 0
            
            return TutorialStatistics(
                totalTutorials = total,
                completedTutorials = completed,
                inProgressTutorials = inProgress,
                overallProgress = progressPercentage,
                progressColor = when {
                    progressPercentage >= 80 -> 0xFF4CAF50 // Green
                    progressPercentage >= 50 -> 0xFF2196F3 // Blue  
                    progressPercentage >= 20 -> 0xFFFF9800 // Orange
                    else -> 0xFF9E9E9E // Gray
                },
                progressLevelText = when {
                    progressPercentage >= 80 -> "🏆 Tutorial Uzmanı"
                    progressPercentage >= 50 -> "🎯 İyi İlerleme"
                    progressPercentage >= 20 -> "📚 Öğrenmeye Devam"
                    else -> "🚀 Yeni Başlayan"
                }
            )
        }
    
    /**
     * Get current tutorial progress percentage
     */
    val tutorialProgress: Int
        get() {
            val tutorial = currentTutorial
            return if (tutorial != null && tutorial.steps.isNotEmpty()) {
                ((currentStepIndex + 1) * 100 / tutorial.steps.size)
            } else {
                0
            }
        }
    
    /**
     * Get progress text for current tutorial
     */
    val tutorialProgressText: String
        get() {
            val tutorial = currentTutorial
            return if (tutorial != null) {
                "Adım ${currentStepIndex + 1} / ${tutorial.steps.size} - ${tutorial.title}"
            } else {
                "Tutorial Yok"
            }
        }
    
    /**
     * Check if current step is the last step
     */
    val isLastStep: Boolean
        get() {
            val tutorial = currentTutorial
            return tutorial != null && currentStepIndex >= tutorial.steps.size - 1
        }
    
    /**
     * Get search suggestions based on tutorial content
     */
    val searchSuggestions: List<String>
        get() = allTutorials.flatMap { tutorial ->
            listOf(tutorial.title) + tutorial.steps.map { it.title }
        }.distinct().take(5)
    
    /**
     * Get completion rate for a specific category
     */
    fun getCategoryCompletionRate(category: TutorialCategory): Double {
        val categoryTutorials = allTutorials.filter { it.category == category }
        return if (categoryTutorials.isNotEmpty()) {
            val completed = categoryTutorials.count { it.isCompleted }
            (completed.toDouble() / categoryTutorials.size) * 100
        } else {
            0.0
        }
    }
    
    /**
     * Check if tutorial can be started (prerequisites met)
     */
    fun canStartTutorial(tutorialId: String): Boolean {
        val tutorial = allTutorials.find { it.id == tutorialId }
        return if (tutorial?.prerequisites?.isNotEmpty() == true) {
            tutorial.prerequisites.all { prereqId ->
                allTutorials.find { it.id == prereqId }?.isCompleted == true
            }
        } else {
            true
        }
    }
    
    /**
     * Get next recommended tutorial based on current progress
     */
    val nextRecommendedTutorial: TutorialData?
        get() {
            // Find first uncompleted tutorial that user can start
            return allTutorials.firstOrNull { tutorial ->
                !tutorial.isCompleted && canStartTutorial(tutorial.id)
            }
        }
    
    /**
     * Get filtered tutorials based on current filter state
     */
    val displayedTutorials: List<TutorialData>
        get() {
            return if (hasActiveFilters) {
                filteredTutorials
            } else {
                allTutorials
            }
        }
    
    /**
     * Check if tutorial mode is active and valid
     */
    val isValidTutorialMode: Boolean
        get() = isInTutorialMode && currentTutorial != null
}

/**
 * Tutorial statistics data class
 */
data class TutorialStatistics(
    val totalTutorials: Int,
    val completedTutorials: Int,
    val inProgressTutorials: Int,
    val overallProgress: Int,
    val progressColor: Long,
    val progressLevelText: String
) {
    val remainingTutorials: Int
        get() = totalTutorials - completedTutorials
    
    val formattedProgress: String
        get() = "$completedTutorials / $totalTutorials tamamlandı"
    
    val completionText: String
        get() = formattedProgress
    
    val formattedCompletionPercentage: String
        get() = "%$overallProgress"
    
    val completionPercentage: Int
        get() = overallProgress
    
    val availableTutorials: Int
        get() = totalTutorials
} 