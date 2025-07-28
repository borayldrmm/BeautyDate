package com.borayildirim.beautydate.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * DataStore preferences for user session and settings
 * Manages username remembering and auto-login functionality
 */
private val Context.userDataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

/**
 * User preferences manager using DataStore
 * Handles saving and retrieving user session data
 */
@Singleton
class UserPreferences @Inject constructor(
    private val context: Context
) {
    
    companion object {
        private val REMEMBERED_USERNAME_KEY = stringPreferencesKey("remembered_username")
        private val AUTO_LOGIN_ENABLED_KEY = booleanPreferencesKey("auto_login_enabled")
        private val LAST_USER_ID_KEY = stringPreferencesKey("last_user_id")
        private val REMEMBER_USERNAME_ENABLED_KEY = booleanPreferencesKey("remember_username_enabled")
        private val BUSINESS_ID_KEY = stringPreferencesKey("business_id")
    }
    
    /**
     * Gets the remembered username as a Flow
     * @return Flow of remembered username
     */
    val rememberedUsername: Flow<String> = context.userDataStore.data
        .map { preferences ->
            preferences[REMEMBERED_USERNAME_KEY] ?: ""
        }
    
    /**
     * Gets auto-login enabled status as a Flow
     * @return Flow of auto-login status
     */
    val autoLoginEnabled: Flow<Boolean> = context.userDataStore.data
        .map { preferences ->
            preferences[AUTO_LOGIN_ENABLED_KEY] ?: false
        }
    
    /**
     * Gets the last user ID as a Flow
     * @return Flow of last user ID
     */
    val lastUserId: Flow<String> = context.userDataStore.data
        .map { preferences ->
            preferences[LAST_USER_ID_KEY] ?: ""
        }
    
    /**
     * Gets remember username enabled status as a Flow
     * @return Flow of remember username status
     */
    val rememberUsernameEnabled: Flow<Boolean> = context.userDataStore.data
        .map { preferences ->
            preferences[REMEMBER_USERNAME_ENABLED_KEY] ?: false
        }
    
    /**
     * Gets the business ID as a Flow
     * @return Flow of business ID
     */
    fun getBusinessId(): Flow<String> = context.userDataStore.data
        .map { preferences ->
            preferences[BUSINESS_ID_KEY] ?: ""
        }
    
    /**
     * Saves the remembered username
     * @param username Username to remember
     */
    suspend fun saveRememberedUsername(username: String) {
        context.userDataStore.edit { preferences ->
            preferences[REMEMBERED_USERNAME_KEY] = username
        }
    }
    
    /**
     * Saves auto-login enabled status
     * @param enabled Whether auto-login is enabled
     */
    suspend fun saveAutoLoginEnabled(enabled: Boolean) {
        context.userDataStore.edit { preferences ->
            preferences[AUTO_LOGIN_ENABLED_KEY] = enabled
        }
    }
    
    /**
     * Saves the last user ID
     * @param userId User ID to save
     */
    suspend fun saveLastUserId(userId: String) {
        context.userDataStore.edit { preferences ->
            preferences[LAST_USER_ID_KEY] = userId
        }
    }
    
    /**
     * Saves remember username enabled status
     * @param enabled Whether remember username is enabled
     */
    suspend fun saveRememberUsernameEnabled(enabled: Boolean) {
        context.userDataStore.edit { preferences ->
            preferences[REMEMBER_USERNAME_ENABLED_KEY] = enabled
        }
    }
    
    /**
     * Saves the business ID
     * @param businessId Business ID to save
     */
    suspend fun saveBusinessId(businessId: String) {
        context.userDataStore.edit { preferences ->
            preferences[BUSINESS_ID_KEY] = businessId
        }
    }
    
    /**
     * Clears all user preferences except remember username setting if enabled
     */
    suspend fun clearUserPreferences() {
        context.userDataStore.edit { preferences ->
            // Save remember username setting before clearing
            val rememberUsernameEnabled = preferences[REMEMBER_USERNAME_ENABLED_KEY] ?: false
            val rememberedUsername = if (rememberUsernameEnabled) {
                preferences[REMEMBERED_USERNAME_KEY] ?: ""
            } else {
                ""
            }
            
            // Clear all preferences
            preferences.clear()
            
            // Only restore if "Beni Hatırla" was enabled
            if (rememberUsernameEnabled && rememberedUsername.isNotEmpty()) {
                preferences[REMEMBER_USERNAME_ENABLED_KEY] = true
                preferences[REMEMBERED_USERNAME_KEY] = rememberedUsername
            }
        }
    }
} 