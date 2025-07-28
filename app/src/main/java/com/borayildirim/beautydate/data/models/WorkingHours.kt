package com.borayildirim.beautydate.data.models

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Represents weekly working hours for a business
 * Memory efficient: immutable data class with computed properties
 * Follows clean architecture principles with domain logic encapsulation
 */
data class WorkingHours(
    val id: String = "",
    val businessId: String = "",
    val monday: DayHours = DayHours(isWorking = true, startTime = "09:00", endTime = "19:00"),
    val tuesday: DayHours = DayHours(isWorking = true, startTime = "09:00", endTime = "19:00"),
    val wednesday: DayHours = DayHours(isWorking = true, startTime = "09:00", endTime = "19:00"),
    val thursday: DayHours = DayHours(isWorking = true, startTime = "09:00", endTime = "19:00"),
    val friday: DayHours = DayHours(isWorking = true, startTime = "09:00", endTime = "19:00"),
    val saturday: DayHours = DayHours(isWorking = true, startTime = "10:00", endTime = "17:00"),
    val sunday: DayHours = DayHours(isWorking = false, startTime = "09:00", endTime = "19:00"),
    val createdAt: String = "",
    val updatedAt: String = ""
) {
    /**
     * Gets working hours for a specific day
     * Memory efficient: direct property access, no collections
     */
    fun getDayHours(dayOfWeek: DayOfWeek): DayHours {
        return when (dayOfWeek) {
            DayOfWeek.MONDAY -> monday
            DayOfWeek.TUESDAY -> tuesday
            DayOfWeek.WEDNESDAY -> wednesday
            DayOfWeek.THURSDAY -> thursday
            DayOfWeek.FRIDAY -> friday
            DayOfWeek.SATURDAY -> saturday
            DayOfWeek.SUNDAY -> sunday
        }
    }
    
    /**
     * Updates working hours for a specific day
     * Memory efficient: immutable copy with single day update
     */
    fun updateDayHours(dayOfWeek: DayOfWeek, dayHours: DayHours): WorkingHours {
        return when (dayOfWeek) {
            DayOfWeek.MONDAY -> copy(monday = dayHours)
            DayOfWeek.TUESDAY -> copy(tuesday = dayHours)
            DayOfWeek.WEDNESDAY -> copy(wednesday = dayHours)
            DayOfWeek.THURSDAY -> copy(thursday = dayHours)
            DayOfWeek.FRIDAY -> copy(friday = dayHours)
            DayOfWeek.SATURDAY -> copy(saturday = dayHours)
            DayOfWeek.SUNDAY -> copy(sunday = dayHours)
        }
    }
    
    /**
     * Applies the same working hours to all days
     * Memory efficient: single DayHours instance reused
     */
    fun applyToAllDays(dayHours: DayHours): WorkingHours {
        return copy(
            monday = dayHours,
            tuesday = dayHours,
            wednesday = dayHours,
            thursday = dayHours,
            friday = dayHours,
            saturday = dayHours,
            sunday = dayHours,
            updatedAt = getCurrentTimestamp()
        )
    }
    
    /**
     * Applies working hours to weekdays only, keeps weekends unchanged
     * Memory efficient: selective update, weekend days preserved
     */
    fun applyToWeekdays(dayHours: DayHours): WorkingHours {
        return copy(
            monday = dayHours,
            tuesday = dayHours,
            wednesday = dayHours,
            thursday = dayHours,
            friday = dayHours,
            updatedAt = getCurrentTimestamp()
        )
    }
    
    /**
     * Gets all days that are currently working
     * Memory efficient: computed list, no stored state
     */
    fun getWorkingDays(): List<DayOfWeek> {
        return DayOfWeek.values().filter { getDayHours(it).isWorking }
    }
    
    /**
     * Gets all days that are closed
     * Memory efficient: computed list, no stored state
     */
    fun getClosedDays(): List<DayOfWeek> {
        return DayOfWeek.values().filter { !getDayHours(it).isWorking }
    }
    
    /**
     * Validates if all working hours are properly configured
     * Memory efficient: single pass validation
     */
    fun isValid(): Boolean {
        return DayOfWeek.values().all { day ->
            getDayHours(day).isValidTimeRange()
        }
    }
    
    /**
     * Creates an updated copy with current timestamp
     * Memory efficient: single property update
     */
    fun withUpdatedTimestamp(): WorkingHours {
        return copy(updatedAt = getCurrentTimestamp())
    }
    
    /**
     * Checks if the business is open on a specific day
     * Memory efficient: direct property access
     */
    fun isOpenOnDay(dayOfWeek: DayOfWeek): Boolean {
        return getDayHours(dayOfWeek).isWorking
    }
    
    private fun getCurrentTimestamp(): String {
        return LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    }
    
    companion object {
        /**
         * Creates default working hours for a new business
         * Memory efficient: pre-defined default configuration
         */
        fun createDefault(businessId: String): WorkingHours {
            val currentTime = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            return WorkingHours(
                businessId = businessId,
                createdAt = currentTime,
                updatedAt = currentTime
            )
        }
        
        /**
         * Creates working hours with all days closed
         * Memory efficient: single DayHours instance with isWorking = false
         */
        fun createAllClosed(businessId: String): WorkingHours {
            val closedDay = DayHours(isWorking = false)
            return createDefault(businessId).applyToAllDays(closedDay)
        }
    }
} 