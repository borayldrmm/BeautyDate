package com.borayildirim.beautydate.data.remote.models

import com.borayildirim.beautydate.data.models.WorkingHours
import com.borayildirim.beautydate.data.models.DayHours
import com.google.firebase.firestore.PropertyName

/**
 * Firestore model for working hours
 * Memory efficient: flat structure optimized for Firestore operations
 * Supports real-time sync and cross-device data consistency
 */
data class WorkingHoursFirestore(
    @PropertyName("id")
    val id: String = "",
    
    @PropertyName("businessId")
    val businessId: String = "",
    
    // Monday schedule
    @PropertyName("mondayIsWorking")
    val mondayIsWorking: Boolean = true,
    @PropertyName("mondayStartTime")
    val mondayStartTime: String = "09:00",
    @PropertyName("mondayEndTime")
    val mondayEndTime: String = "19:00",
    
    // Tuesday schedule
    @PropertyName("tuesdayIsWorking")
    val tuesdayIsWorking: Boolean = true,
    @PropertyName("tuesdayStartTime")
    val tuesdayStartTime: String = "09:00",
    @PropertyName("tuesdayEndTime")
    val tuesdayEndTime: String = "19:00",
    
    // Wednesday schedule
    @PropertyName("wednesdayIsWorking")
    val wednesdayIsWorking: Boolean = true,
    @PropertyName("wednesdayStartTime")
    val wednesdayStartTime: String = "09:00",
    @PropertyName("wednesdayEndTime")
    val wednesdayEndTime: String = "19:00",
    
    // Thursday schedule
    @PropertyName("thursdayIsWorking")
    val thursdayIsWorking: Boolean = true,
    @PropertyName("thursdayStartTime")
    val thursdayStartTime: String = "09:00",
    @PropertyName("thursdayEndTime")
    val thursdayEndTime: String = "19:00",
    
    // Friday schedule
    @PropertyName("fridayIsWorking")
    val fridayIsWorking: Boolean = true,
    @PropertyName("fridayStartTime")
    val fridayStartTime: String = "09:00",
    @PropertyName("fridayEndTime")
    val fridayEndTime: String = "19:00",
    
    // Saturday schedule
    @PropertyName("saturdayIsWorking")
    val saturdayIsWorking: Boolean = true,
    @PropertyName("saturdayStartTime")
    val saturdayStartTime: String = "09:00",
    @PropertyName("saturdayEndTime")
    val saturdayEndTime: String = "17:00",
    
    // Sunday schedule
    @PropertyName("sundayIsWorking")
    val sundayIsWorking: Boolean = false,
    @PropertyName("sundayStartTime")
    val sundayStartTime: String = "09:00",
    @PropertyName("sundayEndTime")
    val sundayEndTime: String = "19:00",
    
    @PropertyName("createdAt")
    val createdAt: String = "",
    
    @PropertyName("updatedAt")
    val updatedAt: String = ""
) {
    /**
     * Converts Firestore model to domain model
     * Memory efficient: direct property mapping
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
    
    /**
     * Converts to Map for Firestore batch operations
     * Memory efficient: direct property extraction without intermediate objects
     */
    fun toMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "businessId" to businessId,
            "mondayIsWorking" to mondayIsWorking,
            "mondayStartTime" to mondayStartTime,
            "mondayEndTime" to mondayEndTime,
            "tuesdayIsWorking" to tuesdayIsWorking,
            "tuesdayStartTime" to tuesdayStartTime,
            "tuesdayEndTime" to tuesdayEndTime,
            "wednesdayIsWorking" to wednesdayIsWorking,
            "wednesdayStartTime" to wednesdayStartTime,
            "wednesdayEndTime" to wednesdayEndTime,
            "thursdayIsWorking" to thursdayIsWorking,
            "thursdayStartTime" to thursdayStartTime,
            "thursdayEndTime" to thursdayEndTime,
            "fridayIsWorking" to fridayIsWorking,
            "fridayStartTime" to fridayStartTime,
            "fridayEndTime" to fridayEndTime,
            "saturdayIsWorking" to saturdayIsWorking,
            "saturdayStartTime" to saturdayStartTime,
            "saturdayEndTime" to saturdayEndTime,
            "sundayIsWorking" to sundayIsWorking,
            "sundayStartTime" to sundayStartTime,
            "sundayEndTime" to sundayEndTime,
            "createdAt" to createdAt,
            "updatedAt" to updatedAt
        )
    }
    
    companion object {
        /**
         * Creates Firestore model from domain model
         * Memory efficient: direct property assignment
         */
        fun fromWorkingHours(workingHours: WorkingHours): WorkingHoursFirestore {
            return WorkingHoursFirestore(
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