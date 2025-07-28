package com.borayildirim.beautydate.data.models

/**
 * Represents working hours for a single day
 * Memory efficient: minimal data structure for day scheduling
 * Used in WorkingHours model for weekly schedule management
 */
data class DayHours(
    val isWorking: Boolean = true,
    val startTime: String = "09:00", // HH:mm format (24-hour)
    val endTime: String = "19:00"    // HH:mm format (24-hour)
) {
    /**
     * Validates if the time range is valid
     * Memory efficient: computed property, no stored state
     */
    fun isValidTimeRange(): Boolean {
        if (!isWorking) return true
        
        return try {
            val start = startTime.split(":").map { it.toInt() }
            val end = endTime.split(":").map { it.toInt() }
            
            val startMinutes = start[0] * 60 + start[1]
            val endMinutes = end[0] * 60 + end[1]
            
            endMinutes > startMinutes
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Gets display text for this day's working hours
     * Memory efficient: computed string, no caching needed
     */
    fun getDisplayText(): String {
        return if (isWorking) {
            "$startTime - $endTime"
        } else {
            "Tatil günü"
        }
    }
    
    /**
     * Creates a copy with updated times
     * Memory efficient: immutable data class operations
     */
    fun updateTimes(newStartTime: String, newEndTime: String): DayHours {
        return copy(startTime = newStartTime, endTime = newEndTime)
    }
} 