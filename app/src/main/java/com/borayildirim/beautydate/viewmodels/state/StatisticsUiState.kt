package com.borayildirim.beautydate.viewmodels.state

import com.borayildirim.beautydate.data.models.BusinessStatistics
import com.borayildirim.beautydate.data.models.StatisticsPeriod
import com.borayildirim.beautydate.data.models.StatisticsCategory

/**
 * UI state for Statistics screen
 * Follows existing UiState pattern with immutable data classes
 * Memory efficient: minimal state with computed properties
 */
data class StatisticsUiState(
    // Loading states
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isExporting: Boolean = false,
    
    // Data state
    val statistics: BusinessStatistics? = null,
    
    // Filter states
    val selectedPeriod: StatisticsPeriod = StatisticsPeriod.ALL_TIME,
    val selectedCategory: StatisticsCategory = StatisticsCategory.OVERVIEW,
    
    // Messages
    val error: String? = null,
    val exportMessage: String? = null
) {
    
    /**
     * Returns whether data is available
     */
    val hasData: Boolean
        get() = statistics != null
    
    /**
     * Returns whether any loading operation is in progress
     */
    val isAnyLoading: Boolean
        get() = isLoading || isRefreshing || isExporting
    
    /**
     * Returns formatted period display text
     */
    val periodDisplayText: String
        get() = selectedPeriod.displayName
    
    /**
     * Returns formatted category display text
     */
    val categoryDisplayText: String
        get() = selectedCategory.displayName
    
    /**
     * Returns financial statistics if available
     */
    val financialStats: com.borayildirim.beautydate.data.models.FinancialStatistics?
        get() = statistics?.financialStats
    
    /**
     * Returns customer statistics if available
     */
    val customerStats: com.borayildirim.beautydate.data.models.CustomerStatistics?
        get() = statistics?.customerStats
    
    /**
     * Returns appointment statistics if available
     */
    val appointmentStats: com.borayildirim.beautydate.data.models.AppointmentStatistics?
        get() = statistics?.appointmentStats
    
    /**
     * Returns employee statistics if available
     */
    val employeeStats: com.borayildirim.beautydate.data.models.EmployeeStatistics?
        get() = statistics?.employeeStats
    
    /**
     * Returns service statistics if available
     */
    val serviceStats: com.borayildirim.beautydate.data.models.ServiceStatistics?
        get() = statistics?.serviceStats
    
    /**
     * Returns available period options
     */
    val availablePeriods: List<StatisticsPeriod>
        get() = StatisticsPeriod.values().toList()
    
    /**
     * Returns available category options
     */
    val availableCategories: List<StatisticsCategory>
        get() = StatisticsCategory.values().toList()
    
    /**
     * Returns key performance indicators for dashboard
     */
    val keyMetrics: KeyMetrics
        get() = KeyMetrics(
            totalRevenue = financialStats?.formattedTotalRevenue ?: "0 ₺",
            netProfit = financialStats?.formattedNetProfit ?: "0 ₺",
            totalCustomers = customerStats?.totalCustomers ?: 0,
            completedAppointments = appointmentStats?.completedAppointments ?: 0,
            activeEmployees = employeeStats?.activeEmployees ?: 0,
            activeServices = serviceStats?.activeServices ?: 0,
            profitMargin = financialStats?.formattedProfitMargin ?: "0%",
            completionRate = appointmentStats?.formattedCompletionRate ?: "0%"
        )
}

/**
 * Key metrics data class for dashboard overview
 * Memory efficient: formatted strings for display
 */
data class KeyMetrics(
    val totalRevenue: String,
    val netProfit: String,
    val totalCustomers: Int,
    val completedAppointments: Int,
    val activeEmployees: Int,
    val activeServices: Int,
    val profitMargin: String,
    val completionRate: String
) 