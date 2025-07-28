package com.borayildirim.beautydate.data.local

import androidx.room.TypeConverter
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * Room TypeConverter for LocalDateTime and LocalTime
 * Handles conversion between database strings and Java time objects
 */
class DateTimeConverter {
    
    private val dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    private val timeFormatter = DateTimeFormatter.ISO_LOCAL_TIME
    
    /**
     * Converts LocalDateTime to String for database storage
     */
    @TypeConverter
    fun fromLocalDateTime(value: LocalDateTime?): String? {
        return value?.format(dateTimeFormatter)
    }
    
    /**
     * Converts String from database to LocalDateTime
     */
    @TypeConverter
    fun toLocalDateTime(value: String?): LocalDateTime? {
        return value?.let { LocalDateTime.parse(it, dateTimeFormatter) }
    }
    
    /**
     * Converts LocalTime to String for database storage
     */
    @TypeConverter
    fun fromLocalTime(value: LocalTime?): String? {
        return value?.format(timeFormatter)
    }
    
    /**
     * Converts String from database to LocalTime
     */
    @TypeConverter
    fun toLocalTime(value: String?): LocalTime? {
        return value?.let { LocalTime.parse(it, timeFormatter) }
    }
} 