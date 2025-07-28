package com.borayildirim.beautydate.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index
import com.borayildirim.beautydate.data.models.WorkingHours
import com.borayildirim.beautydate.data.models.DayHours
import com.borayildirim.beautydate.data.models.DayOfWeek

/**
 * Room entity for working hours storage
 * Memory efficient: normalized structure with indexed businessId
 * Supports offline-first architecture with local data persistence
 */
@Entity(
    tableName = "working_hours",
    indices = [
        Index(value = ["businessId"], unique = true),
        Index(value = ["updatedAt"])
    ]
)
data class WorkingHoursEntity(
    @PrimaryKey
    val id: String,
    val businessId: String,
    
    // Monday hours
    val mondayIsWorking: Boolean,
    val mondayStartTime: String,
    val mondayEndTime: String,
    
    // Tuesday hours  
    val tuesdayIsWorking: Boolean,
    val tuesdayStartTime: String,
    val tuesdayEndTime: String,
    
    // Wednesday hours
    val wednesdayIsWorking: Boolean,
    val wednesdayStartTime: String,
    val wednesdayEndTime: String,
    
    // Thursday hours
    val thursdayIsWorking: Boolean,
    val thursdayStartTime: String,
    val thursdayEndTime: String,
    
    // Friday hours
    val fridayIsWorking: Boolean,
    val fridayStartTime: String,
    val fridayEndTime: String,
    
    // Saturday hours
    val saturdayIsWorking: Boolean,
    val saturdayStartTime: String,
    val saturdayEndTime: String,
    
    // Sunday hours
    val sundayIsWorking: Boolean,
    val sundayStartTime: String,
    val sundayEndTime: String,
    
    val createdAt: String,
    val updatedAt: String
) {
    /**
     * Converts entity to domain model
     * Memory efficient: direct property mapping, no intermediate objects
     */
    fun toWorkingHours(): WorkingHours {
        return WorkingHours(
            id = id,
            businessId = businessId,
            monday = DayHours(mondayIsWorking, mondayStartTime, mondayEndTime),
            tuesday = DayHours(tuesdayIsWorking, tuesdayStartTime, tuesdayEndTime),
            wednesday = DayHours(wednesdayIsWorking, wednesdayStartTime, wednesdayEndTime),
            thursday = DayHours(thursdayIsWorking, thursdayStartTime, thursdayEndTime),
            friday = DayHours(fridayIsWorking, fridayStartTime, fridayEndTime),
            saturday = DayHours(saturdayIsWorking, saturdayStartTime, saturdayEndTime),
            sunday = DayHours(sundayIsWorking, sundayStartTime, sundayEndTime),
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
    
    companion object {
        /**
         * Creates entity from domain model
         * Memory efficient: direct property extraction, no object creation overhead
         */
        fun fromWorkingHours(workingHours: WorkingHours): WorkingHoursEntity {
            return WorkingHoursEntity(
                id = workingHours.id.ifEmpty { 
                    "working_hours_${workingHours.businessId}" 
                },
                businessId = workingHours.businessId,
                
                mondayIsWorking = workingHours.monday.isWorking,
                mondayStartTime = workingHours.monday.startTime,
                mondayEndTime = workingHours.monday.endTime,
                
                tuesdayIsWorking = workingHours.tuesday.isWorking,
                tuesdayStartTime = workingHours.tuesday.startTime,
                tuesdayEndTime = workingHours.tuesday.endTime,
                
                wednesdayIsWorking = workingHours.wednesday.isWorking,
                wednesdayStartTime = workingHours.wednesday.startTime,
                wednesdayEndTime = workingHours.wednesday.endTime,
                
                thursdayIsWorking = workingHours.thursday.isWorking,
                thursdayStartTime = workingHours.thursday.startTime,
                thursdayEndTime = workingHours.thursday.endTime,
                
                fridayIsWorking = workingHours.friday.isWorking,
                fridayStartTime = workingHours.friday.startTime,
                fridayEndTime = workingHours.friday.endTime,
                
                saturdayIsWorking = workingHours.saturday.isWorking,
                saturdayStartTime = workingHours.saturday.startTime,
                saturdayEndTime = workingHours.saturday.endTime,
                
                sundayIsWorking = workingHours.sunday.isWorking,
                sundayStartTime = workingHours.sunday.startTime,
                sundayEndTime = workingHours.sunday.endTime,
                
                createdAt = workingHours.createdAt,
                updatedAt = workingHours.updatedAt
            )
        }
    }
} 