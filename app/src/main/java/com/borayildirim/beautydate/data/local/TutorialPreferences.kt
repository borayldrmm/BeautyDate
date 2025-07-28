package com.borayildirim.beautydate.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.borayildirim.beautydate.data.models.TutorialSpeed
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Tutorial preferences management using DataStore
 * Handles tutorial progress tracking and user preferences
 * Memory efficient: lightweight DataStore operations
 */
@Singleton
class TutorialPreferences @Inject constructor(
    private val context: Context
) {
    companion object {
        private val Context.tutorialDataStore: DataStore<Preferences> by preferencesDataStore(name = "tutorial_preferences")
        
        // Preference keys
        private val HAS_SEEN_WELCOME = booleanPreferencesKey("has_seen_welcome")
        private val HAS_COMPLETED_ONBOARDING = booleanPreferencesKey("has_completed_onboarding")
        private val COMPLETED_TUTORIALS = stringSetPreferencesKey("completed_tutorials")
        private val SKIPPED_TUTORIALS = stringSetPreferencesKey("skipped_tutorials")
        private val SHOW_HINTS = booleanPreferencesKey("show_hints")
        private val AUTO_PLAY_TUTORIALS = booleanPreferencesKey("auto_play_tutorials")
        private val TUTORIAL_SPEED = stringPreferencesKey("tutorial_speed")
        private val ENABLE_GUIDED_TOUR = booleanPreferencesKey("enable_guided_tour")
        private val ENABLE_TOOLTIPS = booleanPreferencesKey("enable_tooltips")
        private val ENABLE_HELP_BADGES = booleanPreferencesKey("enable_help_badges")
    }
    
    private val dataStore = context.tutorialDataStore
    
    /**
     * Gets welcome screen seen status
     */
    val hasSeenWelcome: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[HAS_SEEN_WELCOME] ?: false
    }
    
    /**
     * Gets onboarding completion status
     */
    val hasCompletedOnboarding: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[HAS_COMPLETED_ONBOARDING] ?: false
    }
    
    /**
     * Gets completed tutorials list
     */
    val completedTutorials: Flow<Set<String>> = dataStore.data.map { preferences ->
        preferences[COMPLETED_TUTORIALS] ?: emptySet()
    }
    
    /**
     * Gets skipped tutorials list
     */
    val skippedTutorials: Flow<Set<String>> = dataStore.data.map { preferences ->
        preferences[SKIPPED_TUTORIALS] ?: emptySet()
    }
    
    /**
     * Gets hints visibility preference
     */
    val showHints: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[SHOW_HINTS] ?: true
    }
    
    /**
     * Gets auto-play tutorials preference
     */
    val autoPlayTutorials: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[AUTO_PLAY_TUTORIALS] ?: false
    }
    
    /**
     * Gets tutorial speed preference
     */
    val tutorialSpeed: Flow<TutorialSpeed> = dataStore.data.map { preferences ->
        val speedName = preferences[TUTORIAL_SPEED] ?: TutorialSpeed.NORMAL.name
        try {
            TutorialSpeed.valueOf(speedName)
        } catch (e: IllegalArgumentException) {
            TutorialSpeed.NORMAL
        }
    }
    
    /**
     * Gets guided tour enabled status
     */
    val enableGuidedTour: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[ENABLE_GUIDED_TOUR] ?: true
    }
    
    /**
     * Gets tooltips enabled status
     */
    val enableTooltips: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[ENABLE_TOOLTIPS] ?: true
    }
    
    /**
     * Gets help badges enabled status
     */
    val enableHelpBadges: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[ENABLE_HELP_BADGES] ?: true
    }
    
    /**
     * Sets welcome screen as seen
     */
    suspend fun setWelcomeSeen(seen: Boolean) {
        dataStore.edit { preferences ->
            preferences[HAS_SEEN_WELCOME] = seen
        }
    }
    
    /**
     * Sets onboarding as completed
     */
    suspend fun setOnboardingCompleted(completed: Boolean) {
        dataStore.edit { preferences ->
            preferences[HAS_COMPLETED_ONBOARDING] = completed
        }
    }
    
    /**
     * Marks tutorial as completed
     */
    suspend fun markTutorialCompleted(tutorialId: String) {
        dataStore.edit { preferences ->
            val currentCompleted = preferences[COMPLETED_TUTORIALS] ?: emptySet()
            val currentSkipped = preferences[SKIPPED_TUTORIALS] ?: emptySet()
            
            preferences[COMPLETED_TUTORIALS] = currentCompleted + tutorialId
            preferences[SKIPPED_TUTORIALS] = currentSkipped - tutorialId
        }
    }
    
    /**
     * Marks tutorial as skipped
     */
    suspend fun markTutorialSkipped(tutorialId: String) {
        dataStore.edit { preferences ->
            val currentCompleted = preferences[COMPLETED_TUTORIALS] ?: emptySet()
            val currentSkipped = preferences[SKIPPED_TUTORIALS] ?: emptySet()
            
            preferences[SKIPPED_TUTORIALS] = currentSkipped + tutorialId
            preferences[COMPLETED_TUTORIALS] = currentCompleted - tutorialId
        }
    }
    
    /**
     * Sets hints visibility
     */
    suspend fun setShowHints(show: Boolean) {
        dataStore.edit { preferences ->
            preferences[SHOW_HINTS] = show
        }
    }
    
    /**
     * Sets auto-play tutorials preference
     */
    suspend fun setAutoPlayTutorials(autoPlay: Boolean) {
        dataStore.edit { preferences ->
            preferences[AUTO_PLAY_TUTORIALS] = autoPlay
        }
    }
    
    /**
     * Sets tutorial speed
     */
    suspend fun setTutorialSpeed(speed: TutorialSpeed) {
        dataStore.edit { preferences ->
            preferences[TUTORIAL_SPEED] = speed.name
        }
    }
    
    /**
     * Sets guided tour enabled status
     */
    suspend fun setGuidedTourEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[ENABLE_GUIDED_TOUR] = enabled
        }
    }
    
    /**
     * Sets tooltips enabled status
     */
    suspend fun setTooltipsEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[ENABLE_TOOLTIPS] = enabled
        }
    }
    
    /**
     * Sets help badges enabled status
     */
    suspend fun setHelpBadgesEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[ENABLE_HELP_BADGES] = enabled
        }
    }
    
    /**
     * Resets all tutorial progress
     */
    suspend fun resetAllProgress() {
        dataStore.edit { preferences ->
            preferences[COMPLETED_TUTORIALS] = emptySet()
            preferences[SKIPPED_TUTORIALS] = emptySet()
            preferences[HAS_COMPLETED_ONBOARDING] = false
        }
    }
    
    /**
     * Checks if tutorial is completed
     */
    suspend fun isTutorialCompleted(tutorialId: String): Boolean {
        val completed = dataStore.data.map { preferences ->
            preferences[COMPLETED_TUTORIALS] ?: emptySet()
        }
        return completed.map { it.contains(tutorialId) }.toString().toBoolean()
    }
    
    /**
     * Gets tutorial completion count
     */
    val completedTutorialsCount: Flow<Int> = dataStore.data.map { preferences ->
        (preferences[COMPLETED_TUTORIALS] ?: emptySet()).size
    }
    
    /**
     * Gets total tutorial progress percentage
     */
    fun getTutorialProgress(totalTutorials: Int): Flow<Double> = dataStore.data.map { preferences ->
        val completed = (preferences[COMPLETED_TUTORIALS] ?: emptySet()).size
        if (totalTutorials > 0) (completed.toDouble() / totalTutorials) * 100 else 0.0
    }
} 