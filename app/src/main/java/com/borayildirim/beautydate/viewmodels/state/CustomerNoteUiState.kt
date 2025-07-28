package com.borayildirim.beautydate.viewmodels.state

import com.borayildirim.beautydate.data.models.Customer
import com.borayildirim.beautydate.data.models.CustomerNote

/**
 * UI state for customer notes management screens
 * Holds all state needed for notes listing and management
 * Memory efficient: immutable data class with computed properties
 */
data class CustomerNoteUiState(
    val notes: List<CustomerNote> = emptyList(),
    val filteredNotes: List<CustomerNote> = emptyList(),
    val customers: List<Customer> = emptyList(),
    val searchQuery: String = "",
    val selectedCustomer: Customer? = null,
    val selectedNote: CustomerNote? = null,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isSyncing: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val totalNotes: Int = 0,
    // Note creation/editing state
    val showAddNoteSheet: Boolean = false,
    val showEditNoteSheet: Boolean = false,
    val isCreatingNote: Boolean = false,
    val isUpdatingNote: Boolean = false,
    val isDeletingNote: Boolean = false,
    // Form state
    val noteTitle: String = "",
    val noteContent: String = "",
    val isImportant: Boolean = false
) {
    /**
     * Returns notes to display based on search query and filters
     * Memory efficient: computed property with lazy evaluation
     */
    val displayNotes: List<CustomerNote>
        get() = if (searchQuery.isBlank()) notes else filteredNotes
        
    /**
     * Returns true if no notes are available
     */
    val isEmpty: Boolean
        get() = notes.isEmpty() && !isLoading
        
    /**
     * Returns true if search results are empty but there are notes
     */
    val isSearchEmpty: Boolean
        get() = searchQuery.isNotBlank() && filteredNotes.isEmpty() && notes.isNotEmpty()
        
    /**
     * Returns notes grouped by customer for display
     * Memory efficient: groupBy with minimal object creation
     */
    val notesByCustomer: Map<String, List<CustomerNote>>
        get() = displayNotes.groupBy { it.customerName }
        
    /**
     * Returns important notes only
     */
    val importantNotes: List<CustomerNote>
        get() = displayNotes.filter { it.isImportant }
        
    /**
     * Returns recent notes (last 7 days)
     */
    val recentNotes: List<CustomerNote>
        get() {
            val sevenDaysAgo = System.currentTimeMillis() / 1000 - (7 * 24 * 60 * 60)
            return displayNotes.filter { note ->
                note.createdAt?.seconds ?: 0 >= sevenDaysAgo
            }
        }
        
    /**
     * Returns note statistics
     * Memory efficient: computed on-demand
     */
    val noteStats: NoteStats
        get() {
            val totalCount = notes.size
            val importantCount = notes.count { it.isImportant }
            val customerCount = notes.map { it.customerId }.distinct().size
            val recentCount = recentNotes.size
            
            return NoteStats(
                totalNotes = totalCount,
                importantNotes = importantCount,
                customersWithNotes = customerCount,
                recentNotes = recentCount
            )
        }
        
    /**
     * Returns true if form is valid for creating/updating note
     */
    val isFormValid: Boolean
        get() = noteTitle.isNotBlank() && 
                noteContent.isNotBlank() && 
                selectedCustomer != null
}

/**
 * Note statistics data class
 * Memory efficient: simple data holder
 */
data class NoteStats(
    val totalNotes: Int,
    val importantNotes: Int,
    val customersWithNotes: Int,
    val recentNotes: Int
) 