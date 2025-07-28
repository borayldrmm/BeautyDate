package com.borayildirim.beautydate.data.repository

import com.borayildirim.beautydate.data.models.CustomerNote
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for customer notes
 * Defines contract for note data operations
 * Multi-tenant architecture: All operations use authenticated businessId via AuthUtil
 * Memory efficient: Flow-based reactive data access
 */
interface CustomerNoteRepository {
    
    /**
     * Gets all notes for current authenticated business
     * BusinessId filtering applied automatically via AuthUtil
     */
    fun getAllNotes(): Flow<List<CustomerNote>>
    
    /**
     * Gets notes for a specific customer in current authenticated business
     * BusinessId filtering applied automatically via AuthUtil
     */
    fun getNotesForCustomer(customerId: String): Flow<List<CustomerNote>>
    
    /**
     * Searches notes by query (customer name, phone, title, content) in current authenticated business
     * BusinessId filtering applied automatically via AuthUtil
     */
    fun searchNotes(query: String): Flow<List<CustomerNote>>
    
    /**
     * Gets important notes only for current authenticated business
     * BusinessId filtering applied automatically via AuthUtil
     */
    fun getImportantNotes(): Flow<List<CustomerNote>>
    
    /**
     * Gets a single note by ID
     */
    suspend fun getNoteById(noteId: String): CustomerNote?
    
    /**
     * Creates a new note for current authenticated business
     * BusinessId assigned automatically via AuthUtil
     */
    suspend fun createNote(note: CustomerNote): Result<CustomerNote>
    
    /**
     * Updates an existing note
     */
    suspend fun updateNote(note: CustomerNote): Result<CustomerNote>
    
    /**
     * Deletes a note
     */
    suspend fun deleteNote(noteId: String): Result<Unit>
    
    /**
     * Syncs notes with Firestore for current authenticated business
     * BusinessId applied automatically via AuthUtil
     */
    suspend fun syncWithFirestore(): Result<Unit>
    
    /**
     * Performs initial sync if needed for current authenticated business
     * BusinessId applied automatically via AuthUtil
     */
    suspend fun performInitialSync(): Result<Unit>
    
    /**
     * Gets note count for current authenticated business
     * BusinessId filtering applied automatically via AuthUtil
     */
    suspend fun getNoteCount(): Int
    
    /**
     * Gets note count for a specific customer in current authenticated business
     * BusinessId filtering applied automatically via AuthUtil
     */
    suspend fun getNoteCountForCustomer(customerId: String): Int
} 