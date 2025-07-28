package com.borayildirim.beautydate.data.models

import com.google.firebase.Timestamp
import java.util.*

/**
 * Business statistics data model
 * Aggregates statistics from all business areas for dashboard display
 * Memory efficient: calculated values and aggregated data
 */
data class BusinessStatistics(
    val businessId: String,
    val generatedAt: Timestamp = Timestamp.now(),
    
    // Financial Statistics
    val financialStats: FinancialStatistics,
    
    // Customer Statistics  
    val customerStats: CustomerStatistics,
    
    // Appointment Statistics
    val appointmentStats: AppointmentStatistics,
    
    // Employee Statistics
    val employeeStats: EmployeeStatistics,
    
    // Service Statistics
    val serviceStats: ServiceStatistics
) {
    
    /**
     * Validates business statistics data
     */
    fun validate(): ValidationResult {
        if (businessId.isBlank()) {
            return ValidationResult.invalid("Business ID cannot be blank")
        }
        
        return ValidationResult.valid()
    }
    
    /**
     * Returns formatted generation time
     */
    val formattedGeneratedAt: String
        get() {
            val date = generatedAt.toDate()
            return java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(date)
        }
}

/**
 * Financial statistics data
 */
data class FinancialStatistics(
    val totalRevenue: Double = 0.0,
    val totalIncome: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val netProfit: Double = totalIncome - totalExpenses,
    val creditPayments: Double = 0.0,
    val cashPayments: Double = 0.0,
    val pendingPayments: Double = 0.0,
    val completedPayments: Double = 0.0,
    val totalTransactions: Int = 0,
    val averageTransactionAmount: Double = 0.0
) {
    
    /**
     * Returns credit vs cash payment ratio
     */
    val creditCashRatio: Double
        get() = if (cashPayments > 0) creditPayments / cashPayments else 0.0
        
    /**
     * Returns profit margin percentage
     */
    val profitMargin: Double
        get() = if (totalIncome > 0) (netProfit / totalIncome) * 100 else 0.0
        
    /**
     * Returns formatted values
     */
    val formattedTotalRevenue: String
        get() = "${totalRevenue.toInt()} ₺"
        
    val formattedNetProfit: String
        get() = "${netProfit.toInt()} ₺"
        
    val formattedProfitMargin: String
        get() = "%.1f%%".format(profitMargin)
}

/**
 * Customer statistics data
 */
data class CustomerStatistics(
    val totalCustomers: Int = 0,
    val newCustomersThisMonth: Int = 0,
    val activeCustomers: Int = 0,
    val customerRetentionRate: Double = 0.0,
    val averageCustomerValue: Double = 0.0,
    val topCustomerSpending: Double = 0.0,
    val customersWithNotes: Int = 0,
    val totalCustomerNotes: Int = 0
) {
    
    /**
     * Returns customer growth percentage
     */
    val customerGrowthRate: Double
        get() = if (totalCustomers > 0) (newCustomersThisMonth.toDouble() / totalCustomers) * 100 else 0.0
        
    /**
     * Returns formatted values
     */
    val formattedAverageValue: String
        get() = "${averageCustomerValue.toInt()} ₺"
        
    val formattedGrowthRate: String
        get() = "%.1f%%".format(customerGrowthRate)
}

/**
 * Appointment statistics data
 */
data class AppointmentStatistics(
    val totalAppointments: Int = 0,
    val completedAppointments: Int = 0,
    val upcomingAppointments: Int = 0,
    val missedAppointments: Int = 0,
    val cancelledAppointments: Int = 0,
    val appointmentsThisMonth: Int = 0,
    val averageAppointmentValue: Double = 0.0,
    val bussiestDayOfWeek: String = "",
    val peakHour: Int = 0
) {
    
    /**
     * Returns completion rate percentage
     */
    val completionRate: Double
        get() = if (totalAppointments > 0) (completedAppointments.toDouble() / totalAppointments) * 100 else 0.0
        
    /**
     * Returns miss rate percentage
     */
    val missRate: Double
        get() = if (totalAppointments > 0) (missedAppointments.toDouble() / totalAppointments) * 100 else 0.0
        
    /**
     * Returns formatted values
     */
    val formattedCompletionRate: String
        get() = "%.1f%%".format(completionRate)
        
    val formattedAverageValue: String
        get() = "${averageAppointmentValue.toInt()} ₺"
}

/**
 * Employee statistics data
 */
data class EmployeeStatistics(
    val totalEmployees: Int = 0,
    val activeEmployees: Int = 0,
    val inactiveEmployees: Int = 0,
    val averageSkillsPerEmployee: Double = 0.0,
    val mostSkillfulEmployee: String = "",
    val employeeUtilizationRate: Double = 0.0,
    val averageEmployeeRating: Double = 0.0
) {
    
    /**
     * Returns active percentage
     */
    val activePercentage: Double
        get() = if (totalEmployees > 0) (activeEmployees.toDouble() / totalEmployees) * 100 else 0.0
        
    /**
     * Returns formatted values
     */
    val formattedActivePercentage: String
        get() = "%.1f%%".format(activePercentage)
        
    val formattedAverageSkills: String
        get() = "%.1f".format(averageSkillsPerEmployee)
}

/**
 * Service statistics data
 */
data class ServiceStatistics(
    val totalServices: Int = 0,
    val activeServices: Int = 0,
    val averageServicePrice: Double = 0.0,
    val mostExpensiveService: Double = 0.0,
    val cheapestService: Double = 0.0,
    val mostPopularService: String = "",
    val mostPopularServiceCount: Int = 0,
    val serviceCategories: Int = 0,
    val averageServicesPerCategory: Double = 0.0
) {
    
    /**
     * Returns price range
     */
    val priceRange: Double
        get() = mostExpensiveService - cheapestService
        
    /**
     * Returns formatted values
     */
    val formattedAveragePrice: String
        get() = "${averageServicePrice.toInt()} ₺"
        
    val formattedPriceRange: String
        get() = "${cheapestService.toInt()} ₺ - ${mostExpensiveService.toInt()} ₺"
}

/**
 * Statistics calculation period
 */
enum class StatisticsPeriod(val displayName: String) {
    DAILY("Günlük"),
    WEEKLY("Haftalık"), 
    MONTHLY("Aylık"),
    YEARLY("Yıllık"),
    ALL_TIME("Tümü")
}

/**
 * Statistics category for filtering
 */
enum class StatisticsCategory(val displayName: String) {
    FINANCIAL("Finansal"),
    CUSTOMERS("Müşteriler"),
    APPOINTMENTS("Randevular"),
    EMPLOYEES("Çalışanlar"),
    SERVICES("Hizmetler"),
    OVERVIEW("Genel Bakış")
}

/**
 * Validation result data class
 */
data class ValidationResult(
    val isValid: Boolean,
    val errorMessage: String = ""
) {
    companion object {
        fun valid() = ValidationResult(true)
        fun invalid(message: String) = ValidationResult(false, message)
    }
} 