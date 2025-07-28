package com.borayildirim.beautydate.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.borayildirim.beautydate.data.models.Expense
import com.borayildirim.beautydate.data.models.ExpenseCategory
import com.borayildirim.beautydate.data.repository.ExpenseRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Expense ViewModel with CRUD operations and monthly filtering
 * Handles business expense management with ExpenseRepository integration
 * Memory efficient: Flow-based reactive data with real-time updates
 */
@HiltViewModel
class ExpenseViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExpenseUiState())
    val uiState: StateFlow<ExpenseUiState> = _uiState.asStateFlow()

    private val businessId: String
        get() = firebaseAuth.currentUser?.uid ?: ""

    init {
        loadExpenses()
    }
    
    /**
     * Initializes expenses with cross-device sync
     * Should be called from MainScreen for proper sync timing
     */
    fun initializeExpenses() {
        
        if (businessId.isBlank()) {
            return
        }
        
        // Perform sync first for cross-device consistency
        viewModelScope.launch {
            try {
                val syncResult = expenseRepository.performComprehensiveSync(businessId)
                if (syncResult.isSuccess) {
                } else {
                }
            } catch (e: Exception) {
            }
        }
        
        // Load expenses will be triggered automatically by Flow
    }

    /**
     * Loads all expenses for current business
     * Memory efficient: Flow-based reactive updates
     */
    private fun loadExpenses() {
        if (businessId.isBlank()) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                expenseRepository.getAllExpenses(businessId)
                    .catch { exception ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "Giderler yüklenirken hata oluştu: ${exception.message}"
                        )
                    }
                    .collect { expenses ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            expenses = expenses,
                            errorMessage = null
                        )
                    }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Giderler yüklenirken hata oluştu: ${e.message}"
                )
            }
        }
    }

    /**
     * Adds a new expense
     * Memory efficient: single operation with validation
     */
    fun addExpense(
        category: ExpenseCategory,
        subcategory: String,
        amount: Double,
        description: String,
        expenseDate: String,
        notes: String = ""
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                val expense = Expense.createNew(
                    category = category,
                    subcategory = subcategory,
                    amount = amount,
                    description = description,
                    expenseDate = expenseDate,
                    businessId = businessId,
                    notes = notes
                )

                val result = expenseRepository.addExpense(expense)
                
                result.fold(
                    onSuccess = {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            successMessage = "Gider başarıyla eklendi"
                        )
                    },
                    onFailure = { exception ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "Gider eklenirken hata oluştu: ${exception.message}"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Gider eklenirken hata oluştu: ${e.message}"
                )
            }
        }
    }

    /**
     * Updates an existing expense
     * Memory efficient: single operation with validation
     */
    fun updateExpense(
        expenseId: String,
        category: ExpenseCategory,
        subcategory: String,
        amount: Double,
        description: String,
        notes: String = ""
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                // Get current expense to preserve other fields
                val currentExpense = _uiState.value.expenses.find { it.id == expenseId }
                if (currentExpense == null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Güncellenecek gider bulunamadı"
                    )
                    return@launch
                }

                val updatedExpense = currentExpense.copy(
                    category = category,
                    subcategory = subcategory,
                    amount = amount,
                    description = description,
                    notes = notes,
                    updatedAt = com.google.firebase.Timestamp.now()
                )

                val result = expenseRepository.updateExpense(updatedExpense)
                
                result.fold(
                    onSuccess = {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            successMessage = "Gider başarıyla güncellendi"
                        )
                    },
                    onFailure = { exception ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "Gider güncellenirken hata oluştu: ${exception.message}"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Gider güncellenirken hata oluştu: ${e.message}"
                )
            }
        }
    }

    /**
     * Deletes an expense
     * Memory efficient: single operation with confirmation
     */
    fun deleteExpense(expenseId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                val result = expenseRepository.deleteExpense(expenseId, businessId)
                
                result.fold(
                    onSuccess = {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            successMessage = "Gider başarıyla silindi"
                        )
                    },
                    onFailure = { exception ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "Gider silinirken hata oluştu: ${exception.message}"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Gider silinirken hata oluştu: ${e.message}"
                )
            }
        }
    }

    /**
     * Filters expenses by category
     * Memory efficient: computed filtering without data duplication
     */
    fun filterExpensesByCategory(category: ExpenseCategory?) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
    }

    /**
     * Searches expenses by query
     * Memory efficient: reactive search with Flow
     */
    fun searchExpenses(query: String) {
        if (query.isBlank()) {
            _uiState.value = _uiState.value.copy(searchQuery = "")
            return
        }

        _uiState.value = _uiState.value.copy(searchQuery = query)
        
        viewModelScope.launch {
            try {
                expenseRepository.searchExpenses(query, businessId)
                    .collect { searchResults ->
                        _uiState.value = _uiState.value.copy(
                            searchResults = searchResults
                        )
                    }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Arama sırasında hata oluştu: ${e.message}"
                )
            }
        }
    }

    /**
     * Gets expenses for specific month
     * Business logic: monthly expense filtering
     */
    fun getExpensesForMonth(monthKey: String): List<Expense> {
        return if (monthKey == "Tümü") {
            _uiState.value.expenses
        } else {
            _uiState.value.expenses.filter { expense ->
                expense.getExpenseMonth() == monthKey
            }
        }
    }

    /**
     * Gets monthly expense total
     * Business logic: monthly expense calculation
     */
    fun getMonthlyExpenseTotal(monthKey: String): Double {
        return getExpensesForMonth(monthKey).sumOf { it.amount }
    }

    /**
     * Gets expenses by category with total
     * Business logic: category-based expense analysis
     */
    fun getExpensesByCategory(): Map<ExpenseCategory, Pair<List<Expense>, Double>> {
        return _uiState.value.expenses.groupBy { it.category }.mapValues { (_, expenses) ->
            expenses to expenses.sumOf { it.amount }
        }
    }

    /**
     * Syncs expenses with Firestore
     * Memory efficient: background sync operation
     */
    fun syncExpenses() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSyncing = true)

            try {
                val result = expenseRepository.syncWithFirestore(businessId)
                
                result.fold(
                    onSuccess = {
                        _uiState.value = _uiState.value.copy(
                            isSyncing = false,
                            successMessage = "Senkronizasyon tamamlandı"
                        )
                    },
                    onFailure = { exception ->
                        _uiState.value = _uiState.value.copy(
                            isSyncing = false,
                            errorMessage = "Senkronizasyon hatası: ${exception.message}"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSyncing = false,
                    errorMessage = "Senkronizasyon hatası: ${e.message}"
                )
            }
        }
    }

    /**
     * Clears success message
     */
    fun clearSuccessMessage() {
        _uiState.value = _uiState.value.copy(successMessage = null)
    }

    /**
     * Clears error message
     */
    fun clearErrorMessage() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    /**
     * Clears all messages
     */
    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            successMessage = null,
            errorMessage = null
        )
    }
}

