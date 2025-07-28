package com.borayildirim.beautydate.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.borayildirim.beautydate.data.models.TutorialData
import com.borayildirim.beautydate.data.models.TutorialCategory
import com.borayildirim.beautydate.data.models.TutorialStep
import com.borayildirim.beautydate.data.repository.TutorialRepository
import com.borayildirim.beautydate.viewmodels.state.TutorialUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.coroutines.delay

/**
 * ViewModel for Tutorial system
 * Follows MVVM pattern with Hilt DI and Clean Architecture
 * Memory efficient: StateFlow for UI state management
 */
@HiltViewModel
class TutorialViewModel @Inject constructor(
    private val tutorialRepository: TutorialRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(TutorialUiState())
    val uiState: StateFlow<TutorialUiState> = _uiState.asStateFlow()
    
    init {
        loadTutorials()
        loadTutorialPreferences()
    }
    
    /**
     * Loads all tutorials
     */
    private fun loadTutorials() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                tutorialRepository.getAllTutorials()
                    .catch { exception ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = exception.message ?: "Tutorial'lar yüklenirken hata oluştu"
                        )
                    }
                    .collect { tutorials ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            allTutorials = tutorials,
                            filteredTutorials = filterTutorials(tutorials),
                            error = null
                        )
                    }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Tutorial'lar yüklenirken hata oluştu"
                )
            }
        }
    }
    
    /**
     * Loads tutorial preferences
     */
    private fun loadTutorialPreferences() {
        viewModelScope.launch {
            try {
                tutorialRepository.getTutorialPreferences()
                    .catch { exception ->
                        // Handle preferences loading error silently
                    }
                    .collect { preferences ->
                        _uiState.value = _uiState.value.copy(
                            tutorialPreferences = preferences
                        )
                    }
            } catch (e: Exception) {
                // Handle error silently for preferences
            }
        }
    }
    
    /**
     * Loads recommended tutorials
     */
    fun loadRecommendedTutorials() {
        viewModelScope.launch {
            try {
                tutorialRepository.getRecommendedTutorials()
                    .catch { exception ->
                        // Handle error silently
                    }
                    .collect { recommendations ->
                        _uiState.value = _uiState.value.copy(
                            recommendedTutorials = recommendations
                        )
                    }
            } catch (e: Exception) {
                // Handle error silently
            }
        }
    }
    
    /**
     * Filters tutorials by category and search query
     */
    private fun filterTutorials(tutorials: List<TutorialData>): List<TutorialData> {
        val currentState = _uiState.value
        var filtered = tutorials
        
        // Filter by category
        if (currentState.selectedCategory != null) {
            filtered = filtered.filter { it.category == currentState.selectedCategory }
        }
        
        // Filter by search query
        if (currentState.searchQuery.isNotBlank()) {
            filtered = filtered.filter { tutorial ->
                tutorial.title.contains(currentState.searchQuery, ignoreCase = true) ||
                tutorial.description.contains(currentState.searchQuery, ignoreCase = true)
            }
        }
        
        // Filter by completion status
        when (currentState.filterType) {
            TutorialFilterType.ALL -> { /* No additional filtering */ }
            TutorialFilterType.COMPLETED -> filtered = filtered.filter { it.isCompleted }
            TutorialFilterType.AVAILABLE -> filtered = filtered.filter { !it.isCompleted }
            TutorialFilterType.RECOMMENDED -> filtered = currentState.recommendedTutorials
        }
        
        return filtered
    }
    
    /**
     * Changes selected category filter
     */
    fun selectCategory(category: TutorialCategory?) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
        updateFilteredTutorials()
    }
    
    /**
     * Changes search query
     */
    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        updateFilteredTutorials()
    }
    
    /**
     * Changes filter type
     */
    fun selectFilterType(filterType: TutorialFilterType) {
        _uiState.value = _uiState.value.copy(filterType = filterType)
        updateFilteredTutorials()
    }
    
    /**
     * Updates filtered tutorials based on current filters
     */
    private fun updateFilteredTutorials() {
        val filtered = filterTutorials(_uiState.value.allTutorials)
        _uiState.value = _uiState.value.copy(filteredTutorials = filtered)
    }
    
    /**
     * FIXED: Starts a specific tutorial with proper state management
     */
    fun startTutorial(tutorialId: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                
                val tutorial = tutorialRepository.getTutorialById(tutorialId)
                if (tutorial != null) {
                    _uiState.value = _uiState.value.copy(
                        currentTutorial = tutorial,
                        currentStepIndex = 0,
                        isInTutorialMode = true,
                        isLoading = false,
                        error = null
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Tutorial bulunamadı"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Tutorial başlatılırken hata oluştu: ${e.message}"
                )
            }
        }
    }
    
    /**
     * FIXED: Enhanced step navigation with proper validation
     */
    fun nextTutorialStep() {
        val currentState = _uiState.value
        val tutorial = currentState.currentTutorial
        
        if (tutorial == null) {
            _uiState.value = _uiState.value.copy(
                error = "Aktif tutorial bulunamadı"
            )
            return
        }
        
        val currentStepIndex = currentState.currentStepIndex
        val totalSteps = tutorial.steps.size
        
        if (currentStepIndex < totalSteps - 1) {
            // Move to next step
            val newIndex = currentStepIndex + 1
            _uiState.value = _uiState.value.copy(
                currentStepIndex = newIndex
            )
            
            // Mark current step as completed (async)
            viewModelScope.launch {
                try {
                    val currentStep = tutorial.steps[currentStepIndex]
                    tutorialRepository.markStepCompleted(tutorial.id, currentStep.id)
                } catch (e: Exception) {
                    // Silent failure for step completion marking
                }
            }
        } else {
            // Tutorial completed
            completeTutorial()
        }
    }
    
    /**
     * FIXED: Enhanced previous step navigation
     */
    fun previousTutorialStep() {
        val currentState = _uiState.value
        
        if (currentState.currentStepIndex > 0) {
            val newIndex = currentState.currentStepIndex - 1
            
            _uiState.value = _uiState.value.copy(
                currentStepIndex = newIndex
            )
        }
    }
    
    /**
     * FIXED: Enhanced tutorial completion with better state management
     */
    fun completeTutorial() {
        val currentTutorial = _uiState.value.currentTutorial
        
        if (currentTutorial != null) {
            viewModelScope.launch {
                try {
                    // Mark tutorial as completed
                    tutorialRepository.markTutorialCompleted(currentTutorial.id)
                    
                    // Update UI state
                    _uiState.value = _uiState.value.copy(
                        isInTutorialMode = false,
                        currentTutorial = null,
                        currentStepIndex = 0,
                        completionMessage = "🎉 Harika! '${currentTutorial.title}' tutorial'ını tamamladınız!"
                    )
                    
                    // Refresh tutorials to show updated completion status
                    delay(1500) // Show completion message for a while
                    loadTutorials()
                    
                    // Clear completion message after delay
                    delay(3000)
                    _uiState.value = _uiState.value.copy(completionMessage = null)
                    
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(
                        error = "Tutorial tamamlanırken hata oluştu: ${e.message}"
                    )
                }
            }
        }
    }
    
    /**
     * FIXED: Enhanced skip functionality with confirmation
     */
    fun skipTutorial() {
        val currentTutorial = _uiState.value.currentTutorial
        
        if (currentTutorial != null) {
            viewModelScope.launch {
                try {
                    // Mark tutorial as skipped
                    tutorialRepository.markTutorialSkipped(currentTutorial.id)
                    
                    // Update UI state
                    _uiState.value = _uiState.value.copy(
                        isInTutorialMode = false,
                        currentTutorial = null,
                        currentStepIndex = 0,
                        completionMessage = "Tutorial atlandı"
                    )
                    
                    // Refresh tutorials
                    loadTutorials()
                    
                    // Clear message after delay
                    delay(2000)
                    _uiState.value = _uiState.value.copy(completionMessage = null)
                    
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(
                        error = "Tutorial atlanırken hata oluştu: ${e.message}"
                    )
                }
            }
        }
    }
    
    /**
     * FIXED: Proper exit handling
     */
    fun exitTutorial() {
        _uiState.value = _uiState.value.copy(
            isInTutorialMode = false,
            currentTutorial = null,
            currentStepIndex = 0
        )
    }
    
    /**
     * Marks a specific step as completed
     */
    private fun markStepCompleted(tutorialId: String, stepId: String) {
        viewModelScope.launch {
            try {
                tutorialRepository.markStepCompleted(tutorialId, stepId)
            } catch (e: Exception) {
                // Handle error silently for step completion
            }
        }
    }
    
    /**
     * NEW: Enhanced navigation validation
     */
    fun canGoToPreviousStep(): Boolean {
        return _uiState.value.currentStepIndex > 0
    }
    
    /**
     * NEW: Gets current step with validation
     */
    fun getCurrentStep(): TutorialStep? {
        val state = _uiState.value
        val tutorial = state.currentTutorial
        val stepIndex = state.currentStepIndex
        
        return if (tutorial != null && stepIndex >= 0 && stepIndex < tutorial.steps.size) {
            tutorial.steps[stepIndex]
        } else {
            null
        }
    }
    
    /**
     * ENHANCED: Improved progress calculation
     */
    private fun calculateTutorialProgress(): Int {
        val state = _uiState.value
        val tutorial = state.currentTutorial
        val currentStep = state.currentStepIndex
        
        return if (tutorial != null && tutorial.steps.isNotEmpty()) {
            ((currentStep + 1).toDouble() / tutorial.steps.size * 100).toInt()
        } else {
            0
        }
    }
    
    /**
     * NEW: Reset specific tutorial progress
     */
    fun resetTutorialProgress(tutorialId: String) {
        viewModelScope.launch {
            try {
                tutorialRepository.resetTutorialProgress(tutorialId)
                loadTutorials() // Refresh to show updated status
                _uiState.value = _uiState.value.copy(
                    completionMessage = "Tutorial ilerlmesi sıfırlandı"
                )
                
                // Clear message after delay
                delay(2000)
                _uiState.value = _uiState.value.copy(completionMessage = null)
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "İlerleme sıfırlanırken hata oluştu: ${e.message}"
                )
            }
        }
    }
    
    /**
     * NEW: Reset all tutorial progress
     */
    fun resetAllTutorialProgress() {
        viewModelScope.launch {
            try {
                tutorialRepository.resetAllProgress()
                loadTutorials() // Refresh to show updated status
                _uiState.value = _uiState.value.copy(
                    completionMessage = "Tüm tutorial ilerlemesi sıfırlandı"
                )
                
                // Clear message after delay
                delay(2000)
                _uiState.value = _uiState.value.copy(completionMessage = null)
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "İlerleme sıfırlanırken hata oluştu: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Updates tutorial preferences
     */
    fun updateTutorialPreferences(preferences: com.borayildirim.beautydate.data.models.TutorialPreferences) {
        viewModelScope.launch {
            try {
                tutorialRepository.updateTutorialPreferences(preferences)
                _uiState.value = _uiState.value.copy(
                    tutorialPreferences = preferences
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Ayarlar güncellenirken hata oluştu: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Clears error message
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    /**
     * Clears completion message
     */
    fun clearCompletionMessage() {
        _uiState.value = _uiState.value.copy(completionMessage = null)
    }
    
    /**
     * Checks if tutorial can go to next step
     */
    fun canGoToNextStep(): Boolean {
        val currentState = _uiState.value
        val tutorial = currentState.currentTutorial
        
        return tutorial != null && currentState.currentStepIndex < tutorial.steps.size - 1
    }
}

/**
 * Tutorial filter types
 */
enum class TutorialFilterType(val displayName: String) {
    ALL("Tümü"),
    COMPLETED("Tamamlanan"),
    AVAILABLE("Mevcut"),
    RECOMMENDED("Önerilen")
} 