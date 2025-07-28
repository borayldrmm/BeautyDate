package com.borayildirim.beautydate.data.models

/**
 * Enum representing days of the week with Turkish display names
 * Memory efficient: enum with computed properties
 * Used for working hours management and calendar integration
 */
enum class DayOfWeek(val displayName: String, val shortName: String) {
    MONDAY("Pazartesi", "Pzt"),
    TUESDAY("Salı", "Sal"),
    WEDNESDAY("Çarşamba", "Çar"),
    THURSDAY("Perşembe", "Per"),
    FRIDAY("Cuma", "Cum"),
    SATURDAY("Cumartesi", "Cmt"),
    SUNDAY("Pazar", "Paz");
    
    /**
     * Gets the next day in the week
     * Memory efficient: computed property
     */
    fun next(): DayOfWeek {
        return values()[(ordinal + 1) % values().size]
    }
    
    /**
     * Gets the previous day in the week
     * Memory efficient: computed property
     */
    fun previous(): DayOfWeek {
        return values()[(ordinal - 1 + values().size) % values().size]
    }
    
    /**
     * Checks if this is a weekend day
     * Memory efficient: computed property
     */
    fun isWeekend(): Boolean {
        return this == SATURDAY || this == SUNDAY
    }
    
    companion object {
        /**
         * Gets all weekdays (Monday to Friday)
         * Memory efficient: pre-computed list
         */
        fun getWeekdays(): List<DayOfWeek> {
            return listOf(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY)
        }
        
        /**
         * Gets weekend days
         * Memory efficient: pre-computed list
         */
        fun getWeekends(): List<DayOfWeek> {
            return listOf(SATURDAY, SUNDAY)
        }
    }
} 