package com.borayildirim.beautydate.viewmodels.state

import com.borayildirim.beautydate.data.models.Appointment
import com.borayildirim.beautydate.data.models.WorkingHours
import com.borayildirim.beautydate.data.models.Customer
import com.borayildirim.beautydate.data.models.Service
import java.time.LocalDate
import java.time.LocalTime

/**
 * UI state for Calendar screen
 * Memory efficient: immutable data class with computed properties
 * Supports offline-first appointment management with working hours integration
 */
data class CalendarUiState(
    // Date and time management
    val selectedDate: LocalDate = LocalDate.now(),
    val selectedMonth: Int = LocalDate.now().monthValue,
    val selectedYear: Int = LocalDate.now().year,
    val currentWeekDays: List<LocalDate> = run {
        val today = LocalDate.now()
        // Always center the week around today - 3 days before, today, 3 days after  
        val startDay = today.minusDays(3)
        (0..6).map { startDay.plusDays(it.toLong()) }
    },
    
    // Monthly calendar data - 42 days (6 weeks * 7 days) to fill calendar grid
    val currentMonthDays: List<LocalDate> = run {
        val today = LocalDate.now()
        val firstDayOfMonth = today.withDayOfMonth(1)
        val startOfCalendar = firstDayOfMonth.minusDays(firstDayOfMonth.dayOfWeek.value.toLong() - 1)
        (0..41).map { startOfCalendar.plusDays(it.toLong()) }
    },
    
    // Working hours and time slots
    val workingHours: WorkingHours? = null,
    val isWorkingDay: Boolean = false,
    val dayStartTime: LocalTime? = null,
    val dayEndTime: LocalTime? = null,
    val timeSlots: List<TimeSlot> = emptyList(),
    
    // Appointments data
    val appointments: List<Appointment> = emptyList(),
    val selectedTimeSlot: TimeSlot? = null,
    val appointmentsForSelectedDate: List<Appointment> = emptyList(),
    
    // UI interaction states
    val showSlotSelectionCard: Boolean = false,
    val showMonthSelector: Boolean = false,
    val isLoadingAppointments: Boolean = false,
    val isLoadingWorkingHours: Boolean = false,
    val isSyncing: Boolean = false,
    
    // Available data for appointment creation
    val availableCustomers: List<Customer> = emptyList(),
    val availableServices: List<Service> = emptyList(),
    
    // Error and success states
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val isOnline: Boolean = true
) {
    
    /**
     * Gets working hours for the selected date
     * Memory efficient: computed property, no stored state
     */
    val selectedDateWorkingHours: String
        get() {
            if (!isWorkingDay || dayStartTime == null || dayEndTime == null) {
                return "Kapalı"
            }
            return "${dayStartTime.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"))}-${dayEndTime.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"))}"
        }
    
    /**
     * Checks if selected date is today
     * Memory efficient: computed property
     */
    val isSelectedDateToday: Boolean
        get() = selectedDate == LocalDate.now()
    
    /**
     * Checks if selected date is in the past
     * Memory efficient: computed property
     */
    val isSelectedDatePast: Boolean
        get() = selectedDate.isBefore(LocalDate.now())
    
    /**
     * Gets available time slots count
     * Memory efficient: computed property
     */
    val availableSlotCount: Int
        get() = timeSlots.count { it.status == TimeSlotStatus.AVAILABLE }
    
    /**
     * Gets booked time slots count
     * Memory efficient: computed property
     */
    val bookedSlotCount: Int
        get() = timeSlots.count { it.status == TimeSlotStatus.BOOKED }
    
    /**
     * Checks if any loading operation is in progress
     * Memory efficient: computed property
     */
    val isLoading: Boolean
        get() = isLoadingAppointments || isLoadingWorkingHours || isSyncing
}

/**
 * Represents a time slot in the calendar
 * Memory efficient: data class with business logic encapsulation
 */
data class TimeSlot(
    val time: LocalTime,
    val status: TimeSlotStatus,
    val appointment: Appointment? = null
) {
    /**
     * Gets formatted time string
     * Memory efficient: computed property
     */
    val formattedTime: String
        get() = time.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"))
    
    /**
     * Checks if slot is clickable
     * Memory efficient: computed property
     */
    val isClickable: Boolean
        get() = status == TimeSlotStatus.AVAILABLE || status == TimeSlotStatus.BOOKED
    
    /**
     * Gets display text for slot
     * Memory efficient: computed property
     */
    val displayText: String
        get() = when (status) {
            TimeSlotStatus.AVAILABLE -> "Boş"
            TimeSlotStatus.BOOKED -> appointment?.customerName ?: "Dolu"
            TimeSlotStatus.PAST -> "Geçti"
            TimeSlotStatus.OUTSIDE_WORKING_HOURS -> "Çalışma Dışı"
        }
}

/**
 * Time slot status enumeration
 * Memory efficient: sealed enum for type safety
 */
enum class TimeSlotStatus {
    AVAILABLE,              // Slot is available for booking
    BOOKED,                 // Slot has an appointment
    PAST,                   // Slot is in the past
    OUTSIDE_WORKING_HOURS   // Slot is outside working hours
}

/**
 * Calendar day representation
 * Memory efficient: data class for week view
 */
data class CalendarDay(
    val date: LocalDate,
    val isToday: Boolean = false,
    val isSelected: Boolean = false,
    val appointmentCount: Int = 0,
    val isWorking: Boolean = true
) {
    /**
     * Gets day of month
     * Memory efficient: computed property
     */
    val dayOfMonth: Int
        get() = date.dayOfMonth
    
    /**
     * Gets day name (short)
     * Memory efficient: computed property
     */
    val dayName: String
        get() = when (date.dayOfWeek) {
            java.time.DayOfWeek.MONDAY -> "Pzt"
            java.time.DayOfWeek.TUESDAY -> "Sal"
            java.time.DayOfWeek.WEDNESDAY -> "Çar"
            java.time.DayOfWeek.THURSDAY -> "Per"
            java.time.DayOfWeek.FRIDAY -> "Cum"
            java.time.DayOfWeek.SATURDAY -> "Cmt"
            java.time.DayOfWeek.SUNDAY -> "Paz"
        }
} 