/**
 * Expense UI State
 * Memory efficient: immutable data class with computed properties
 */
data class ExpenseUiState(
    val isLoading: Boolean = false,
    val isSyncing: Boolean = false,
    val expenses: List<Expense> = emptyList(),
    val searchResults: List<Expense> = emptyList(),
    val selectedCategory: ExpenseCategory? = null,
    val searchQuery: String = "",
    val successMessage: String? = null,
    val errorMessage: String? = null
) {
    /**
     * Gets filtered expenses based on search and category
     * Computed property: efficient filtering
     */
    val filteredExpenses: List<Expense>
        get() {
            var filtered = if (searchQuery.isNotBlank()) searchResults else expenses
            
            selectedCategory?.let { category ->
                filtered = filtered.filter { it.category == category }
            }
            
            return filtered.sortedByDescending { it.expenseDate }
        }

    /**
     * Gets total expenses amount
     * Computed property: efficient calculation
     */
    val totalExpenses: Double
        get() = expenses.sumOf { it.amount }

    /**
     * Gets formatted total expenses
     * Computed property: string formatting
     */
    val formattedTotalExpenses: String
        get() = "${totalExpenses.toInt()} ₺"

    /**
     * Gets expense count
     * Computed property: count calculation
     */
    val expenseCount: Int
        get() = expenses.size

    /**
     * Checks if there are any expenses
     * Computed property: boolean check
     */
    val hasExpenses: Boolean
        get() = expenses.isNotEmpty()
} 