package com.borayildirim.beautydate.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.borayildirim.beautydate.data.models.Customer
import com.borayildirim.beautydate.data.models.CustomerNote
import com.borayildirim.beautydate.data.repository.CustomerNoteRepository
import com.borayildirim.beautydate.data.repository.CustomerRepository
import com.borayildirim.beautydate.utils.AuthUtil
import com.borayildirim.beautydate.viewmodels.state.CustomerNoteUiState
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for customer notes management
 * Handles note CRUD operations with offline-first approach
 * Multi-tenant architecture: BusinessId handled automatically by AuthUtil
 * Memory efficient: Flow-based state management with debounced search
 */
@HiltViewModel
class CustomerNoteViewModel @Inject constructor(
    private val noteRepository: CustomerNoteRepository,
    private val customerRepository: CustomerRepository,
    private val firebaseAuth: FirebaseAuth,
    private val authUtil: AuthUtil
) : ViewModel() {

    private val _uiState = MutableStateFlow(CustomerNoteUiState())
    val uiState: StateFlow<CustomerNoteUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null
    private var isInitialized: Boolean = false

    init {
        initializeNotes()
    }

    /**
     * Initializes notes data with automatic authentication check
     * Memory efficient: reuses existing data if already loaded
     * BusinessId handled automatically by AuthUtil
     */
    fun initializeNotes() {
        
        // Check if user is authenticated before proceeding
        if (!authUtil.isUserAuthenticated()) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                errorMessage = authUtil.getAuthErrorMessage()
            )
            return
        }
        
        val businessId = authUtil.getCurrentBusinessIdSafe()
        
        if (isInitialized && _uiState.value.notes.isNotEmpty() && _uiState.value.customers.isNotEmpty()) {
            return
        }
        
        isInitialized = true
        
        // Set loading state
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        
        // Perform initial sync
        performInitialSyncIfNeeded()
        
        // Load notes and customers concurrently
        loadNotes()
        loadCustomers()
    }

    /**
     * Performs initial sync if needed
     * BusinessId handled automatically by repository layer
     */
    private fun performInitialSyncIfNeeded() {
        viewModelScope.launch {
            try {
                customerRepository.performInitialSync()
            } catch (e: Exception) {
            }
        }
    }

    /**
     * Loads all notes from repository
     * Memory efficient: Flow-based reactive data collection
     * BusinessId handled automatically by repository layer
     */
    private fun loadNotes() {
        viewModelScope.launch {
            try {
                noteRepository.getAllNotes()
                    .catch { exception ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "Notlar yüklenirken hata oluştu: ${exception.message}"
                        )
                    }
                    .collect { notes ->
                        _uiState.value = _uiState.value.copy(
                            notes = notes,
                            isLoading = false
                        )
                    }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Notlar yüklenirken hata oluştu: ${e.message}"
                )
            }
        }
    }

    /**
     * Loads customers for note association
     * BusinessId handled automatically by repository layer
     */
    private fun loadCustomers() {
        viewModelScope.launch {
            try {
                customerRepository.getAllCustomers()
                    .catch { exception ->
                    }
                    .collect { customers ->
                        _uiState.value = _uiState.value.copy(customers = customers)
                    }
            } catch (e: Exception) {
            }
        }
    }

    /**
     * Searches notes by query
     * Memory efficient: Flow-based search with reactive updates
     */
    fun searchNotes(query: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(searchQuery = query)
                
                if (query.isBlank()) {
                    loadNotes()
                } else {
                    noteRepository.searchNotes(query)
                        .catch { error ->
                        }
                        .collect { filteredNotes ->
                            _uiState.value = _uiState.value.copy(filteredNotes = filteredNotes)
                        }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Arama sırasında hata oluştu"
                )
            }
        }
    }

    /**
     * Shows add note sheet
     */
    fun showAddNoteSheet() {
        // Ensure customers are loaded before showing the sheet
        if (_uiState.value.customers.isEmpty()) {
            loadCustomers()
        }
        
        _uiState.value = _uiState.value.copy(
            showAddNoteSheet = true,
            selectedNote = null,
            noteTitle = "",
            noteContent = "",
            isImportant = false,
            selectedCustomer = null
        )
    }

    /**
     * Shows edit note sheet
     */
    fun showEditNoteSheet(note: CustomerNote) {
        val customer = _uiState.value.customers.find { it.id == note.customerId }
        _uiState.value = _uiState.value.copy(
            showEditNoteSheet = true,
            selectedNote = note,
            noteTitle = note.title,
            noteContent = note.content,
            isImportant = note.isImportant,
            selectedCustomer = customer
        )
    }

    /**
     * Hides note sheets
     */
    fun hideNoteSheets() {
        _uiState.value = _uiState.value.copy(
            showAddNoteSheet = false,
            showEditNoteSheet = false,
            selectedNote = null,
            noteTitle = "",
            noteContent = "",
            isImportant = false,
            selectedCustomer = null
        )
    }

    /**
     * Updates note form fields
     */
    fun updateNoteTitle(title: String) {
        _uiState.value = _uiState.value.copy(noteTitle = title)
    }

    fun updateNoteContent(content: String) {
        _uiState.value = _uiState.value.copy(noteContent = content)
    }

    fun updateIsImportant(isImportant: Boolean) {
        _uiState.value = _uiState.value.copy(isImportant = isImportant)
    }

    fun selectCustomer(customer: Customer) {
        _uiState.value = _uiState.value.copy(selectedCustomer = customer)
    }

    /**
     * Creates a new note
     */
    fun createNote() {
        val state = _uiState.value
        if (!state.isFormValid) {
            _uiState.value = state.copy(errorMessage = "Lütfen tüm alanları doldurun")
            return
        }

        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isCreatingNote = true, errorMessage = null)

                val note = CustomerNote.createForCustomer(
                    customer = state.selectedCustomer!!,
                    businessId = authUtil.getCurrentBusinessIdSafe()
                ).copy(
                    title = state.noteTitle,
                    content = state.noteContent,
                    isImportant = state.isImportant
                )

                noteRepository.createNote(note)
                    .onSuccess {
                        _uiState.value = _uiState.value.copy(
                            isCreatingNote = false,
                            showAddNoteSheet = false,
                            successMessage = "Not başarıyla oluşturuldu",
                            noteTitle = "",
                            noteContent = "",
                            isImportant = false,
                            selectedCustomer = null
                        )
                    }
                    .onFailure { error ->
                        _uiState.value = _uiState.value.copy(
                            isCreatingNote = false,
                            errorMessage = "Not oluşturulurken hata oluştu: ${error.message}"
                        )
                    }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isCreatingNote = false,
                    errorMessage = "Not oluşturulurken hata oluştu: ${e.message}"
                )
            }
        }
    }

    /**
     * Updates an existing note
     */
    fun updateNote() {
        val state = _uiState.value
        val selectedNote = state.selectedNote
        
        if (selectedNote == null || !state.isFormValid) {
            _uiState.value = state.copy(errorMessage = "Lütfen tüm alanları doldurun")
            return
        }

        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isUpdatingNote = true, errorMessage = null)

                val updatedNote = selectedNote.copy(
                    title = state.noteTitle,
                    content = state.noteContent,
                    isImportant = state.isImportant
                )

                noteRepository.updateNote(updatedNote)
                    .onSuccess {
                        _uiState.value = _uiState.value.copy(
                            isUpdatingNote = false,
                            showEditNoteSheet = false,
                            successMessage = "Not başarıyla güncellendi",
                            noteTitle = "",
                            noteContent = "",
                            isImportant = false,
                            selectedCustomer = null
                        )
                    }
                    .onFailure { error ->
                        _uiState.value = _uiState.value.copy(
                            isUpdatingNote = false,
                            errorMessage = "Not güncellenirken hata oluştu: ${error.message}"
                        )
                    }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isUpdatingNote = false,
                    errorMessage = "Not güncellenirken hata oluştu: ${e.message}"
                )
            }
        }
    }

    /**
     * Deletes a note
     */
    fun deleteNote(noteId: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isDeletingNote = true, errorMessage = null)

                noteRepository.deleteNote(noteId)
                    .onSuccess {
                        _uiState.value = _uiState.value.copy(
                            isDeletingNote = false,
                            successMessage = "Not başarıyla silindi"
                        )
                    }
                    .onFailure { error ->
                        _uiState.value = _uiState.value.copy(
                            isDeletingNote = false,
                            errorMessage = "Not silinirken hata oluştu: ${error.message}"
                        )
                    }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isDeletingNote = false,
                    errorMessage = "Not silinirken hata oluştu: ${e.message}"
                )
            }
        }
    }

    /**
     * Syncs notes with Firestore
     * Memory efficient: background sync operation
     */
    fun syncNotes() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                
                val result = noteRepository.syncWithFirestore()
                
                if (result.isSuccess) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Notlar başarıyla senkronize edildi"
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.exceptionOrNull()?.message ?: "Senkronizasyon başarısız"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Bilinmeyen hata"
                )
            }
        }
    }

    /**
     * Clears success and error messages
     */
    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            successMessage = null,
            errorMessage = null
        )
    }
} 