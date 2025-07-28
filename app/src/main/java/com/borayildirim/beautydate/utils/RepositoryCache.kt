package com.borayildirim.beautydate.utils

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Simple memory cache utility for repository data
 * Provides TTL-based caching with automatic cleanup
 */
@Singleton
class RepositoryCache @Inject constructor() {
    
    private val mutex = Mutex()
    private val cache = ConcurrentHashMap<String, CacheEntry>()
    
    companion object {
        private const val DEFAULT_TTL_MS = 5 * 60 * 1000L // 5 minutes
        private const val MAX_CACHE_SIZE = 100
    }
    
    data class CacheEntry(
        val data: Any?,
        val timestamp: Long = System.currentTimeMillis(),
        val ttlMs: Long = DEFAULT_TTL_MS
    ) {
        fun isExpired(): Boolean = System.currentTimeMillis() - timestamp > ttlMs
    }
    
    /**
     * Gets cached value with type safety
     */
    @Suppress("UNCHECKED_CAST")
    suspend fun <T> get(key: String): T? = mutex.withLock {
        cleanupExpired()
        val entry = cache[key]
        if (entry?.isExpired() == true) {
            cache.remove(key)
            null
        } else {
            entry?.data as? T
        }
    }
    
    /**
     * Puts value in cache with TTL
     */
    suspend fun <T> put(key: String, value: T, ttlMs: Long = DEFAULT_TTL_MS) = mutex.withLock {
        if (cache.size >= MAX_CACHE_SIZE) {
            cleanupExpired()
            // If still too large, remove oldest entries
            if (cache.size >= MAX_CACHE_SIZE) {
                cache.clear()
            }
        }
        cache[key] = CacheEntry(value, ttlMs = ttlMs)
    }
    
    /**
     * Invalidates specific key
     */
    suspend fun invalidate(key: String) = mutex.withLock {
        cache.remove(key)
    }
    
    /**
     * Invalidates all keys with prefix
     */
    suspend fun invalidatePrefix(prefix: String) = mutex.withLock {
        cache.keys.removeAll { it.startsWith(prefix) }
    }
    
    /**
     * Clears all cache
     */
    suspend fun clear() = mutex.withLock {
        cache.clear()
    }
    
    /**
     * Gets cache statistics
     */
    suspend fun getStats(): CacheStats = mutex.withLock {
        val totalSize = cache.size
        val expiredCount = cache.values.count { it.isExpired() }
        CacheStats(
            totalEntries = totalSize,
            expiredEntries = expiredCount,
            activeEntries = totalSize - expiredCount
        )
    }
    
    private fun cleanupExpired() {
        cache.entries.removeAll { it.value.isExpired() }
    }
    
    data class CacheStats(
        val totalEntries: Int,
        val expiredEntries: Int,
        val activeEntries: Int
    )
} 