package com.borayildirim.beautydate.viewmodels.state

import com.borayildirim.beautydate.data.models.Appointment
import com.borayildirim.beautydate.data.models.AppointmentStatus
import com.borayildirim.beautydate.data.models.Customer

/**
 * UI State for appointment management screens
 * Contains all appointment data and UI state flags with computed properties
 * Memory efficient: immutable data class with reactive computed values
 */
data class AppointmentUiState(
    // Data
    val appointments: List<Appointment> = emptyList(),
    val filteredAppointments: List<Appointment> = emptyList(),
    val customers: List<Customer> = emptyList(),
    val selectedAppointment: Appointment? = null,
    
    // Filter and Search
    val selectedStatus: AppointmentStatus? = null,
    val searchQuery: String = "",
    
    // UI Flags
    val isLoading: Boolean = false,
    val isSyncing: Boolean = false,
    val isOnline: Boolean = true,
    val showAddAppointmentSheet: Boolean = false,
    val showAppointmentDetail: Boolean = false,
    val showStatusUpdateDialog: Boolean = false,
    val showDeleteConfirmDialog: Boolean = false,
    
    // Messages
    val errorMessage: String? = null,
    val successMessage: String? = null,
    
    // Statistics
    val appointmentStatistics: Map<AppointmentStatus, Int> = emptyMap()
) {
    /**
     * Checks if there are any appointments
     * Computed property: efficient boolean check
     */
    val hasAppointments: Boolean
        get() = appointments.isNotEmpty()
    
    /**
     * Checks if search is currently active
     * Computed property: search state
     */
    val isSearchActive: Boolean
        get() = searchQuery.isNotBlank()
    
    /**
     * Checks if filter is currently active
     * Computed property: filter state
     */
    val isFilterActive: Boolean
        get() = selectedStatus != null
    
    /**
     * Gets appointment count by status for filter chips
     * Computed property: efficient status counting
     */
    fun getAppointmentCountByStatus(status: AppointmentStatus): Int {
        return appointments.count { it.status == status }
    }
    
    /**
     * Gets total appointment count
     * Computed property: total count
     */
    val totalAppointments: Int
        get() = appointments.size
    
    /**
     * Gets scheduled appointments count (upcoming appointments)
     * Computed property: business logic specific counting
     */
    val scheduledAppointmentsCount: Int
        get() = appointments.count { it.status == AppointmentStatus.SCHEDULED }
    
    /**
     * Gets completed appointments count
     * Computed property: business logic specific counting
     */
    val completedAppointmentsCount: Int
        get() = appointments.count { it.status == AppointmentStatus.COMPLETED }
    
    /**
     * Gets cancelled appointments count
     * Computed property: business logic specific counting
     */
    val cancelledAppointmentsCount: Int
        get() = appointments.count { it.status == AppointmentStatus.CANCELLED }
    
    /**
     * Gets missed appointments count (no show)
     * Computed property: business logic specific counting
     */
    val missedAppointmentsCount: Int
        get() = appointments.count { it.status == AppointmentStatus.NO_SHOW }
    
    /**
     * Gets filtered appointments based on current search and filter state
     * Computed property: reactive filtering logic
     */
    val displayedAppointments: List<Appointment>
        get() {
            var filtered = appointments
            
            // Apply status filter
            selectedStatus?.let { status ->
                filtered = filtered.filter { it.status == status }
            }
            
            // Apply search filter
            if (searchQuery.isNotBlank()) {
                filtered = filtered.filter { appointment ->
                    appointment.customerName.contains(searchQuery, ignoreCase = true) ||
                    appointment.customerPhone.contains(searchQuery, ignoreCase = true) ||
                    appointment.serviceName.contains(searchQuery, ignoreCase = true)
                }
            }
            
            // Sort by date and time (newest first for completed, oldest first for scheduled)
            return filtered.sortedWith { a, b ->
                when {
                    a.status == AppointmentStatus.SCHEDULED && b.status == AppointmentStatus.SCHEDULED -> {
                        // For scheduled appointments, show nearest dates first
                        compareValuesBy(a, b) { "${it.appointmentDate} ${it.appointmentTime}" }
                    }
                    else -> {
                        // For other statuses, show newest first
                        compareValuesBy(b, a) { "${it.appointmentDate} ${it.appointmentTime}" }
                    }
                }
            }
        }
    
    /**
     * Checks if data is empty after filtering
     * Computed property: empty state detection
     */
    val isFilteredEmpty: Boolean
        get() = displayedAppointments.isEmpty() && hasAppointments
    
    /**
     * Gets status statistics for dashboard
     * Computed property: status distribution
     */
    val statusStatistics: Map<AppointmentStatus, Int>
        get() = AppointmentStatus.values().associateWith { status ->
            appointments.count { it.status == status }
        }
    
    /**
     * Checks if sync is required
     * Computed property: sync state
     */
    val needsSync: Boolean
        get() = !isSyncing && !isOnline
    
    /**
     * Gets today's appointments
     * Computed property: daily filtering
     */
    val todaysAppointments: List<Appointment>
        get() {
            val today = java.time.LocalDate.now().format(
                java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")
            )
            return appointments.filter { it.appointmentDate == today }
        }
    
    /**
     * Gets upcoming appointments (scheduled only)
     * Computed property: business logic filtering
     */
    val upcomingAppointments: List<Appointment>
        get() = appointments.filter { it.status == AppointmentStatus.SCHEDULED }
            .sortedWith { a, b ->
                compareValuesBy(a, b) { "${it.appointmentDate} ${it.appointmentTime}" }
            }
    
    /**
     * Validates if UI state is in a valid condition
     * Computed property: state validation
     */
    val isValidState: Boolean
        get() = !isLoading || appointments.isNotEmpty()
} 