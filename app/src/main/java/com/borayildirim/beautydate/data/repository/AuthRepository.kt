package com.borayildirim.beautydate.data.repository

import com.borayildirim.beautydate.data.local.UserPreferences
import com.borayildirim.beautydate.data.models.User
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for authentication operations
 * Handles Firebase Auth and Firestore operations for user management
 */
@Singleton
class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val userPreferences: UserPreferences
) {
    
    /**
     * Gets current Firebase user
     * @return Current Firebase user or null
     */
    fun getCurrentUser(): FirebaseUser? = auth.currentUser
    
    /**
     * Gets current user as Flow
     * @return Flow of current user
     */
    fun getCurrentUserFlow(): Flow<FirebaseUser?> {
        return kotlinx.coroutines.flow.flow {
            emit(auth.currentUser)
        }
    }
    
    /**
     * Signs in user with email and password
     * @param email User email
     * @param password User password
     * @return Result with success status and error message
     */
    suspend fun signInWithEmailAndPassword(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            result.user?.let { user ->
                // Save user ID for auto-login
                userPreferences.saveLastUserId(user.uid)
            }
            Result.success(result.user!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Signs in user with username and password
     * First finds the user by username, then authenticates with email
     * @param username Username
     * @param password User password
     * @return Result with success status and error message
     */
    suspend fun signInWithUsernameAndPassword(username: String, password: String): Result<FirebaseUser> {
        return try {
            // Find user by username first with improved error handling
            val userEmail = getUserEmailByUsername(username)
            if (userEmail == null) {
                return Result.failure(Exception("Kullanıcı adı bulunamadı"))
            }
            
            // Sign in with email and password
            val result = auth.signInWithEmailAndPassword(userEmail, password).await()
            result.user?.let { user ->
                // Save user ID for auto-login
                userPreferences.saveLastUserId(user.uid)
            }
            Result.success(result.user!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Finds user email by username
     * Enhanced method to avoid cache issues and improve reliability
     * @param username Username to search for
     * @return User email or null if not found
     */
    private suspend fun getUserEmailByUsername(username: String): String? {
        return try {
            
            // First try the secure username mapping collection
            val email = getUserEmailByUsernameSecure(username)
            if (email != null) {
                return email
            }
            
            // Fallback to users collection if secure method fails
            
            // Try both exact case and lowercase
            val searchUsernames = listOf(username, username.lowercase())
            
            for (searchUsername in searchUsernames) {
                
                // Try default source first (cache then server)
                val querySnapshot = firestore.collection("users")
                    .whereEqualTo("username", searchUsername)
                    .limit(1)
                    .get()
                    .await()
                
                
                val foundEmail = querySnapshot.documents.firstOrNull()?.getString("email")
                
                if (foundEmail != null) {
                    // Create mapping for future secure lookups
                    createUsernameMapping(username, foundEmail)
                    return foundEmail
                }
            }
            
            null
            
        } catch (e: Exception) {
            // Log the exception for debugging
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Alternative secure method: Finds user email by username using separate collection
     * This method uses a separate 'usernames' collection for security
     * @param username Username to search for
     * @return User email or null if not found
     */
    private suspend fun getUserEmailByUsernameSecure(username: String): String? {
        return try {
            
            // Try both exact case and lowercase
            val searchUsernames = listOf(username, username.lowercase())
            
            for (searchUsername in searchUsernames) {
                
                // Query the separate usernames collection
                val document = firestore.collection("usernames")
                    .document(searchUsername)
                    .get()
                    .await()
                
                if (document.exists()) {
                    val email = document.getString("email")
                    if (email != null) {
                        return email
                    } else {
                    }
                } else {
                }
            }
            
            null
            
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Creates username mapping document for secure lookup
     * @param username Username
     * @param email Email
     */
    private suspend fun createUsernameMapping(username: String, email: String) {
        try {
            val lowercaseUsername = username.lowercase()
            
            firestore.collection("usernames")
                .document(lowercaseUsername)
                .set(mapOf(
                    "email" to email,
                    "originalUsername" to username,
                    "createdAt" to com.google.firebase.Timestamp.now()
                ))
                .await()
            
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * Creates new user account with business details
     * @param username Username
     * @param email User email
     * @param password User password
     * @param businessName Business name
     * @param phoneNumber Phone number
     * @param address Business address
     * @param taxNumber Tax number (optional)
     * @return Result with success status and error message
     */
    suspend fun createUserWithEmailAndPassword(
        username: String,
        email: String,
        password: String,
        businessName: String,
        phoneNumber: String,
        address: String,
        taxNumber: String = ""
    ): Result<FirebaseUser> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            result.user?.let { user ->
                // Create user document in Firestore
                val userData = User(
                    id = user.uid,
                    username = username.lowercase(), // Store username in lowercase for consistency
                    email = email,
                    businessName = businessName,
                    phoneNumber = phoneNumber,
                    address = address,
                    taxNumber = taxNumber,
                    createdAt = Timestamp.now(),
                    emailVerified = false,
                    acceptedTerms = true, // Always true during registration
                    termsAcceptedAt = Timestamp.now()
                )
                
                
                firestore.collection("users")
                    .document(user.uid)
                    .set(userData)
                    .await()
                
                
                // Create username mapping for secure lookup
                try {
                    createUsernameMapping(username, email)
                } catch (mappingError: Exception) {
                    mappingError.printStackTrace()
                }
                
                // Send email verification
                user.sendEmailVerification().await()
                
                // Save user ID for auto-login
                userPreferences.saveLastUserId(user.uid)
            }
            Result.success(result.user!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Signs out current user
     */
    suspend fun signOut() {
        auth.signOut()
        userPreferences.clearUserPreferences()
    }
    
    /**
     * Deletes the current user account and all associated data
     * @param email User's email for verification
     * @param password User's password for re-authentication
     * @return Result indicating success or failure
     */
    suspend fun deleteAccount(email: String, password: String): Result<Unit> {
        return try {
            val currentUser = auth.currentUser
                ?: return Result.failure(Exception("Kullanıcı oturumu bulunamadı"))
            
            // Re-authenticate user before deletion (security requirement)
            val credential = EmailAuthProvider.getCredential(email, password)
            currentUser.reauthenticate(credential).await()
            
            val userId = currentUser.uid
            
            // Delete all user data from Firestore
            val batch = firestore.batch()
            
            // Delete main user document
            val userDoc = firestore.collection("users").document(userId)
            batch.delete(userDoc)
            
            // Delete username mapping
            val usernameQuery = firestore.collection("username_mappings")
                .whereEqualTo("email", email)
                .limit(1)
                .get()
                .await()
            
            usernameQuery.documents.forEach { doc ->
                batch.delete(doc.reference)
            }
            
            // Delete all user's business data collections
            val collections = listOf("customers", "appointments", "employees", "services", "expenses", "payments")
            for (collection in collections) {
                val userDataQuery = firestore.collection(collection)
                    .whereEqualTo("businessId", userId)
                    .limit(500) // Firestore batch limit
                    .get()
                    .await()
                
                userDataQuery.documents.forEach { doc ->
                    batch.delete(doc.reference)
                }
            }
            
            // Execute batch delete
            batch.commit().await()
            
            // Delete the Firebase Auth user account
            currentUser.delete().await()
            
            // Clear local preferences
            userPreferences.clearUserPreferences()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Sends password reset email to the provided email address
     * @param email Email address to send reset link
     * @return Result indicating success or failure
     */
    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Checks if user email is verified
     * @param user Firebase user
     * @return True if email is verified
     */
    suspend fun isEmailVerified(user: FirebaseUser): Boolean {
        // Reload user to get latest email verification status
        user.reload().await()
        
        // Update Firestore document with latest verification status
        if (user.isEmailVerified) {
            firestore.collection("users")
                .document(user.uid)
                .update("emailVerified", true)
                .await()
        }
        
        return user.isEmailVerified
    }
    
    /**
     * Gets user data from Firestore
     * @param userId User ID
     * @return User data or null
     */
    suspend fun getUserData(userId: String): User? {
        return try {
            val document = firestore.collection("users")
                .document(userId)
                .get()
                .await()
            
            document.toObject(User::class.java)
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Updates user data in Firestore
     * @param user User data to update
     * @return Result with success status
     */
    suspend fun updateUserData(user: User): Result<Unit> {
        return try {
            firestore.collection("users")
                .document(user.id)
                .set(user)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Saves remembered username
     * @param username Username to remember
     */
    suspend fun saveRememberedUsername(username: String) {
        userPreferences.saveRememberedUsername(username)
    }
    
    /**
     * Gets remembered username as Flow
     * @return Flow of remembered username
     */
    fun getRememberedUsername(): Flow<String> {
        return userPreferences.rememberedUsername
    }
    
    /**
     * Gets remember username enabled status as Flow
     * @return Flow of remember username enabled status
     */
    fun getRememberUsernameEnabled(): Flow<Boolean> {
        return userPreferences.rememberUsernameEnabled
    }
    
    /**
     * Saves remember username enabled status
     * @param enabled Whether remember username is enabled
     */
    suspend fun saveRememberUsernameEnabled(enabled: Boolean) {
        userPreferences.saveRememberUsernameEnabled(enabled)
    }
    
    /**
     * Saves auto-login enabled status
     * @param enabled Whether auto-login is enabled
     */
    suspend fun saveAutoLoginEnabled(enabled: Boolean) {
        userPreferences.saveAutoLoginEnabled(enabled)
    }
    
    /**
     * Gets auto-login enabled status as Flow
     * @return Flow of auto-login status
     */
    fun getAutoLoginEnabled(): Flow<Boolean> {
        return userPreferences.autoLoginEnabled
    }
    
    /**
     * Gets last user ID as Flow
     * @return Flow of last user ID
     */
    fun getLastUserId(): Flow<String> {
        return userPreferences.lastUserId
    }


} 