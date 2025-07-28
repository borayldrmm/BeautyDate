package com.borayildirim.beautydate.data.local

import androidx.room.*

import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for customer notes
 * Provides efficient database operations with Flow-based reactivity
 * Memory efficient: Flow-based queries with proper indexing
 */
@Dao
interface CustomerNoteDao {
    
    /**
     * Gets all notes for a business with reactive updates
     * Memory efficient: Flow-based query with business filtering
     */
    @Query("SELECT * FROM customer_notes WHERE businessId = :businessId ORDER BY createdAt DESC")
    fun getAllNotes(businessId: String): Flow<List<CustomerNoteEntity>>
    
    /**
     * Gets notes for a specific customer
     * Memory efficient: indexed query on customerId
     */
    @Query("SELECT * FROM customer_notes WHERE customerId = :customerId AND businessId = :businessId ORDER BY createdAt DESC")
    fun getNotesForCustomer(customerId: String, businessId: String): Flow<List<CustomerNoteEntity>>
    
    /**
     * Searches notes by customer name or phone
     * Memory efficient: indexed search with LIKE operator
     */
    @Query("""
        SELECT * FROM customer_notes 
        WHERE businessId = :businessId 
        AND (customerName LIKE '%' || :query || '%' 
             OR customerPhone LIKE '%' || :query || '%'
             OR title LIKE '%' || :query || '%'
             OR content LIKE '%' || :query || '%')
        ORDER BY createdAt DESC
    """)
    fun searchNotes(businessId: String, query: String): Flow<List<CustomerNoteEntity>>
    
    /**
     * Gets important notes only
     * Memory efficient: indexed query on isImportant
     */
    @Query("SELECT * FROM customer_notes WHERE businessId = :businessId AND isImportant = 1 ORDER BY createdAt DESC")
    fun getImportantNotes(businessId: String): Flow<List<CustomerNoteEntity>>
    
    /**
     * Gets a single note by ID
     * Memory efficient: primary key lookup
     */
    @Query("SELECT * FROM customer_notes WHERE id = :noteId")
    suspend fun getNoteById(noteId: String): CustomerNoteEntity?
    
    /**
     * Inserts a new note
     * Memory efficient: single insert operation
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: CustomerNoteEntity)
    
    /**
     * Inserts multiple notes (for sync operations)
     * Memory efficient: batch insert operation
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotes(notes: List<CustomerNoteEntity>)
    
    /**
     * Updates an existing note
     * Memory efficient: single update operation
     */
    @Update
    suspend fun updateNote(note: CustomerNoteEntity)
    
    /**
     * Deletes a note
     * Memory efficient: single delete operation
     */
    @Delete
    suspend fun deleteNote(note: CustomerNoteEntity)
    
    /**
     * Deletes a note by ID
     * Memory efficient: primary key delete
     */
    @Query("DELETE FROM customer_notes WHERE id = :noteId")
    suspend fun deleteNoteById(noteId: String)
    
    /**
     * Deletes all notes for a business (for cleanup)
     * Memory efficient: bulk delete with business filter
     */
    @Query("DELETE FROM customer_notes WHERE businessId = :businessId")
    suspend fun deleteAllNotesForBusiness(businessId: String)
    
    /**
     * Gets note count for a business
     * Memory efficient: count query with business filter
     */
    @Query("SELECT COUNT(*) FROM customer_notes WHERE businessId = :businessId")
    suspend fun getNoteCount(businessId: String): Int
    
    /**
     * Gets note count for a specific customer
     * Memory efficient: count query with customer filter
     */
    @Query("SELECT COUNT(*) FROM customer_notes WHERE customerId = :customerId AND businessId = :businessId")
    suspend fun getNoteCountForCustomer(customerId: String, businessId: String): Int
} 