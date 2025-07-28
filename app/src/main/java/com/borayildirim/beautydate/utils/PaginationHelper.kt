package com.borayildirim.beautydate.utils

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Simple pagination helper for large data sets
 * Provides pagination state management and loading indicators
 */
@Singleton
class PaginationHelper @Inject constructor() {
    
    companion object {
        const val DEFAULT_PAGE_SIZE = 20
        const val PREFETCH_DISTANCE = 5
    }
    
    /**
     * Creates pagination state for a list
     */
    @Composable
    fun <T> rememberPaginationState(
        items: List<T>,
        pageSize: Int = DEFAULT_PAGE_SIZE,
        onLoadMore: () -> Unit = {}
    ): PaginationState<T> {
        var currentPage by remember { mutableIntStateOf(0) }
        var isLoading by remember { mutableStateOf(false) }
        
        val paginatedItems = remember(items, currentPage) {
            items.take((currentPage + 1) * pageSize)
        }
        
        val hasMore = remember(items, currentPage) {
            items.size > (currentPage + 1) * pageSize
        }
        
        return PaginationState(
            items = paginatedItems,
            hasMore = hasMore,
            isLoading = isLoading,
            currentPage = currentPage,
            loadMore = {
                if (!isLoading && hasMore) {
                    isLoading = true
                    currentPage++
                    onLoadMore()
                    isLoading = false
                }
            }
        )
    }
    
    /**
     * Loading indicator for pagination
     */
    @Composable
    fun LoadingIndicator() {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.dp
            )
        }
    }
    
    /**
     * Checks if we should load more items based on current scroll position
     */
    fun shouldLoadMore(
        listState: LazyListState,
        itemCount: Int,
        prefetchDistance: Int = PREFETCH_DISTANCE
    ): Boolean {
        val lastVisibleIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
        return lastVisibleIndex >= itemCount - prefetchDistance
    }
}

/**
 * Pagination state holder
 */
data class PaginationState<T>(
    val items: List<T>,
    val hasMore: Boolean,
    val isLoading: Boolean,
    val currentPage: Int,
    val loadMore: () -> Unit
)

/**
 * Extension function to add pagination loading indicator to LazyColumn
 */
fun LazyListScope.paginationLoadingItem(
    isLoading: Boolean
) {
    if (isLoading) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            }
        }
    }
} 