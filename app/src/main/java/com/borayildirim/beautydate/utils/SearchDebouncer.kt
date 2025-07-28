package com.borayildirim.beautydate.utils

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Search debouncer utility for optimizing search performance
 * Prevents excessive search calls and improves user experience
 * Memory efficient: cancels previous searches automatically
 */
@Singleton
class SearchDebouncer @Inject constructor() {
    
    companion object {
        private const val DEFAULT_DEBOUNCE_MS = 300L
        private const val MIN_SEARCH_LENGTH = 2
    }
    
    /**
     * Creates a debounced search flow
     * Automatically cancels previous searches when new query arrives
     * 
     * @param initialQuery Initial search query
     * @param debounceMs Debounce delay in milliseconds
     * @param minLength Minimum query length to trigger search
     * @param searchAction Suspend function to execute search
     * @return Flow of search results
     */
    fun <T> createDebouncedSearch(
        initialQuery: String = "",
        debounceMs: Long = DEFAULT_DEBOUNCE_MS,
        minLength: Int = MIN_SEARCH_LENGTH,
        searchAction: suspend (String) -> T
    ): DebouncedSearchFlow<T> {
        return DebouncedSearchFlow(
            initialQuery = initialQuery,
            debounceMs = debounceMs,
            minLength = minLength,
            searchAction = searchAction
        )
    }
}

/**
 * Debounced search flow implementation
 * Manages search queries with automatic debouncing and cancellation
 */
class DebouncedSearchFlow<T>(
    private val initialQuery: String,
    private val debounceMs: Long,
    private val minLength: Int,
    private val searchAction: suspend (String) -> T
) {
    
    private val _query = MutableStateFlow(initialQuery)
    private val _results = MutableStateFlow<SearchResult<T>>(SearchResult.Idle)
    private val _isSearching = MutableStateFlow(false)
    
    val query: StateFlow<String> = _query.asStateFlow()
    val results: StateFlow<SearchResult<T>> = _results.asStateFlow()
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()
    
    private var searchJob: Job? = null
    
    init {
        setupSearchFlow()
    }
    
    /**
     * Updates search query
     * Automatically triggers debounced search
     */
    fun updateQuery(newQuery: String) {
        _query.value = newQuery
    }
    
    /**
     * Clears current search
     */
    fun clearSearch() {
        _query.value = ""
        _results.value = SearchResult.Idle
        _isSearching.value = false
        searchJob?.cancel()
    }
    
    /**
     * Manually triggers search with current query
     */
    fun triggerSearch() {
        searchJob?.cancel()
        executeSearch(_query.value)
    }
    
    /**
     * Sets up the reactive search flow
     */
    private fun setupSearchFlow() {
        searchJob = query
            .debounce(debounceMs)
            .distinctUntilChanged()
            .onEach { query ->
                if (query.length >= minLength) {
                    executeSearch(query)
                } else if (query.isEmpty()) {
                    _results.value = SearchResult.Idle
                    _isSearching.value = false
                } else {
                    _results.value = SearchResult.Idle
                    _isSearching.value = false
                }
            }
            .launchIn(CoroutineScope(Dispatchers.IO + SupervisorJob()))
    }
    
    /**
     * Executes search with proper error handling
     */
    private fun executeSearch(query: String) {
        searchJob?.cancel()
        
        searchJob = CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
            try {
                _isSearching.value = true
                _results.value = SearchResult.Loading
                
                val result = searchAction(query)
                _results.value = SearchResult.Success(result)
                
            } catch (e: CancellationException) {
                // Search was cancelled, don't update state
            } catch (e: Exception) {
                _results.value = SearchResult.Error(e.message ?: "Search failed")
            } finally {
                _isSearching.value = false
            }
        }
    }
    
    /**
     * Cleanup resources
     */
    fun cleanup() {
        searchJob?.cancel()
    }
}

/**
 * Search result sealed class
 */
sealed class SearchResult<out T> {
    object Idle : SearchResult<Nothing>()
    object Loading : SearchResult<Nothing>()
    data class Success<T>(val data: T) : SearchResult<T>()
    data class Error(val message: String) : SearchResult<Nothing>()
}

/**
 * Extension functions for easier SearchResult handling
 */
fun <T> SearchResult<T>.isLoading(): Boolean = this is SearchResult.Loading
fun <T> SearchResult<T>.isSuccess(): Boolean = this is SearchResult.Success
fun <T> SearchResult<T>.isError(): Boolean = this is SearchResult.Error
fun <T> SearchResult<T>.isIdle(): Boolean = this is SearchResult.Idle

fun <T> SearchResult<T>.getDataOrNull(): T? = when (this) {
    is SearchResult.Success -> data
    else -> null
}

fun <T> SearchResult<T>.getErrorOrNull(): String? = when (this) {
    is SearchResult.Error -> message
    else -> null
}

/**
 * Search utility functions
 */
object SearchUtils {
    
    /**
     * Normalizes query for better search results
     */
    fun normalizeQuery(query: String): String {
        return query.trim()
            .lowercase()
            .replace(Regex("\\s+"), " ") // Replace multiple spaces with single space
    }
    
    /**
     * Checks if query is valid for search
     */
    fun isValidQuery(query: String, minLength: Int = 2): Boolean {
        return query.trim().length >= minLength
    }
    
    /**
     * Highlights search term in text
     */
    fun highlightSearchTerm(text: String, searchTerm: String): String {
        if (searchTerm.isBlank()) return text
        
        val regex = Regex(Regex.escape(searchTerm), RegexOption.IGNORE_CASE)
        return text.replace(regex) { matchResult ->
            "**${matchResult.value}**" // Markdown-style highlighting
        }
    }
    
    /**
     * Filters list based on multiple search criteria
     */
    inline fun <T> filterByMultipleCriteria(
        list: List<T>,
        query: String,
        vararg criteria: T.() -> String
    ): List<T> {
        if (query.isBlank()) return list
        
        val normalizedQuery = normalizeQuery(query)
        
        return list.filter { item ->
            criteria.any { criterion ->
                normalizeQuery(item.criterion()).contains(normalizedQuery)
            }
        }
    }
} 